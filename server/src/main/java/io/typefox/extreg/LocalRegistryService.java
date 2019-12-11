/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/
package io.typefox.extreg;

import static io.typefox.extreg.util.UrlUtil.createApiUrl;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Component;

import io.typefox.extreg.entities.Extension;
import io.typefox.extreg.entities.ExtensionReview;
import io.typefox.extreg.entities.ExtensionVersion;
import io.typefox.extreg.entities.FileResource;
import io.typefox.extreg.entities.Publisher;
import io.typefox.extreg.json.ExtensionJson;
import io.typefox.extreg.json.ExtensionReferenceJson;
import io.typefox.extreg.json.PublisherJson;
import io.typefox.extreg.json.ReviewJson;
import io.typefox.extreg.json.ReviewListJson;
import io.typefox.extreg.json.ReviewResultJson;
import io.typefox.extreg.json.SearchEntryJson;
import io.typefox.extreg.json.SearchResultJson;
import io.typefox.extreg.search.ExtensionSearch;
import io.typefox.extreg.util.CollectionUtil;
import io.typefox.extreg.util.ErrorResultException;
import io.typefox.extreg.util.NotFoundException;

@Component
public class LocalRegistryService implements IExtensionRegistry {
 
    Logger logger = LoggerFactory.getLogger(LocalRegistryService.class);

    @Autowired
    EntityManager entityManager;

    @Autowired
    EntityService entities;

    @Autowired
    ElasticsearchOperations searchOperations;

    @Value("#{environment.OVSX_SERVER_URL}")
    String serverUrl;

    @Override
    public PublisherJson getPublisher(String publisherName) {
        try {
            var publisher = entities.findPublisher(publisherName);
            var json = new PublisherJson();
            json.name = publisher.getName();
            json.extensions = new HashMap<>();
            for (var extName : entities.getAllExtensionNames(publisher)) {
                json.extensions.put(extName, createApiUrl(serverUrl, publisher.getName(), extName));
            }
            return json;
        } catch (NoResultException exc) {
            throw new NotFoundException(exc);
        }
    }

    @Override
    public ExtensionJson getExtension(String publisherName, String extensionName) {
        try {
            var extension = entities.findExtension(publisherName, extensionName);
            ExtensionJson json = toJson(extension.getLatest(), true);
            return json;
        } catch (NoResultException exc) {
            throw new NotFoundException(exc);
        }
    }

    @Override
    public ExtensionJson getExtension(String publisherName, String extensionName, String version) {
        try {
            var extVersion = entities.findVersion(publisherName, extensionName, version);
            ExtensionJson json = toJson(extVersion, false);
            return json;
        } catch (NoResultException exc) {
            throw new NotFoundException(exc);
        }
    }

    @Override
    public byte[] getFile(String publisherName, String extensionName, String fileName) {
        try {
            var extension = entities.findExtension(publisherName, extensionName);
            var extVersion = extension.getLatest();
            var resource = getFile(extVersion, fileName);
            if (resource == null)
                throw new NotFoundException();
            return resource.getContent();
        } catch (NoResultException exc) {
            throw new NotFoundException(exc);
        }
    }

