/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/
package io.typefox.extreg.entities;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;

@Entity
public class ExtensionVersion {

    @Id
    @GeneratedValue
    public long id;

    @ManyToOne
    @MapsId
    public Extension extension;

    public String version;

    public boolean preview;

    public LocalDateTime timestamp;

    public String displayName;

    public String description;

    @ElementCollection
    public List<String> categories;

    @ElementCollection
    public List<String> keywords;

    public String license;

    public String homepage;

    public String repository;

    public String bugs;

    public String markdown;

    public String qna;

    @ManyToMany
    public List<Extension> dependencies;

    @ManyToMany
    public List<Extension> bundledExtensions;

}