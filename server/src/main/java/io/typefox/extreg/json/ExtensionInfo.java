/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/
package io.typefox.extreg.json;

import java.time.LocalDateTime;
import java.util.List;

public class ExtensionInfo {

    public static ExtensionInfo error(String message) {
        var info = new ExtensionInfo();
        info.error = message;
        return info;
    }

    public String error;

    public String name;

    public String publisher;

    public List<String> allVersions;

    public String version;

    public boolean preview;

    public LocalDateTime timestamp;

    public String displayName;

    public String description;

    public List<String> categories;

    public List<String> keywords;

    public String license;

    public String homepage;

    public String repository;

    public String bugs;

    public String markdown;

    public String galleryColor;

    public String galleryTheme;

    public List<Badge> badges;

    public String qna;

    public List<String> dependencies;

    public List<String> bundledExtensions;

}