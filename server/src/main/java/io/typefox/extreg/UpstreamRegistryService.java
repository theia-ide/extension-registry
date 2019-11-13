/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/
package io.typefox.extreg;

import static java.util.concurrent.CompletableFuture.failedFuture;

import java.util.concurrent.CompletableFuture;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import com.google.common.base.Strings;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.typefox.extreg.json.ExtensionJson;
import io.typefox.extreg.json.PublisherJson;
import io.typefox.extreg.json.ReviewListJson;
import io.typefox.extreg.json.SearchResultJson;
import io.typefox.extreg.util.CompletableFutureCallback;

@ApplicationScoped
public class UpstreamRegistryService implements IExtensionRegistry {

    @ConfigProperty(name = "extreg.upstream.url", defaultValue = "")
    String upstreamUrl;

    private WebTarget target;

    private WebTarget getTarget() {
        if (Strings.isNullOrEmpty(upstreamUrl))
            return null;
        if (target == null)
            target = ClientBuilder.newClient().target(upstreamUrl);
        return target;
    }

    @Override
    public CompletableFuture<PublisherJson> getPublisher(String publisherName) {
        var target = getTarget();
        if (target == null)
            return failedFuture(new NotFoundException());
        var result = new CompletableFuture<PublisherJson>();
        target.path("/api/{publisher}")
                .resolveTemplate("publisher", publisherName)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .async()
                .get(new CompletableFutureCallback<>(result));
        return result;
    }

    @Override
    public CompletableFuture<ExtensionJson> getExtension(String publisherName, String extensionName) {
        var target = getTarget();
        if (target == null)
            return failedFuture(new NotFoundException());
        var result = new CompletableFuture<ExtensionJson>();
        target.path("/api/{publisher}/{extension}")
                .resolveTemplate("publisher", publisherName)
                .resolveTemplate("extension", extensionName)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .async()
                .get(new CompletableFutureCallback<>(result));
        return result;
    }

    @Override
    public CompletableFuture<ExtensionJson> getExtensionVersion(String publisherName,
            String extensionName, String version) {
        var target = getTarget();
        if (target == null)
            return failedFuture(new NotFoundException());
        var result = new CompletableFuture<ExtensionJson>();
        target.path("/api/{publisher}/{extension}/{version}")
                .resolveTemplate("publisher", publisherName)
                .resolveTemplate("extension", extensionName)
                .resolveTemplate("version", version)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .async()
                .get(new CompletableFutureCallback<>(result));
        return result;
    }

    @Override
    public CompletableFuture<byte[]> getFile(String publisherName, String extensionName, String fileName) {
        var target = getTarget();
        if (target == null)
            return failedFuture(new NotFoundException());
        var result = new CompletableFuture<byte[]>();
        target.path("/api/{publisher}/{extension}/file/{fileName}")
                .resolveTemplate("publisher", publisherName)
                .resolveTemplate("extension", extensionName)
                .resolveTemplate("fileName", fileName)
                .request()
                .async()
                .get(new CompletableFutureCallback<>(result));
        return result;
    }

    @Override
    public CompletableFuture<byte[]> getFile(String publisherName, String extensionName, String version,
            String fileName) {
        var target = getTarget();
        if (target == null)
            return failedFuture(new NotFoundException());
        var result = new CompletableFuture<byte[]>();
        target.path("/api/{publisher}/{extension}/{version}/file/{fileName}")
                .resolveTemplate("publisher", publisherName)
                .resolveTemplate("extension", extensionName)
                .resolveTemplate("version", version)
                .resolveTemplate("fileName", fileName)
                .request()
                .async()
                .get(new CompletableFutureCallback<>(result));
        return result;
    }

    @Override
    public CompletableFuture<ReviewListJson> getReviews(String publisherName, String extensionName) {
        var target = getTarget();
        if (target == null)
            return failedFuture(new NotFoundException());
        var result = new CompletableFuture<ReviewListJson>();
        target.path("/api/{publisher}/{extension}/reviews")
                .resolveTemplate("publisher", publisherName)
                .resolveTemplate("extension", extensionName)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .async()
                .get(new CompletableFutureCallback<>(result));
        return result;
	}

    @Override
    public CompletableFuture<SearchResultJson> search(String query, String category, int size, int offset) {
        var target = getTarget();
        if (target == null)
            return failedFuture(new NotFoundException());
        var result = new CompletableFuture<SearchResultJson>();
        target.path("/api/-/search")
                .queryParam("query", query)
                .queryParam("category", category)
                .queryParam("size", size)
                .queryParam("offset", offset)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .async()
                .get(new CompletableFutureCallback<>(result));
        return result;
    }

}