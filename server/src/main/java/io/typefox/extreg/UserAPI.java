/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/
package io.typefox.extreg;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import io.typefox.extreg.entities.UserData;
import io.typefox.extreg.entities.UserSession;
import io.typefox.extreg.json.UserJson;

@Path("/api/-/user")
public class UserAPI {

    @Inject
    EntityManager entityManager;

    @Inject
    EntityService entities;

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public UserJson userInfo(@CookieParam("sessionid") Cookie sessionCookie) {
        var json = new UserJson();
        if (sessionCookie == null) {
            json.error = "Not logged in.";
            return json;
        }
        var user = entities.findUser(sessionCookie.getValue());
        if (user == null) {
            json.error = "Invalid session.";
            return json;
        }
        json.name = user.getName();
        json.avatarUrl = "https://s.gravatar.com/avatar/9a638e5879d268e59d158a2091723c3c?s=80";
        return json;
    }

    @GET
    @Path("/login")
    @Transactional
    public Response login(@CookieParam("sessionid") Cookie sessionCookie) {
        if (sessionCookie != null) {
            var session = entities.findSession(sessionCookie.getValue());
            if (session != null) {
                return Response.temporaryRedirect(getRedirectURI())
                    .cookie(new NewCookie(sessionCookie))
                    .build();
            }
        }
        var user = entityManager.find(UserData.class, 1l);
        var session = new UserSession();
        session.setId(UUID.randomUUID().toString());
        session.setUser(user);
        entityManager.persist(session);
        return Response.temporaryRedirect(getRedirectURI())
                .cookie(new NewCookie("sessionid", session.getId()))
                .build();
    }

    @GET
    @Path("/logout")
    @Transactional
    public Response logout(@CookieParam("sessionid") Cookie sessionCookie) {
        if (sessionCookie == null) {
            return Response.temporaryRedirect(getRedirectURI()).build();
        }
        var session = entities.findSession(sessionCookie.getValue());
        if (session != null) {
            entityManager.remove(session);
        }
        return Response.temporaryRedirect(getRedirectURI())
            .cookie(new NewCookie(sessionCookie, null, 0, false))
            .build();
    }

    private URI getRedirectURI() {
        try {
            return new URI("/");
        } catch (URISyntaxException exc) {
            throw new WebApplicationException(exc);
		}
    }

}