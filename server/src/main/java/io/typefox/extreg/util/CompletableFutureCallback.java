/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/
package io.typefox.extreg.util;

import java.util.concurrent.CompletableFuture;

import javax.ws.rs.client.InvocationCallback;

public class CompletableFutureCallback<T> implements InvocationCallback<T> {

    private final CompletableFuture<T> completableFuture;

    public CompletableFutureCallback(CompletableFuture<T> completableFuture) {
        this.completableFuture = completableFuture;
    }

    @Override
    public void completed(T response) {
        completableFuture.complete(response);
    }

    @Override
    public void failed(Throwable throwable) {
        completableFuture.completeExceptionally(throwable);
    }

}