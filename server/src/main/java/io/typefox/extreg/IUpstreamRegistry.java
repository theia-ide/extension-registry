/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/
package io.typefox.extreg;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.typefox.extreg.json.ExtensionJson;
import io.typefox.extreg.json.PublisherJson;
import io.typefox.extreg.json.ReviewListJson;
import io.typefox.extreg.json.SearchResultJson;

/**
 * Interface for upstream registry to be accessed through the REST client.
 */
@Path("/api")
@RegisterRestClient
public interface IUpstreamRegistry extends IExtensionRegistry {

    @GET
    @Path("/{publisher}")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    PublisherJson getPublisher(@PathParam("publisher") String publisherName);

    @GET
    @Path("/{publisher}/{extension}")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    ExtensionJson getExtension(@PathParam("publisher") String publisherName,
                               @PathParam("extension") String extensionName);

    @GET
    @Path("/{publisher}/{extension}/{version}")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    ExtensionJson getExtension(@PathParam("publisher") String publisherName,
                               @PathParam("extension") String extensionName,
                               @PathParam("version") String version);

    @GET
    @Path("/{publisher}/{extension}/file/{fileName}")
    @Override
    byte[] getFile(@PathParam("publisher") String publisherName,
                   @PathParam("extension") String extensionName,
                   @PathParam("fileName") String fileName);

    @GET
    @Path("/{publisher}/{extension}/{version}/file/{fileName}")
    @Override
    byte[] getFile(@PathParam("publisher") String publisherName,
                   @PathParam("extension") String extensionName,
                   @PathParam("version") String version,
                   @PathParam("fileName") String fileName);

    @GET
    @Path("/{publisher}/{extension}/reviews")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    ReviewListJson getReviews(@PathParam("publisher") String publisherName,
                              @PathParam("extension") String extensionName);

    @GET
    @Path("/-/search")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    SearchResultJson search(@QueryParam("query") String query,
                            @QueryParam("category") String category,
                            @QueryParam("size") @DefaultValue("20") int size,
                            @QueryParam("offset") @DefaultValue("0") int offset);

}