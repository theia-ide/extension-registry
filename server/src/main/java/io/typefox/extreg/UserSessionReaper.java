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

import javax.persistence.EntityManagerFactory;

import org.hibernate.search.mapper.orm.common.impl.HibernateOrmUtils;
import org.jboss.logging.Logger;

import io.typefox.extreg.entities.UserSession;

/**
 * Daemon thread that removes outdated user sessions from the database.
 */
public class UserSessionReaper implements Runnable {

    private static final Duration SESSION_DURATION = Duration.of(6, ChronoUnit.DAYS);
    private static final long SLEEP_TIME = 30_000;

    private final EntityManagerFactory entityManagerFactory;

    private Logger logger = Logger.getLogger(UserSessionReaper.class);

    public UserSessionReaper(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException exc) {
                // Interrupt to force wake up
            }

            var entityManager = entityManagerFactory.createEntityManager();
            var transaction = HibernateOrmUtils.toSessionImplementor(entityManager).beginTransaction();

            var qs = "SELECT us FROM UserSession us";
            var query = entityManager.createQuery(qs, UserSession.class);
            var userSessions = query.getResultList();
            var currentTime = LocalDateTime.now(ZoneId.of("UTC"));
            int deletedRows = 0;
            for (var userSession : userSessions) {
                var age = Duration.between(userSession.getLastUsed(), currentTime);
                if (age.compareTo(SESSION_DURATION) >= 0) {
                    entityManager.remove(userSession);
                    deletedRows++;
                }
            }
            logger.debug("Deleted user sessions: " + deletedRows);

            transaction.commit();
            entityManager.close();
        }
    }

}