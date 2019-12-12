/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/
package io.typefox.extreg.repositories;

import java.util.List;

import org.springframework.data.repository.Repository;
import org.springframework.data.util.Streamable;

import io.typefox.extreg.entities.Extension;
import io.typefox.extreg.entities.Publisher;

public interface ExtensionRepository extends Repository<Extension, Long> {

    List<Extension> findByPublisher(Publisher publisher);

    List<Extension> findByPublisherOrderByNameAsc(Publisher publisher);

    Extension findByNameAndPublisher(String name, Publisher publisher);

    Extension findByNameAndPublisherName(String name, String publisherName);

    Streamable<Extension> findAll();

}