/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/
package io.typefox.extreg;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.typefox.extreg.json.ExtensionJson;
import io.typefox.extreg.json.PublisherJson;
import io.typefox.extreg.json.ReviewListJson;
import io.typefox.extreg.json.SearchResultJson;

/**
 * Declaration of the registry API methods that can be accessed without authentication.
 */
@Path("/api")
@RegisterRestClient
public interface IExtensionRegistry {

    @GET
    @Path("/{publisher}")
    @Produces(MediaType.APPLICATION_JSON)
    PublisherJson getPublisher(String publisherName);

    @GET
    @Path("/{publisher}/{extension}")
    @Produces(MediaType.APPLICATION_JSON)
    ExtensionJson getExtension(String publisherName, String extensionName);

    @GET
    @Path("/{publisher}/{extension}/{version}")
    @Produces(MediaType.APPLICATION_JSON)
    ExtensionJson getExtension(String publisherName, String extensionName, String version);

    @GET
    @Path("/{publisher}/{extension}/file/{fileName}")
    byte[] getFile(String publisherName, String extensionName, String fileName);

    @GET
    @Path("/{publisher}/{extension}/{version}/file/{fileName}")
    byte[] getFile(String publisherName, String extensionName, String version, String fileName);

    @GET
    @Path("/{publisher}/{extension}/reviews")
    @Produces(MediaType.APPLICATION_JSON)
    ReviewListJson getReviews(String publisherName, String extensionName);

    @GET
    @Path("/-/search")
    @Produces(MediaType.APPLICATION_JSON)
    SearchResultJson search(String query, String category, int size, int offset);

}