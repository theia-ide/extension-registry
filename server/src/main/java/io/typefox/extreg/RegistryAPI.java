/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/
package io.typefox.extreg;

import java.io.InputStream;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.collect.Lists;

import io.typefox.extreg.entities.Extension;
import io.typefox.extreg.entities.ExtensionReview;
import io.typefox.extreg.entities.ExtensionVersion;
import io.typefox.extreg.entities.Publisher;
import io.typefox.extreg.json.ExtensionJson;
import io.typefox.extreg.json.ReviewJson;
import io.typefox.extreg.json.ReviewResultJson;
import io.typefox.extreg.json.SearchResultJson;
import io.typefox.extreg.util.ErrorResultException;

@Path("/api")
public class RegistryAPI {

    @Inject
    EntityManager entityManager;

    @Inject
    EntityService entities;

    @Inject
    LocalRegistryService local;

    @Inject
    UpstreamRegistryService upstream;

    protected Iterable<IExtensionRegistry> getRegistries() {
        return Lists.newArrayList(local, upstream);
    }

    @GET
    @Path("/{publisher}")
    @Produces(MediaType.APPLICATION_JSON)
    public void getPublisher(@PathParam("publisher") String publisherName,
                             @Suspended AsyncResponse response) {
        handle(response, registry -> registry.getPublisher(publisherName));
    }

    @GET
    @Path("/{publisher}/{extension}")
    @Produces(MediaType.APPLICATION_JSON)
    public void getExtension(@PathParam("publisher") String publisherName,
                             @PathParam("extension") String extensionName,
                             @Suspended AsyncResponse response) {
        handle(response, registry -> registry.getExtension(publisherName, extensionName));
    }

    @GET
    @Path("/{publisher}/{extension}/{version}")
    @Produces(MediaType.APPLICATION_JSON)
    public void getExtensionVersion(@PathParam("publisher") String publisherName,
                                    @PathParam("extension") String extensionName,
                                    @PathParam("version") String version,
                                    @Suspended AsyncResponse response) {
        handle(response, registry -> registry.getExtensionVersion(publisherName, extensionName, version));
    }

    @GET
    @Path("/{publisher}/{extension}/file/{fileName}")
    public void getFile(@PathParam("publisher") String publisherName,
                        @PathParam("extension") String extensionName,
                        @PathParam("fileName") String fileName,
                        @Suspended AsyncResponse response) {
        handle(response, registry -> registry.getFile(publisherName, extensionName, fileName));
    }

    @GET
    @Path("/{publisher}/{extension}/{version}/file/{fileName}")
    public void getFile(@PathParam("publisher") String publisherName,
                        @PathParam("extension") String extensionName,
                        @PathParam("version") String version,
                        @PathParam("fileName") String fileName,
                        @Suspended AsyncResponse response) {
        String type;
        if (fileName.endsWith(".vsix"))
            type = MediaType.APPLICATION_OCTET_STREAM;
        else if (fileName.contains("."))
            type = URLConnection.guessContentTypeFromName(fileName);
        else
            type = MediaType.TEXT_PLAIN;
        handle(response, type, registry -> registry.getFile(publisherName, extensionName, version, fileName));
    }

    @GET
    @Path("/{publisher}/{extension}/reviews")
    @Produces(MediaType.APPLICATION_JSON)
    public void getReviews(@PathParam("publisher") String publisherName,
                           @PathParam("extension") String extensionName,
                           @Suspended AsyncResponse response) {
        handle(response, registry -> registry.getReviews(publisherName, extensionName));
    }

    @GET
    @Path("/-/search")
    @Produces(MediaType.APPLICATION_JSON)
    public void search(@QueryParam("query") String query,
                       @QueryParam("category") String category,
                       @QueryParam("size") @DefaultValue("20") int size,
                       @QueryParam("offset") @DefaultValue("0") int offset,
                       @Suspended AsyncResponse response) {
        var result = new SearchResultJson();
        result.extensions = new ArrayList<>(size);
        search(query, category, size, offset, getRegistries().iterator(), result, response);
    }

    private void search(String query, String category, int size, int offset,
            Iterator<IExtensionRegistry> registryIterator, SearchResultJson result,
            AsyncResponse response) {
        if (!registryIterator.hasNext())
            response.resume(result);
        var registry = registryIterator.next();
        registry.search(query, category, size, offset)
                .handle((subResult, throwable) -> {
                    if (subResult == null) {
                        search(query, category, size, offset, registryIterator, result, response);
                    } else {
                        result.extensions.addAll(subResult.extensions);
                        result.offset = Math.max(result.offset, subResult.offset);
                        var subResultSize = subResult.extensions.size();
                        if (subResultSize < size)
                            search(query, category,
                                   size - subResultSize,
                                   Math.max(offset - subResult.offset - subResultSize, 0),
                                   registryIterator, result, response);
                        else
                            response.resume(result);
                    }
                    return null;
                });
    }

