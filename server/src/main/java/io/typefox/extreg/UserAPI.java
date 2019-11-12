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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.Transactional;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.runtime.StartupEvent;
import io.typefox.extreg.entities.UserData;
import io.typefox.extreg.entities.UserSession;
import io.typefox.extreg.json.UserJson;

@Path("/api/-/user")
public class UserAPI {

    private static final String COOKIE_COMMENT = "User session id";
    private static final int COOKIE_MAX_AGE = 604_800; // one week in seconds

    @Inject
    EntityManager entityManager;

    @Inject
    EntityService entities;

    @ConfigProperty(name = "quarkus.http.host")
    String httpHost;

    void onStart(@Observes StartupEvent event, @Context EntityManagerFactory entityManagerFactory) {
        var sessionReaper = new Thread(new UserSessionReaper(entityManagerFactory));
        sessionReaper.setName("User session reaper");
        sessionReaper.setDaemon(true);
        sessionReaper.start();
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response userInfo(@CookieParam("sessionid") Cookie sessionCookie) {
        var json = new UserJson();
        if (sessionCookie == null) {
            json.error = "Not logged in.";
            return Response.ok(json).build();
        }
        var session = entities.findSession(sessionCookie.getValue());
        if (session == null) {
            json.error = "Invalid session.";
            return Response.ok(json).build();
        }
        updateLastUsed(session);
        json.name = session.getUser().getName();
        json.avatarUrl = "https://s.gravatar.com/avatar/9a638e5879d268e59d158a2091723c3c?s=80";
        return Response.ok(json)
                .cookie(new NewCookie(sessionCookie, COOKIE_COMMENT, COOKIE_MAX_AGE, false))
                .build();
    }

    @GET
    @Path("/login")
    @Transactional
    public Response login(@CookieParam("sessionid") Cookie sessionCookie) {
        if (sessionCookie != null) {
            var session = entities.findSession(sessionCookie.getValue());
            if (session != null) {
                return Response.temporaryRedirect(getRedirectURI())
                    .cookie(new NewCookie(sessionCookie, COOKIE_COMMENT, COOKIE_MAX_AGE, false))
                    .build();
            }
        }
        var user = entityManager.find(UserData.class, 1l);
        var session = new UserSession();
        session.setId(UUID.randomUUID().toString());
        session.setUser(user);
        updateLastUsed(session);
        entityManager.persist(session);
        return Response.temporaryRedirect(getRedirectURI())
                .cookie(new NewCookie("sessionid", session.getId(),
                                      "/", getDomain(), COOKIE_COMMENT, COOKIE_MAX_AGE, false))
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

    private String getDomain() {
        try {
            var uri = new URI(httpHost);
            return uri.getHost();
        } catch (URISyntaxException exc) {
            throw new WebApplicationException(exc);
        }
    }

    private void updateLastUsed(UserSession session) {
        session.setLastUsed(LocalDateTime.now(ZoneId.of("UTC")));
    }

}