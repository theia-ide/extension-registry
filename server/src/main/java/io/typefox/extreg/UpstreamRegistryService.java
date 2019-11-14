/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/
package io.typefox.extreg;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import com.google.common.base.Strings;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.typefox.extreg.json.ExtensionJson;
import io.typefox.extreg.json.PublisherJson;
import io.typefox.extreg.json.ReviewListJson;
import io.typefox.extreg.json.SearchResultJson;

@ApplicationScoped
public class UpstreamRegistryService implements IExtensionRegistry {

    @ConfigProperty(name = "extreg.upstream.url", defaultValue = "")
    String upstreamUrl;

    private WebTarget target;

    private WebTarget getTarget() {
        if (Strings.isNullOrEmpty(upstreamUrl))
            throw new NotFoundException();
        if (target == null) {
            Client client = ClientBuilder.newClient();
            client.property("jersey.config.client.connectTimeout", 10_000);
            client.property("jersey.config.client.readTimeout", 30_000);
            target = client.target(upstreamUrl).path("/api");
        }
        return target;
    }

    @Override
    public PublisherJson getPublisher(String publisherName) {
        return getTarget().path("/{publisher}")
                .resolveTemplate("publisher", publisherName)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get(PublisherJson.class);
    }

    @Override
    public ExtensionJson getExtension(String publisherName, String extensionName) {
        var target = getTarget().path("/{publisher}/{extension}")
                .resolveTemplate("publisher", publisherName)
                .resolveTemplate("extension", extensionName);
        System.out.println(target.getUri());
        return target.request()
                .accept(MediaType.APPLICATION_JSON)
                .get(ExtensionJson.class);
    }

    @Override
    public ExtensionJson getExtension(String publisherName,
            String extensionName, String version) {
        return getTarget().path("/{publisher}/{extension}/{version}")
                .resolveTemplate("publisher", publisherName)
                .resolveTemplate("extension", extensionName)
                .resolveTemplate("version", version)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get(ExtensionJson.class);
    }

    @Override
    public byte[] getFile(String publisherName, String extensionName, String fileName) {
        return getTarget().path("/{publisher}/{extension}/file/{fileName}")
                .resolveTemplate("publisher", publisherName)
                .resolveTemplate("extension", extensionName)
                .resolveTemplate("fileName", fileName)
                .request()
                .get(byte[].class);
    }

    @Override
    public byte[] getFile(String publisherName, String extensionName, String version, String fileName) {
        return getTarget().path("/{publisher}/{extension}/{version}/file/{fileName}")
                .resolveTemplate("publisher", publisherName)
                .resolveTemplate("extension", extensionName)
                .resolveTemplate("version", version)
                .resolveTemplate("fileName", fileName)
                .request()
                .get(byte[].class);
    }

    @Override
    public ReviewListJson getReviews(String publisherName, String extensionName) {
        return getTarget().path("/{publisher}/{extension}/reviews")
                .resolveTemplate("publisher", publisherName)
                .resolveTemplate("extension", extensionName)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get(ReviewListJson.class);
	}

    @Override
    public SearchResultJson search(String query, String category, int size, int offset) {
        return getTarget().path("/-/search")
                .queryParam("query", query)
                .queryParam("category", category)
                .queryParam("size", size)
                .queryParam("offset", offset)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get(SearchResultJson.class);
    }

}