    @POST
    @Path("/-/publish")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public ExtensionJson publish(InputStream content) {
        try (var processor = new ExtensionProcessor(content)) {
            var publisher = entities.findPublisherOptional(processor.getPublisherName());
            if (publisher.isEmpty()) {
                var pub = new Publisher();
                pub.setName(processor.getPublisherName());
                entityManager.persist(pub);
                publisher = Optional.of(pub);
            }
            var extension = entities.findExtensionOptional(processor.getExtensionName(), publisher.get());
            var extVersion = processor.getMetadata();
            extVersion.setTimestamp(LocalDateTime.now(ZoneId.of("UTC")));
            if (extension.isEmpty()) {
                var ext = new Extension();
                ext.setName(processor.getExtensionName());
                ext.setPublisher(publisher.get());
                ext.setLatest(extVersion);
                entityManager.persist(ext);
                extension = Optional.of(ext);
            } else {
                entities.checkUniqueVersion(extVersion.getVersion(), extension.get());
                if (entities.isLatestVersion(extVersion.getVersion(), extension.get()))
                    extension.get().setLatest(extVersion);
            }
            extVersion.setExtension(extension.get());
            extVersion.setExtensionFileName(
                    publisher.get().getName()
                    + "." + extension.get().getName()
                    + "-" + extVersion.getVersion()
                    + ".vsix");

            entityManager.persist(extVersion);
            var binary = processor.getBinary(extVersion);
            entityManager.persist(binary);
            var readme = processor.getReadme(extVersion);
            if (readme != null)
                entityManager.persist(readme);
            var icon = processor.getIcon(extVersion);
            if (icon != null)
                entityManager.persist(icon);
            processor.getExtensionDependencies().forEach(dep -> addDependency(dep, extVersion));
            processor.getBundledExtensions().forEach(dep -> addBundledExtension(dep, extVersion));

            return local.toJson(extVersion, false);
        } catch (ErrorResultException | NoResultException exc) {
            return ExtensionJson.error(exc.getMessage());
        }
    }

    private void addDependency(String dependency, ExtensionVersion extVersion) {
        var split = dependency.split("\\.");
        if (split.length != 2)
            return;
        try {
            var publisher = entities.findPublisher(split[0]);
            var extension = entities.findExtension(split[1], publisher);
            var depList = extVersion.getDependencies();
            if (depList == null) {
                depList = new ArrayList<Extension>();
                extVersion.setDependencies(depList);
            }
            depList.add(extension);
        } catch (NoResultException exc) {
            // Ignore the entry
        }
    }

    private void addBundledExtension(String bundled, ExtensionVersion extVersion) {
        var split = bundled.split("\\.");
        if (split.length != 2)
            return;
        try {
            var publisher = entities.findPublisher(split[0]);
            var extension = entities.findExtension(split[1], publisher);
            var depList = extVersion.getBundledExtensions();
            if (depList == null) {
                depList = new ArrayList<Extension>();
                extVersion.setBundledExtensions(depList);
            }
            depList.add(extension);
        } catch (NoResultException exc) {
            // Ignore the entry
        }
    }

    @POST
    @Path("/{publisher}/{extension}/review")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public ReviewResultJson review(ReviewJson review,
                                   @PathParam("publisher") String publisherName,
                                   @PathParam("extension") String extensionName,
                                   @CookieParam("sessionid") Cookie sessionCookie) {
        try {
            var json = new ReviewResultJson();
            if (sessionCookie == null) {
                json.error = "Not logged in.";
                return json;
            }
            var session = entities.findSession(sessionCookie.getValue());
            if (session == null) {
                json.error = "Invalid session.";
                return json;
            }
            if (review.rating < 0 || review.rating > 5) {
                json.error = "The rating must be an integer number between 0 and 5.";
                return json;
            }
            var extension = entities.findExtension(publisherName, extensionName);
            var extReview = new ExtensionReview();
            extReview.setExtension(extension);
            extReview.setTimestamp(LocalDateTime.now(ZoneId.of("UTC")));
            extReview.setUsername(session.getUser().getName());
            extReview.setTitle(review.title);
            extReview.setComment(review.comment);
            extReview.setRating(review.rating);
            entityManager.persist(extReview);
            extension.setAverageRating(computeAverageRating(extension));
            return json;
        } catch (NoResultException exc) {
            throw new NotFoundException(exc);
        }
    }

    private double computeAverageRating(Extension extension) {
        var reviews = entities.findAllReviews(extension);
        long sum = 0;
        for (var review : reviews) {
            sum += review.getRating();
        }
        return (double) sum / reviews.size();
    }

    protected <T> void handle(AsyncResponse response, Function<IExtensionRegistry, CompletableFuture<T>> serviceCall) {
        handle(getRegistries().iterator(), response, null, serviceCall);
    }

    protected <T> void handle(AsyncResponse response, String mediaType, Function<IExtensionRegistry, CompletableFuture<T>> serviceCall) {
        handle(getRegistries().iterator(), response, mediaType, serviceCall);
    }

    private <T> void handle(Iterator<IExtensionRegistry> registryIterator, AsyncResponse response, String mediaType,
            Function<IExtensionRegistry, CompletableFuture<T>> serviceCall) {
        if (!registryIterator.hasNext())
            response.resume(new NotFoundException());
        var registry = registryIterator.next();
        serviceCall.apply(registry)
                .thenAccept(result -> {
                    if (mediaType == null)
                        response.resume(result);
                    else
                        response.resume(Response.ok(result, mediaType).build());
                })
                .exceptionally(throwable -> {
                    var cause = getCause(throwable);
                    if (cause instanceof NotFoundException)
                        handle(registryIterator, response, mediaType, serviceCall);
                    else
                        response.resume(cause);
                    return null;
                });
    }

    private Throwable getCause(Throwable throwable) {
        if (throwable instanceof CompletionException || throwable instanceof ExecutionException)
            return throwable.getCause();
        else
            return throwable;
    }

}