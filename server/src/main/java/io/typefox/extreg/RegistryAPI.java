/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/
package io.typefox.extreg;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.base.Strings;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hibernate.search.mapper.orm.Search;

import io.typefox.extreg.entities.Extension;
import io.typefox.extreg.entities.ExtensionReview;
import io.typefox.extreg.entities.ExtensionVersion;
import io.typefox.extreg.entities.Publisher;
import io.typefox.extreg.json.ExtensionJson;
import io.typefox.extreg.json.ExtensionReferenceJson;
import io.typefox.extreg.json.PublisherJson;
import io.typefox.extreg.json.ReviewJson;
import io.typefox.extreg.json.ReviewListJson;
import io.typefox.extreg.json.ReviewResultJson;
import io.typefox.extreg.json.SearchEntryJson;
import io.typefox.extreg.json.SearchResultJson;
import io.typefox.extreg.util.ErrorResultException;

@Path("/api")
public class RegistryAPI {

    @Inject
    EntityManager entityManager;

    @Inject
    EntityService entities;

    @ConfigProperty(name = "quarkus.http.host")
    String httpHost;

    @GET
    @Path("/{publisher}")
    @Produces(MediaType.APPLICATION_JSON)
    public PublisherJson getPublisher(@PathParam("publisher") String publisherName) {
        try {
            var publisher = entities.findPublisher(publisherName);
            var json = new PublisherJson();
            json.name = publisher.getName();
            json.extensions = new HashMap<>();
            for (var extName : entities.getAllExtensionNames(publisher)) {
                json.extensions.put(extName, createApiUrl(publisher.getName(), extName));
            }
            return json;
        } catch (NoResultException exc) {
            throw new NotFoundException(exc);
        }
    }

    @GET
    @Path("/{publisher}/{extension}/{version}")
    @Produces(MediaType.APPLICATION_JSON)
    public ExtensionJson getExtensionVersion(@PathParam("publisher") String publisherName,
                                             @PathParam("extension") String extensionName,
                                             @PathParam("version") String version) {
        try {
            var extVersion = entities.findVersion(publisherName, extensionName, version);
            var json = new ExtensionJson();
            copyMetadata(json, extVersion, false);
            return json;
        } catch (NoResultException exc) {
            throw new NotFoundException(exc);
        }
    }

    @GET
    @Path("/{publisher}/{extension}")
    @Produces(MediaType.APPLICATION_JSON)
    public ExtensionJson getExtension(@PathParam("publisher") String publisherName,
                                      @PathParam("extension") String extensionName) {
        try {
            var extension = entities.findExtension(publisherName, extensionName);
            var json = new ExtensionJson();
            copyMetadata(json, extension.getLatest(), true);
            return json;
        } catch (NoResultException exc) {
            throw new NotFoundException(exc);
        }
    }

    @GET
    @Path("/{publisher}/{extension}/{version}/file/{fileName}")
    public Response getFile(@PathParam("publisher") String publisherName,
                            @PathParam("extension") String extensionName,
                            @PathParam("version") String version,
                            @PathParam("fileName") String fileName) {
        try {
            var extVersion = entities.findVersion(publisherName, extensionName, version);
            return getFile(extVersion, fileName);
        } catch (NoResultException exc) {
            throw new NotFoundException(exc);
        }
    }

    @GET
    @Path("/{publisher}/{extension}/file/{fileName}")
    public Response getFile(@PathParam("publisher") String publisherName,
                            @PathParam("extension") String extensionName,
                            @PathParam("fileName") String fileName) {
        try {
            var extension = entities.findExtension(publisherName, extensionName);
            var extVersion = extension.getLatest();
            return getFile(extVersion, fileName);
        } catch (NoResultException exc) {
            throw new NotFoundException(exc);
        }
    }

    private Response getFile(ExtensionVersion extVersion, String fileName) {
        if (fileName.equals(extVersion.getExtensionFileName())) {
            var content = entities.findBinary(extVersion).getContent();
            return Response.ok(content, MediaType.APPLICATION_OCTET_STREAM).build();
        }
        if (fileName.equals(extVersion.getReadmeFileName())) {
            var content = entities.findReadme(extVersion).getContent();
            return Response.ok(content, MediaType.TEXT_PLAIN).build();
        }
        if (fileName.equals(extVersion.getIconFileName())) {
            var content = entities.findIcon(extVersion).getContent();
            return Response.ok(content, URLConnection.guessContentTypeFromName(fileName)).build();
        }
        throw new NotFoundException();
    }

    @GET
    @Path("/-/search")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public SearchResultJson search(@QueryParam("query") String query,
                                   @QueryParam("category") String category,
                                   @QueryParam("size") @DefaultValue("20") int size,
                                   @QueryParam("offset") @DefaultValue("0") int offset) {
        var extensions = Search.session(entityManager)
                .search(Extension.class)
                .predicate(f -> Strings.isNullOrEmpty(query)
                                ? f.matchAll()
                                : f.simpleQueryString().field("name").matching(query))
                .fetchHits(offset, size);
        var json = new SearchResultJson();
        json.offset = offset;
        json.extensions = new ArrayList<>();
        for (var extension : extensions) {
            var extVer = extension.getLatest();
            var entry = new SearchEntryJson();
            entry.name = extension.getName();
            entry.publisher = extension.getPublisher().getName();
            entry.extensionUrl = createApiUrl(entry.publisher, entry.name);
            entry.iconUrl = createApiUrl(entry.publisher, entry.name, "file", extVer.getIconFileName());
            entry.version = extVer.getVersion();
            entry.timestamp = extVer.getTimestamp();
            entry.averageRating = extension.getAverageRating();
            entry.displayName = extVer.getDisplayName();
            json.extensions.add(entry);
        }
        return json;
    }

