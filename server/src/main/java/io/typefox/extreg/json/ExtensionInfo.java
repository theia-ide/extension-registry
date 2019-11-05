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

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;;

@JsonInclude(Include.NON_NULL)
public class ExtensionInfo {

    public static ExtensionInfo error(String message) {
        var info = new ExtensionInfo();
        info.error = message;
        return info;
    }

    @Nullable
    public String error;

    public String name;

    public String publisher;

    public List<String> allVersions;

    public String version;

    public LocalDateTime timestamp;

    @Nullable
    public Boolean preview;

    @Nullable
    public String displayName;

    @Nullable
    public String description;

    @Nullable
    public List<String> categories;

    @Nullable
    public List<String> keywords;

    @Nullable
    public String license;

    @Nullable
    public String homepage;

    @Nullable
    public String repository;

    @Nullable
    public String bugs;

    @Nullable
    public String markdown;

    @Nullable
    public String galleryColor;

    @Nullable
    public String galleryTheme;

    @Nullable
    public String qna;

    @Nullable
    public List<Badge> badges;

    @Nullable
    public List<ExtensionReference> dependencies;

    @Nullable
    public List<ExtensionReference> bundledExtensions;

}