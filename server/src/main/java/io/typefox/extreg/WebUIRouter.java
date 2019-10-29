/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/
package io.typefox.extreg;

import io.quarkus.vertx.web.Route;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

public class WebUIRouter {

    @Route(path = "/*", methods = HttpMethod.GET)
    public void staticResource(RoutingContext context) {
        var segments = context.request().path().split("/");
        if (segments.length <= 1 || segments[1].startsWith("api")) {
            // Root path or API access
            context.next();
            return;
        }
        var lastSegment = segments[segments.length - 1];
        if (lastSegment.contains(".")) {
            if (segments.length == 2) {
                // Static resource in root path
                context.next();
            } else {
                // Route static resource
                context.reroute("/" + segments[segments.length - 1]);
            }
        } else {
            // Route index.html
            context.reroute("/");
        }
    }

}