    @POST
    @Path("/-/publish")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public ExtensionJson publish(InputStream content) {
        try {
            var processor = new ExtensionProcessor(content);
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

            var json = new ExtensionJson();
            copyMetadata(json, extVersion, false);
            return json;
        } catch (ErrorResultException | NoResultException exc) {
            return ExtensionJson.error(exc.getMessage());
        }
    }

    @GET
    @Path("/{publisher}/{extension}/reviews")
    @Produces(MediaType.APPLICATION_JSON)
    public ReviewListJson getReviews(@PathParam("publisher") String publisherName,
                                     @PathParam("extension") String extensionName) {
        try {
            var extension = entities.findExtension(publisherName, extensionName);
            var reviews = entities.findAllReviews(extension);
            var list = new ReviewListJson();
            list.postUrl = createApiUrl(extension.getPublisher().getName(), extension.getName(), "review");
            list.reviews = new ArrayList<>();
            for (var extReview : reviews) {
                var json = new ReviewJson();
                json.user = extReview.getUsername();
                json.timestamp = extReview.getTimestamp();
                json.title = extReview.getTitle();
                json.comment = extReview.getComment();
                json.rating = extReview.getRating();
                list.reviews.add(json);
            }
            return list;
        } catch (NoResultException exc) {
            throw new NotFoundException(exc);
        }
    }

    @POST
    @Path("/{publisher}/{extension}/review")
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public ReviewResultJson review(ReviewJson review,
                                   @PathParam("publisher") String publisherName,
                                   @PathParam("extension") String extensionName) {
        try {
            var json = new ReviewResultJson();
            if (review.rating < 0 || review.rating > 5) {
                json.error = "The rating must be an integer number between 0 and 5.";
                return json;
            }
            var extension = entities.findExtension(publisherName, extensionName);
            var extReview = new ExtensionReview();
            extReview.setExtension(extension);
            extReview.setTimestamp(LocalDateTime.now(ZoneId.of("UTC")));
            extReview.setUsername(review.user);
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

    private void copyMetadata(ExtensionJson json, ExtensionVersion extVersion, boolean isLatest) {
        var extension = extVersion.getExtension();
        json.publisher = extension.getPublisher().getName();
        json.name = extension.getName();
        json.publisherUrl = createApiUrl(json.publisher);
        json.reviewsUrl = createApiUrl(json.publisher, json.name, "reviews");
        json.allVersions = new HashMap<>();
        for (var versionStr : entities.getAllVersionStrings(extension)) {
            String url = createApiUrl(json.publisher, json.name, versionStr);
            json.allVersions.put(versionStr, url);
        }
        json.version = extVersion.getVersion();
        json.preview = extVersion.isPreview();
        json.timestamp = extVersion.getTimestamp();
        if (isLatest) {
            json.downloadUrl = createApiUrl(json.publisher, json.name, "file", extVersion.getExtensionFileName());
            json.iconUrl = createApiUrl(json.publisher, json.name, "file", extVersion.getIconFileName());
            json.readmeUrl = createApiUrl(json.publisher, json.name, "file", extVersion.getReadmeFileName());
        } else {
            json.downloadUrl = createApiUrl(json.publisher, json.name, json.version, "file", extVersion.getExtensionFileName());
            json.iconUrl = createApiUrl(json.publisher, json.name, json.version, "file", extVersion.getIconFileName());
            json.readmeUrl = createApiUrl(json.publisher, json.name, json.version, "file", extVersion.getReadmeFileName());
        }
        json.displayName = extVersion.getDisplayName();
        json.description = extVersion.getDescription();
        json.categories = extVersion.getCategories();
        json.keywords = extVersion.getKeywords();
        json.license = extVersion.getLicense();
        json.homepage = extVersion.getHomepage();
        json.repository = extVersion.getRepository();
        json.bugs = extVersion.getBugs();
        json.markdown = extVersion.getMarkdown();
        json.galleryColor = extVersion.getGalleryColor();
        json.galleryTheme = extVersion.getGalleryTheme();
        json.qna = extVersion.getQna();
        if (extVersion.getDependencies() != null) {
            json.dependencies = new ArrayList<>();
            for (var depExtension : extVersion.getDependencies()) {
                var ref = new ExtensionReferenceJson();
                ref.publisher = depExtension.getPublisher().getName();
                ref.extension = depExtension.getName();
                ref.url = createApiUrl(ref.publisher, ref.extension);
                json.dependencies.add(ref);
            }
        }
        if (extVersion.getBundledExtensions() != null) {
            json.bundledExtensions = new ArrayList<>();
            for (var bndExtension : extVersion.getBundledExtensions()) {
                var ref = new ExtensionReferenceJson();
                ref.publisher = bndExtension.getPublisher().getName();
                ref.extension = bndExtension.getName();
                ref.url = createApiUrl(ref.publisher, ref.extension);
                json.bundledExtensions.add(ref);
            }
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

    private double computeAverageRating(Extension extension) {
        var reviews = entities.findAllReviews(extension);
        long sum = 0;
        for (var review : reviews) {
            sum += review.getRating();
        }
        return (double) sum / reviews.size();
    }

    private String createApiUrl(String... segments) {
        try {
            var result = new StringBuilder(httpHost);
            result.append("/api");
            for (var segment : segments) {
                if (segment == null)
                    return null;
				result.append('/').append(URLEncoder.encode(segment, "UTF-8"));
            }
            return result.toString();
        } catch (UnsupportedEncodingException exc) {
            throw new RuntimeException(exc);
        }
    }

}