    @Override
    public byte[] getFile(String publisherName, String extensionName, String version, String fileName) {
        try {
            var extVersion = entities.findVersion(publisherName, extensionName, version);
            var resource = getFile(extVersion, fileName);
            if (resource == null)
                throw new NotFoundException();
            return resource.getContent();
        } catch (NoResultException exc) {
            throw new NotFoundException(exc);
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
    public ReviewListJson getReviews(String publisherName, String extensionName) {
        try {
            var extension = entities.findExtension(publisherName, extensionName);
            var reviews = entities.findAllReviews(extension);
            var list = new ReviewListJson();
            list.postUrl = createApiUrl(serverUrl, extension.getPublisher().getName(), extension.getName(), "review");
            list.reviews = new ArrayList<>(reviews.size());
            for (var extReview : reviews) {
                var json = new ReviewJson();
                json.user = extReview.getUsername();
                json.timestamp = extReview.getTimestamp().toString();
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

    @EventListener
    @Transactional
    public void initSearchIndex(ApplicationStartedEvent event) {
        searchOperations.createIndex(ExtensionSearch.class);
        if (event.getApplicationContext().getEnvironment().getProperty("OVSX_INIT_SEARCH_INDEX") != null) {
            logger.info("Initializing search index...");
            var allExtensions = entities.findAllExtensions();
            if (allExtensions.size() > 0) {
                var indexQueries = CollectionUtil.map(allExtensions, extension ->
                    new IndexQueryBuilder()
                        .withObject(toSearch(extension))
                        .build()
                );
                searchOperations.bulkIndex(indexQueries);
            }
        }
    }

    public void updateSearchIndex(Extension extension) {
        var indexQuery = new IndexQueryBuilder()
                .withObject(toSearch(extension))
                .build();
        searchOperations.index(indexQuery);
    }

    @Override
    public SearchResultJson search(String queryString, String category, int size, int offset) {
        var json = new SearchResultJson();
        if (size <= 0) {
            json.extensions = Collections.emptyList();
            return json;
        }

        var pageRequest = PageRequest.of(offset / size, size);
        var searchResult = search(queryString, category, pageRequest);
        json.extensions = toSearchEntries(searchResult, size, offset % size);
        json.offset = offset;
        if (json.extensions.size() < size && searchResult.hasNext()) {
            // This is necessary when offset % size > 0
            var remainder = search(queryString, category, pageRequest.next());
            json.extensions.addAll(toSearchEntries(remainder, size - json.extensions.size(), 0));
        }
        return json;
    }

    private Page<ExtensionSearch> search(String queryString, String category, Pageable pageRequest) {
        var queryBuilder = new NativeSearchQueryBuilder()
                .withIndices("extensions")
                .withPageable(pageRequest);
        if (!Strings.isNullOrEmpty(queryString)) {
            var multiMatchQuery = QueryBuilders.multiMatchQuery(queryString)
                    .field("name").boost(5)
                    .field("displayName").boost(5)
                    .field("tags").boost(3)
                    .field("publisher").boost(2)
                    .field("description")
                    .fuzziness(Fuzziness.AUTO)
                    .prefixLength(2);
            queryBuilder.withQuery(multiMatchQuery);
        }
        if (!Strings.isNullOrEmpty(category)) {
            queryBuilder.withFilter(QueryBuilders.matchPhraseQuery("categories", category));
        }
        return searchOperations.queryForPage(queryBuilder.build(), ExtensionSearch.class);
    }

    private List<SearchEntryJson> toSearchEntries(Page<ExtensionSearch> page, int size, int offset) {
        if (offset > 0 || size < page.getNumberOfElements())
            return CollectionUtil.map(
                    Iterables.limit(Iterables.skip(page.getContent(), offset), size),
                    this::toSearchEntry);
        else
            return CollectionUtil.map(page.getContent(), this::toSearchEntry);
    }

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

            updateSearchIndex(extension.get());
            return toJson(extVersion, false);
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

    @Transactional
    public ReviewResultJson review(ReviewJson review, String publisherName, String extensionName, String sessionId) {
        try {
            var session = entities.findSession(sessionId);
            if (session == null) {
                return ReviewResultJson.error("Invalid session.");
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
            return new ReviewResultJson();
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

    private SearchEntryJson toSearchEntry(ExtensionSearch search) {
        var extension = entityManager.find(Extension.class, search.id);
        var extVer = extension.getLatest();
        var entry = new SearchEntryJson();
        entry.name = extension.getName();
        entry.publisher = extension.getPublisher().getName();
        entry.url = createApiUrl(serverUrl, entry.publisher, entry.name);
        entry.iconUrl = createApiUrl(serverUrl, entry.publisher, entry.name, "file", extVer.getIconFileName());
        entry.version = extVer.getVersion();
        entry.timestamp = extVer.getTimestamp().toString();
        entry.averageRating = extension.getAverageRating();
        entry.displayName = extVer.getDisplayName();
        entry.description = extVer.getDescription();
        entry.downloadUrl = createApiUrl(serverUrl, entry.publisher, entry.name, "file", extVer.getExtensionFileName());
        return entry;
    }

    private ExtensionSearch toSearch(Extension extension) {
        var search = new ExtensionSearch();
        var extVer = extension.getLatest();
        search.id = extension.getId();
        search.name = extension.getName();
        search.publisher = extension.getPublisher().getName();
        search.displayName = extVer.getDisplayName();
        search.description = extVer.getDescription();
        search.categories = extVer.getCategories();
        search.tags = extVer.getTags();
        return search;
    }

    public ExtensionJson toJson(ExtensionVersion extVersion, boolean isLatest) {
        var json = new ExtensionJson();
        var extension = extVersion.getExtension();
        json.publisher = extension.getPublisher().getName();
        json.name = extension.getName();
        json.averageRating = extension.getAverageRating();
        json.reviewCount = entities.countReviews(extension);
        json.publisherUrl = createApiUrl(serverUrl, json.publisher);
        json.reviewsUrl = createApiUrl(serverUrl, json.publisher, json.name, "reviews");
        json.allVersions = new HashMap<>();
        for (var versionStr : entities.getAllVersionStrings(extension)) {
            String url = createApiUrl(serverUrl, json.publisher, json.name, versionStr);
            json.allVersions.put(versionStr, url);
        }
        json.version = extVersion.getVersion();
        json.preview = extVersion.isPreview();
        json.timestamp = extVersion.getTimestamp().toString();
        if (isLatest) {
            json.downloadUrl = createApiUrl(serverUrl, json.publisher, json.name, "file", extVersion.getExtensionFileName());
            json.iconUrl = createApiUrl(serverUrl, json.publisher, json.name, "file", extVersion.getIconFileName());
            json.readmeUrl = createApiUrl(serverUrl, json.publisher, json.name, "file", extVersion.getReadmeFileName());
        } else {
            json.downloadUrl = createApiUrl(serverUrl, json.publisher, json.name, json.version, "file", extVersion.getExtensionFileName());
            json.iconUrl = createApiUrl(serverUrl, json.publisher, json.name, json.version, "file", extVersion.getIconFileName());
            json.readmeUrl = createApiUrl(serverUrl, json.publisher, json.name, json.version, "file", extVersion.getReadmeFileName());
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
                ref.url = createApiUrl(serverUrl, ref.publisher, ref.extension);
                json.dependencies.add(ref);
            }
        }
        if (extVersion.getBundledExtensions() != null) {
            json.bundledExtensions = new ArrayList<>(extVersion.getBundledExtensions().size());
            for (var bndExtension : extVersion.getBundledExtensions()) {
                var ref = new ExtensionReferenceJson();
                ref.publisher = bndExtension.getPublisher().getName();
                ref.extension = bndExtension.getName();
                ref.url = createApiUrl(serverUrl, ref.publisher, ref.extension);
                json.bundledExtensions.add(ref);
            }
        }
        return json;
    }

}