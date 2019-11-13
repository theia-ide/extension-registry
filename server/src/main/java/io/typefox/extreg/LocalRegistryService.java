/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/
package io.typefox.extreg;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.failedFuture;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;

import com.google.common.base.Strings;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hibernate.search.mapper.orm.Search;

import io.typefox.extreg.entities.Extension;
import io.typefox.extreg.entities.ExtensionVersion;
import io.typefox.extreg.entities.FileResource;
import io.typefox.extreg.json.ExtensionJson;
import io.typefox.extreg.json.ExtensionReferenceJson;
import io.typefox.extreg.json.PublisherJson;
import io.typefox.extreg.json.ReviewJson;
import io.typefox.extreg.json.ReviewListJson;
import io.typefox.extreg.json.SearchEntryJson;
import io.typefox.extreg.json.SearchResultJson;

@ApplicationScoped
public class LocalRegistryService implements IExtensionRegistry {

    @Inject
    EntityManager entityManager;

    @Inject
    EntityService entities;

    @ConfigProperty(name = "quarkus.http.host")
    String httpHost;

    @Override
    public CompletableFuture<PublisherJson> getPublisher(String publisherName) {
        try {
            var publisher = entities.findPublisher(publisherName);
            var json = new PublisherJson();
            json.name = publisher.getName();
            json.extensions = new HashMap<>();
            for (var extName : entities.getAllExtensionNames(publisher)) {
                json.extensions.put(extName, createApiUrl(publisher.getName(), extName));
            }
            return completedFuture(json);
        } catch (NoResultException exc) {
            return failedFuture(new NotFoundException(exc));
        } catch (Exception exc) {
            return failedFuture(exc);
        }
    }

    @Override
    public CompletableFuture<ExtensionJson> getExtension(String publisherName, String extensionName) {
        try {
            var extension = entities.findExtension(publisherName, extensionName);
            ExtensionJson json = toJson(extension.getLatest(), true);
            return completedFuture(json);
        } catch (NoResultException exc) {
            return failedFuture(new NotFoundException(exc));
        } catch (Exception exc) {
            return failedFuture(exc);
        }
    }

    @Override
    public CompletableFuture<ExtensionJson> getExtensionVersion(String publisherName, String extensionName,
            String version) {
        try {
            var extVersion = entities.findVersion(publisherName, extensionName, version);
            ExtensionJson json = toJson(extVersion, false);
            return completedFuture(json);
        } catch (NoResultException exc) {
            return failedFuture(new NotFoundException(exc));
        } catch (Exception exc) {
            return failedFuture(exc);
        }
    }

    @Override
    public CompletableFuture<byte[]> getFile(String publisherName, String extensionName, String fileName) {
        try {
            var extension = entities.findExtension(publisherName, extensionName);
            var extVersion = extension.getLatest();
            var resource = getFile(extVersion, fileName);
            if (resource == null)
                return failedFuture(new NotFoundException());
            return completedFuture(resource.getContent());
        } catch (NoResultException exc) {
            return failedFuture(new NotFoundException(exc));
        } catch (Exception exc) {
            return failedFuture(exc);
        }
    }

    @Override
    public CompletableFuture<byte[]> getFile(String publisherName, String extensionName, String version,
            String fileName) {
        try {
            var extVersion = entities.findVersion(publisherName, extensionName, version);
            var resource = getFile(extVersion, fileName);
            if (resource == null)
                return failedFuture(new NotFoundException());
            return completedFuture(resource.getContent());
        } catch (NoResultException exc) {
            return failedFuture(new NotFoundException(exc));
        } catch (Exception exc) {
            return failedFuture(exc);
        }
    }

    private FileResource getFile(ExtensionVersion extVersion, String fileName) {
        if (fileName.equals(extVersion.getExtensionFileName()))
            return entities.findBinary(extVersion);
        if (fileName.equals(extVersion.getReadmeFileName()))
            return entities.findReadme(extVersion);
        if (fileName.equals(extVersion.getIconFileName()))
            return entities.findIcon(extVersion);
        return null;
    }

