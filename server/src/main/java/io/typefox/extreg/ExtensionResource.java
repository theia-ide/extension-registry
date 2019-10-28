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
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/extension")
@Produces(MediaType.APPLICATION_JSON)
public class ExtensionResource {

    @Inject
    EntityManager entityManager;

    @GET
    @Path("/{name}/{version}")
    @Transactional
    public Extension getExtension(@PathParam("name") String name, @PathParam("version") String version) {
        String key = name + "@" + version;
        var extension = entityManager.find(Extension.class, key);
        if (extension == null)
            throw new NotFoundException();
        return extension;
    }

}