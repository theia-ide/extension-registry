/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/
package io.typefox.extreg;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.hibernate.NonUniqueResultException;
import org.springframework.stereotype.Component;

import io.typefox.extreg.entities.Extension;
import io.typefox.extreg.entities.ExtensionBinary;
import io.typefox.extreg.entities.ExtensionIcon;
import io.typefox.extreg.entities.ExtensionReadme;
import io.typefox.extreg.entities.ExtensionReview;
import io.typefox.extreg.entities.ExtensionVersion;
import io.typefox.extreg.entities.Publisher;
import io.typefox.extreg.entities.UserSession;
import io.typefox.extreg.util.ErrorResultException;
import io.typefox.extreg.util.SemanticVersion;

@Component
public class EntityService {

    @Inject
    EntityManager entityManager;

    public Publisher findPublisher(String name) {
        var qs = "SELECT pub FROM Publisher pub WHERE (pub.name = :name)";
        var query = entityManager.createQuery(qs, Publisher.class);
        query.setParameter("name", name);
        return query.getSingleResult();
    }

    public Optional<Publisher> findPublisherOptional(String name) {
        var qs = "SELECT pub FROM Publisher pub WHERE (pub.name = :name)";
        var query = entityManager.createQuery(qs, Publisher.class);
        query.setParameter("name", name);
        return getOptionalResult(query);
    }

    public Extension findExtension(String name, Publisher publisher) {
        var qs = "SELECT ext FROM Extension ext WHERE (ext.publisher = :publisher and ext.name = :name)";
        var query = entityManager.createQuery(qs, Extension.class);
        query.setParameter("publisher", publisher);
        query.setParameter("name", name);
        return query.getSingleResult();
    }

    public Extension findExtension(String publisherName, String extensionName) {
        var publisher = findPublisher(publisherName);
        return findExtension(extensionName, publisher);
    }

    public Optional<Extension> findExtensionOptional(String name, Publisher publisher) {
        var qs = "SELECT ext FROM Extension ext WHERE (ext.publisher = :publisher and ext.name = :name)";
        var query = entityManager.createQuery(qs, Extension.class);
        query.setParameter("publisher", publisher);
        query.setParameter("name", name);
        return getOptionalResult(query);
    }

    private <T> Optional<T> getOptionalResult(TypedQuery<T> query) {
        var list = query.getResultList();
        if (list.isEmpty())
            return Optional.empty();
        else if (list.size() == 1)
            return Optional.of(list.get(0));
        else
            throw new NonUniqueResultException(list.size());
    }

    public List<String> getAllExtensionNames(Publisher publisher) {
        var qs = "SELECT ext.name FROM Extension ext WHERE (ext.publisher = :publisher)";
        var query = entityManager.createQuery(qs, String.class);
        query.setParameter("publisher", publisher);
        return query.getResultList();
    }

    public ExtensionVersion findVersion(String version, Extension extension) {
        var qs = "SELECT exv FROM ExtensionVersion exv WHERE (exv.extension = :extension and exv.version = :version)";
        var query = entityManager.createQuery(qs, ExtensionVersion.class);
        query.setParameter("extension", extension);
        query.setParameter("version", version);
        return query.getSingleResult();
    }

    public ExtensionVersion findVersion(String publisherName, String extensionName, String version) {
        var extension = findExtension(publisherName, extensionName);
        return findVersion(version, extension);
    }

    public List<String> getAllVersionStrings(Extension extension) {
        var qs = "SELECT exv.version FROM ExtensionVersion exv WHERE (exv.extension = :extension)";
        var query = entityManager.createQuery(qs, String.class);
        query.setParameter("extension", extension);
        return query.getResultList();
    }

    public ExtensionBinary findBinary(ExtensionVersion extVersion) {
        var qs = "SELECT bin FROM ExtensionBinary bin WHERE (bin.extension = :extension)";
        var query = entityManager.createQuery(qs, ExtensionBinary.class);
        query.setParameter("extension", extVersion);
        return query.getSingleResult();
    }

    public ExtensionIcon findIcon(ExtensionVersion extVersion) {
        var qs = "SELECT ico FROM ExtensionIcon ico WHERE (ico.extension = :extension)";
        var query = entityManager.createQuery(qs, ExtensionIcon.class);
        query.setParameter("extension", extVersion);
        return query.getSingleResult();
    }

    public ExtensionReadme findReadme(ExtensionVersion extVersion) {
        var qs = "SELECT rdm FROM ExtensionReadme rdm WHERE (rdm.extension = :extension)";
        var query = entityManager.createQuery(qs, ExtensionReadme.class);
        query.setParameter("extension", extVersion);
        return query.getSingleResult();
    }

    public List<ExtensionReview> findAllReviews(Extension extension) {
        var qs = "SELECT rev FROM ExtensionReview rev WHERE (rev.extension = :extension)";
        var query = entityManager.createQuery(qs, ExtensionReview.class);
        query.setParameter("extension", extension);
        return query.getResultList();
    }

    public int countReviews(Extension extension) {
        var qs = "SELECT count(*) FROM ExtensionReview rev WHERE (rev.extension = :extension)";
        var query = entityManager.createQuery(qs, Long.class);
        query.setParameter("extension", extension);
        return query.getSingleResult().intValue();
    }

    public void checkUniqueVersion(String version, Extension extension) {
        var qs = "SELECT count(*) FROM ExtensionVersion exv WHERE (exv.extension = :extension and exv.version = :version)";
        var query = entityManager.createQuery(qs, Long.class);
        query.setParameter("extension", extension);
        query.setParameter("version", version);
        if (query.getSingleResult() > 0)
            throw new ErrorResultException("Extension " + extension.getName() + " version " + version + " is already published.");
    }

    public boolean isLatestVersion(String version, Extension extension) {
        var allVersions = getAllVersionStrings(extension);
        var newSemver = new SemanticVersion(version);
        for (var publishedVersion : allVersions) {
            var oldSemver = new SemanticVersion(publishedVersion);
            if (newSemver.compareTo(oldSemver) < 0)
                return false;
        }
        return true;
    }

    public UserSession findSession(String sessionId) {
        return entityManager.find(UserSession.class, sessionId);
    }

}