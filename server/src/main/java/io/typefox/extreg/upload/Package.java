/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/
package io.typefox.extreg.upload;

import java.util.List;
import java.util.Map;

/**
 * Java representation of the package.json file bundled in an extension.
 */
public class Package {

    public String name;

    public String version;

    public boolean preview;

    public String displayName;

    public String publisher;

    public String description;

    public List<String> categories;

    public List<String> keywords;

    public String license;

    public String icon;

    public GalleryBanner galleryBanner;

    public String homepage;

    public PackageRepository repository;

    public PackageBugs bugs;

    public String markdown;

    public String qna;

    public List<Badge> badges;

    public List<String> extensionDependencies;

    public List<String> extensionPack;

    public String main;

    public Map<String, String> engines;

    public Map<String, String> scripts;

    public List<String> activationEvents;

    public Map<String, String> dependencies;

    public Map<String, String> __metadata;

}