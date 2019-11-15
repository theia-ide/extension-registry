/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/
package io.typefox.extreg;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

import javax.persistence.EntityManagerFactory;

import org.hibernate.Session;
import org.jboss.logging.Logger;

import io.typefox.extreg.entities.UserSession;

/**
 * Daemon thread that removes outdated user sessions from the database.
 * XXX Do we still need this?
 *     If yes, use a ScheduledExecutorService to schedule it at a fixed rate.
 */
public class UserSessionReaper implements Runnable {

    private static final Duration SESSION_DURATION = Duration.of(6, ChronoUnit.DAYS);

    private final EntityManagerFactory entityManagerFactory;

    private Logger logger = Logger.getLogger(UserSessionReaper.class);

    public UserSessionReaper(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public void run() {
        var entityManager = entityManagerFactory.createEntityManager();
        var transaction = entityManager.unwrap(Session.class).beginTransaction();

        var qs = "SELECT us FROM UserSession us";
        var query = entityManager.createQuery(qs, UserSession.class);
        var userSessions = query.getResultStream();
        var currentTime = LocalDateTime.now(ZoneId.of("UTC"));
        var toRemove = userSessions
                .filter(us -> Duration.between(us.getLastUsed(), currentTime).compareTo(SESSION_DURATION) >= 0)
                .collect(Collectors.toList());
        toRemove.forEach(us -> entityManager.remove(us));
        logger.debug("Deleted user sessions: " + toRemove.size());

        transaction.commit();
        entityManager.close();
    }

}