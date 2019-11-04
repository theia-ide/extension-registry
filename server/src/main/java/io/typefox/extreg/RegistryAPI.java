/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/
package io.typefox.extreg;

import java.util.List;

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

import org.hibernate.Hibernate;
import org.hibernate.engine.spi.SessionImplementor;

import io.typefox.extreg.entities.Extension;
import io.typefox.extreg.entities.ExtensionVersion;
import io.typefox.extreg.entities.Publisher;
import io.typefox.extreg.json.ExtensionInfo;
import io.typefox.extreg.json.PublisherInfo;
import io.typefox.extreg.json.SearchResult;
import io.typefox.extreg.util.ErrorResultException;

@Path("/api")
public class RegistryAPI {

    @Inject
    private EntityManager entityManager;

    @GET
    @Path("{publisher}")
    @Produces(MediaType.APPLICATION_JSON)
    public PublisherInfo getPublisher(@PathParam("publisher") String publisherName) {
        try {
            var json = new PublisherInfo();
            var publisher = findPublisher(publisherName);
            json.name = publisher.name;
            json.extensions = findExtensionNames(publisher);
            return json;
        } catch (NoResultException exc) {
            throw new NotFoundException(exc);
        }
    }

    @GET
    @Path("{publisher}/{extension}")
    @Produces(MediaType.APPLICATION_JSON)
    public ExtensionInfo getExtension(@PathParam("publisher") String publisherName,
            @PathParam("extension") String extensionName) {
        try {
            var json = new ExtensionInfo();
            var publisher = findPublisher(publisherName);
            json.publisher = publisher.name;
            var extension = findExtension(extensionName, publisher);
            json.name = extension.name;
            copyMetadata(json, extension.latest);
            return json;
        } catch (NoResultException exc) {
            throw new NotFoundException(exc);
        }
    }

    @GET
    @Path("{publisher}/{extension}/{version}")
    @Produces(MediaType.APPLICATION_JSON)
    public ExtensionInfo getExtensionVersion(@PathParam("publisher") String publisherName,
            @PathParam("extension") String extensionName, @PathParam("version") String version) {
        try {
            var json = new ExtensionInfo();
            var publisher = findPublisher(publisherName);
            json.publisher = publisher.name;
            var extension = findExtension(extensionName, publisher);
            json.name = extension.name;
            var extVersion = findVersion(version, extension);
            copyMetadata(json, extVersion);
            return json;
        } catch (NoResultException exc) {
            throw new NotFoundException(exc);
        }
    }

    @GET
    @Path("-/search")
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResult search(@QueryParam("query") String query) {
        var json = new SearchResult();
        // TODO
        return json;
    }

    @POST
    @Path("-/publish")
    @Transactional
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_JSON)
    public ExtensionInfo publish(byte[] content) {
        try {
            var processor = new ExtensionProcessor(content);
            var json = new ExtensionInfo();
            var publisher = findPublisher(processor.getPublisherName());
            json.publisher = publisher.name;
            var extension = findExtension(processor.getExtensionName(), publisher);
            json.name = extension.name;
            var extVersion = processor.getMetadata();
            checkUniqueVersion(extVersion.version, extension);
            // TODO dependencies, bundledExtensions
            copyMetadata(json, extVersion);

            var lobCreator = Hibernate.getLobCreator((SessionImplementor) entityManager);
            entityManager.persist(extVersion);
            var binary = processor.getBinary(extVersion, lobCreator);
            entityManager.persist(binary);
            var readme = processor.getReadme(extVersion, lobCreator);
            if (readme != null)
                entityManager.persist(readme);
            var icon = processor.getIcon(extVersion, lobCreator);
            if (icon != null)
                entityManager.persist(icon);

            return json;
        } catch (ErrorResultException | NoResultException exc) {
            return ExtensionInfo.error(exc.getMessage());
        }
    }

    private Publisher findPublisher(String name) {
        var qs = "SELECT pub FROM Publisher pub WHERE (pub.name = :name)";
        var query = entityManager.createQuery(qs, Publisher.class);
        query.setParameter("name", name);
        return query.getSingleResult();
    }

    private Extension findExtension(String name, Publisher publisher) {
        var qs = "SELECT ext FROM Extension ext WHERE (ext.publisher = :publisher and ext.name = :name)";
        var query = entityManager.createQuery(qs, Extension.class);
        query.setParameter("publisher", publisher.id);
        query.setParameter("name", name);
        return query.getSingleResult();
    }

    private List<String> findExtensionNames(Publisher publisher) {
        var qs = "SELECT ext.name FROM Extension ext WHERE (ext.publisher = :publisher)";
        var query = entityManager.createQuery(qs, String.class);
        query.setParameter("publisher", publisher.id);
        return query.getResultList();
    }

    private ExtensionVersion findVersion(String version, Extension extension) {
        var qs = "SELECT ver FROM ExtensionVersion ver WHERE (ver.extension = :extension and ext.version = :version)";
        var query = entityManager.createQuery(qs, ExtensionVersion.class);
        query.setParameter("extension", extension.id);
        query.setParameter("version", version);
        return query.getSingleResult();
    }

    private void copyMetadata(ExtensionInfo json, ExtensionVersion extVersion) {
        json.version = extVersion.version;
        json.preview = extVersion.preview;
        json.timestamp = extVersion.timestamp;
        json.displayName = extVersion.displayName;
        json.description = extVersion.description;
        json.categories = extVersion.categories;
        json.keywords = extVersion.keywords;
        json.license = extVersion.license;
        json.homepage = extVersion.homepage;
        json.repository = extVersion.repository;
        json.bugs = extVersion.bugs;
        json.markdown = extVersion.markdown;
        json.galleryColor = extVersion.galleryColor;
        json.galleryTheme = extVersion.galleryTheme;
        json.qna = extVersion.qna;
        // TODO dependencies, bundledExtensions
    }

    private void checkUniqueVersion(String version, Extension extension) {
        var qs = "SELECT count(*) FROM ExtensionVersion ver WHERE (ver.extension = :extension and ext.version = :version)";
        var query = entityManager.createQuery(qs, Long.class);
        query.setParameter("extension", extension.id);
        query.setParameter("version", version);
        if (query.getSingleResult() > 0)
            throw new ErrorResultException("Extension " + extension.name + " version " + version + " is already published.");
    }

}