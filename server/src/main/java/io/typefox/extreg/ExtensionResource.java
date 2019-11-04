/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/
package io.typefox.extreg;

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
import javax.ws.rs.core.MediaType;

import org.hibernate.Hibernate;
import org.hibernate.engine.spi.SessionImplementor;

import io.typefox.extreg.entities.Extension;
import io.typefox.extreg.entities.ExtensionVersion;
import io.typefox.extreg.upload.ExtensionProcessor;

@Path("/api/extension")
public class ExtensionResource {

    @Inject
    private EntityManager entityManager;

    @POST
    @Transactional
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_JSON)
    public ExtensionVersion uploadExtension(byte[] content) {
        var processor = new ExtensionProcessor(content);
        // TODO create extension if it does not exist yet, associate the version with it
        var version = processor.getMetadata();
        entityManager.persist(version);
        var lobCreator = Hibernate.getLobCreator((SessionImplementor) entityManager);
        var binary = processor.getBinary(version, lobCreator);
        entityManager.persist(binary);
        return version;
    }

    @GET
    @Path("/{name}/{version}")
    @Produces(MediaType.APPLICATION_JSON)
    public Extension getExtension(@PathParam("name") String name, @PathParam("version") String version) {
        try {
            var qs = "SELECT ext from Extension ext where (ext.name = :name and ext.version = :version)";
            var query = entityManager.createQuery(qs, Extension.class);
            query.setParameter("name", name);
            query.setParameter("version", version);
            return query.getSingleResult();
        } catch (NoResultException exc) {
            throw new NotFoundException(exc);
        }
    }

}