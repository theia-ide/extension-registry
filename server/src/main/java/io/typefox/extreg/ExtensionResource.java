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
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.typefox.extreg.entities.Extension;

@Path("/api/extension")
@Produces(MediaType.APPLICATION_JSON)
public class ExtensionResource {

    @Inject
    EntityManager entityManager;

    @GET
    @Path("/{name}/{version}")
    @Transactional
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