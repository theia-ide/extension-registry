/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/
package io.typefox.extreg;

import java.util.concurrent.CompletableFuture;

import io.typefox.extreg.json.ExtensionJson;
import io.typefox.extreg.json.PublisherJson;
import io.typefox.extreg.json.ReviewListJson;
import io.typefox.extreg.json.SearchResultJson;

/**
 * Declaration of the registry API methods that can be accessed without authentication.
 */
public interface IExtensionRegistry {

    CompletableFuture<PublisherJson> getPublisher(String publisherName);

    CompletableFuture<ExtensionJson> getExtension(String publisherName, String extensionName);

    CompletableFuture<ExtensionJson> getExtensionVersion(String publisherName, String extensionName, String version);

    CompletableFuture<byte[]> getFile(String publisherName, String extensionName, String fileName);

    CompletableFuture<byte[]> getFile(String publisherName, String extensionName, String version, String fileName);

    CompletableFuture<ReviewListJson> getReviews(String publisherName, String extensionName);

    CompletableFuture<SearchResultJson> search(String query, String category, int size, int offset);

}