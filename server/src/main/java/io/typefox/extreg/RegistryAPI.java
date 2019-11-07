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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.typefox.extreg.entities.Extension;
import io.typefox.extreg.entities.ExtensionReview;
import io.typefox.extreg.entities.ExtensionVersion;
import io.typefox.extreg.entities.Publisher;
import io.typefox.extreg.json.ExtensionInfo;
import io.typefox.extreg.json.ExtensionReference;
import io.typefox.extreg.json.PublisherInfo;
import io.typefox.extreg.json.Review;
import io.typefox.extreg.json.SearchResult;
import io.typefox.extreg.util.ErrorResultException;

@Path("/api")
public class RegistryAPI {

    @Inject
    EntityManager entityManager;

    @Inject
    EntityService entities;

    @GET
    @Path("/{publisher}")
    @Produces(MediaType.APPLICATION_JSON)
    public PublisherInfo getPublisher(@PathParam("publisher") String publisherName) {
        try {
            var publisher = entities.findPublisher(publisherName);
            var json = new PublisherInfo();
            json.name = publisher.getName();
            json.extensions = entities.getAllExtensionNames(publisher);
            return json;
        } catch (NoResultException exc) {
            throw new NotFoundException(exc);
        }
    }

    @GET
    @Path("/{publisher}/{extension}")
    @Produces(MediaType.APPLICATION_JSON)
    public ExtensionInfo getExtension(@PathParam("publisher") String publisherName,
                                      @PathParam("extension") String extensionName) {
        try {
            var extension = entities.findExtension(publisherName, extensionName);
            var json = new ExtensionInfo();
            copyMetadata(json, extension.getLatest());
            return json;
        } catch (NoResultException exc) {
            throw new NotFoundException(exc);
        }
    }

    @GET
    @Path("/{publisher}/{extension}/{version}")
    @Produces(MediaType.APPLICATION_JSON)
    public ExtensionInfo getExtensionVersion(@PathParam("publisher") String publisherName,
                                             @PathParam("extension") String extensionName,
                                             @PathParam("version") String version) {
        try {
            var extVersion = entities.findVersion(publisherName, extensionName, version);
            var json = new ExtensionInfo();
            copyMetadata(json, extVersion);
            return json;
        } catch (NoResultException exc) {
            throw new NotFoundException(exc);
        }
    }

    @GET
    @Path("/{publisher}/{extension}/{version}/file/{download}")
    public Response downloadExtension(@PathParam("publisher") String publisherName,
                                    @PathParam("extension") String extensionName,
                                    @PathParam("version") String version,
                                    @PathParam("download") String download) {
        try {
            var extVersion = entities.findVersion(publisherName, extensionName, version);
            return getFile(extVersion, download);
        } catch (NoResultException exc) {
            throw new NotFoundException(exc);
        }
    }

    @GET
    @Path("/{publisher}/{extension}/file/{download}")
    public Response downloadExtension(@PathParam("publisher") String publisherName,
                                    @PathParam("extension") String extensionName,
                                    @PathParam("download") String download) {
        try {
            var extension = entities.findExtension(publisherName, extensionName);
            var extVersion = extension.getLatest();
            return getFile(extVersion, download);
        } catch (NoResultException exc) {
            throw new NotFoundException(exc);
        }
    }

    private Response getFile(ExtensionVersion extVersion, String download) {
        if (download.equals(extVersion.getExtensionFileName())) {
            var content = entities.findBinary(extVersion).getContent();
            return Response.ok(content, MediaType.APPLICATION_OCTET_STREAM).build();
        }
        if (download.equals(extVersion.getReadmeFileName())) {
            var content = entities.findReadme(extVersion).getContent();
            return Response.ok(content, MediaType.TEXT_PLAIN).build();
        }
        if (download.equals(extVersion.getIconFileName())) {
            var content = entities.findIcon(extVersion).getContent();
            return Response.ok(content, URLConnection.guessContentTypeFromName(download)).build();
        }
        throw new NotFoundException();
    }

    @GET
    @Path("/-/search")
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResult search(@QueryParam("query") String query) {
        var json = new SearchResult();
        // TODO
        return json;
    }

    @POST
    @Path("/-/publish")
    @Transactional
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_JSON)
    public ExtensionInfo publish(InputStream content) {
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

            var json = new ExtensionInfo();
            copyMetadata(json, extVersion);
            return json;
        } catch (ErrorResultException | NoResultException exc) {
            return ExtensionInfo.error(exc.getMessage());
        }
    }

    @GET
    @Path("/{publisher}/{extension}/reviews")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Review> getReviews(@PathParam("publisher") String publisherName,
                                   @PathParam("extension") String extensionName) {
        try {
            var reviews = entities.findAllReviews(publisherName, extensionName);
            var array = new ArrayList<Review>();
            for (ExtensionReview extReview : reviews) {
                var json = new Review();
                json.user = extReview.getUsername();
                json.title = extReview.getTitle();
                json.comment = extReview.getComment();
                json.rating = extReview.getRating();
                array.add(json);
            }
            return array;
        } catch (NoResultException exc) {
            throw new NotFoundException(exc);
        }
    }

    @POST
    @Path("/{publisher}/{extension}/review")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    public void review(Review review,
                       @PathParam("publisher") String publisherName,
                       @PathParam("extension") String extensionName) {
        try {
            var extension = entities.findExtension(publisherName, extensionName);
            var extReview = new ExtensionReview();
            extReview.setExtension(extension);
            extReview.setUsername(review.user);
            extReview.setTitle(review.title);
            extReview.setComment(review.comment);
            extReview.setRating(review.rating);
            entityManager.persist(extReview);
        } catch (NoResultException exc) {
            throw new NotFoundException(exc);
        }
    }

    private void copyMetadata(ExtensionInfo json, ExtensionVersion extVersion) {
        var extension = extVersion.getExtension();
        json.publisher = extension.getPublisher().getName();
        json.name = extension.getName();
        json.allVersions = entities.getAllVersionStrings(extension);
        json.version = extVersion.getVersion();
        json.preview = extVersion.isPreview();
        json.timestamp = extVersion.getTimestamp();
        json.extensionFileName = extVersion.getExtensionFileName();
        json.iconFileName = extVersion.getIconFileName();
        json.readmeFileName = extVersion.getReadmeFileName();
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
            for (Extension depExtension : extVersion.getDependencies()) {
                var ref = new ExtensionReference();
                ref.publisher = depExtension.getPublisher().getName();
                ref.extension = depExtension.getName();
                json.dependencies.add(ref);
            }
        }
        if (extVersion.getBundledExtensions() != null) {
            json.bundledExtensions = new ArrayList<>();
            for (Extension bndExtension : extVersion.getBundledExtensions()) {
                var ref = new ExtensionReference();
                ref.publisher = bndExtension.getPublisher().getName();
                ref.extension = bndExtension.getName();
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

}