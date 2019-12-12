/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/
package io.typefox.extreg.repositories;

import org.springframework.data.repository.Repository;

import io.typefox.extreg.entities.ExtensionBinary;
import io.typefox.extreg.entities.ExtensionVersion;

public interface ExtensionBinaryRepository extends Repository<ExtensionBinary, Long> {

    ExtensionBinary findByExtension(ExtensionVersion extVersion);

}