    @Override
    public CompletableFuture<ReviewListJson> getReviews(String publisherName, String extensionName) {
        try {
            var extension = entities.findExtension(publisherName, extensionName);
            var reviews = entities.findAllReviews(extension);
            var list = new ReviewListJson();
            list.postUrl = createApiUrl(extension.getPublisher().getName(), extension.getName(), "review");
            list.reviews = new ArrayList<>(reviews.size());
            for (var extReview : reviews) {
                var json = new ReviewJson();
                json.user = extReview.getUsername();
                json.timestamp = extReview.getTimestamp();
                json.title = extReview.getTitle();
                json.comment = extReview.getComment();
                json.rating = extReview.getRating();
                list.reviews.add(json);
            }
            return completedFuture(list);
        } catch (NoResultException exc) {
            return failedFuture(new NotFoundException(exc));
        } catch (Exception exc) {
            return failedFuture(exc);
        }
    }

    @Override
    @Transactional
    public CompletableFuture<SearchResultJson> search(String query, String category, int size, int offset) {
        try {
            var searchResult = Search.session(entityManager)
                .search(Extension.class)
                .predicate(spf -> {
                    if (Strings.isNullOrEmpty(query) && Strings.isNullOrEmpty(category))
                        return spf.matchAll();
                    var bool = spf.bool();
                    if (!Strings.isNullOrEmpty(category))
                        bool = bool.must(spf.match()
                                .field("latest.categories")
                                .matching(category));
                    if (!Strings.isNullOrEmpty(query))
                        bool = bool.must(spf.simpleQueryString()
                            .fields("name", "latest.displayName").boost(5)
                            .fields("publisher.name", "latest.tags").boost(2)
                            .fields("latest.description")
                            .matching(query));
                    return bool;
                })
                .fetch(offset, size);
            var json = new SearchResultJson();
            json.extensions = toSearchEntries(searchResult.getHits());
            json.offset = (int) Math.min(offset, searchResult.getTotalHitCount());
            return completedFuture(json);
        } catch (Exception exc) {
            return failedFuture(exc);
        }
    }

    private List<SearchEntryJson> toSearchEntries(List<Extension> extensions) {
        var list = new ArrayList<SearchEntryJson>(extensions.size());
        for (var extension : extensions) {
            var extVer = extension.getLatest();
            var entry = new SearchEntryJson();
            entry.name = extension.getName();
            entry.publisher = extension.getPublisher().getName();
            entry.url = createApiUrl(entry.publisher, entry.name);
            entry.iconUrl = createApiUrl(entry.publisher, entry.name, "file", extVer.getIconFileName());
            entry.version = extVer.getVersion();
            entry.timestamp = extVer.getTimestamp();
            entry.averageRating = extension.getAverageRating();
            entry.displayName = extVer.getDisplayName();
            entry.description = extVer.getDescription();
            entry.downloadUrl = createApiUrl(entry.publisher, entry.name, "file", extVer.getExtensionFileName());
            list.add(entry);
        }
        return list;
    }

    public ExtensionJson toJson(ExtensionVersion extVersion, boolean isLatest) {
        var json = new ExtensionJson();
        var extension = extVersion.getExtension();
        json.publisher = extension.getPublisher().getName();
        json.name = extension.getName();
        json.averageRating = extension.getAverageRating();
        json.reviewCount = entities.countReviews(extension);
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
        json.tags = extVersion.getTags();
        json.license = extVersion.getLicense();
        json.homepage = extVersion.getHomepage();
        json.repository = extVersion.getRepository();
        json.bugs = extVersion.getBugs();
        json.markdown = extVersion.getMarkdown();
        json.galleryColor = extVersion.getGalleryColor();
        json.galleryTheme = extVersion.getGalleryTheme();
        json.qna = extVersion.getQna();
        if (extVersion.getDependencies() != null) {
            json.dependencies = new ArrayList<>(extVersion.getDependencies().size());
            for (var depExtension : extVersion.getDependencies()) {
                var ref = new ExtensionReferenceJson();
                ref.publisher = depExtension.getPublisher().getName();
                ref.extension = depExtension.getName();
                ref.url = createApiUrl(ref.publisher, ref.extension);
                json.dependencies.add(ref);
            }
        }
        if (extVersion.getBundledExtensions() != null) {
            json.bundledExtensions = new ArrayList<>(extVersion.getBundledExtensions().size());
            for (var bndExtension : extVersion.getBundledExtensions()) {
                var ref = new ExtensionReferenceJson();
                ref.publisher = bndExtension.getPublisher().getName();
                ref.extension = bndExtension.getName();
                ref.url = createApiUrl(ref.publisher, ref.extension);
                json.bundledExtensions.add(ref);
            }
        }
        return json;
    }

    /**
     * Create a URL pointing to an API path.
     */
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
            throw new WebApplicationException(exc);
        }
    }

}