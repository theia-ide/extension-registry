/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/
package io.typefox.extreg;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.hibernate.engine.jdbc.LobCreator;

import io.typefox.extreg.entities.ExtensionBinary;
import io.typefox.extreg.entities.ExtensionIcon;
import io.typefox.extreg.entities.ExtensionReadme;
import io.typefox.extreg.entities.ExtensionVersion;
import io.typefox.extreg.util.ArchiveUtil;
import io.typefox.extreg.util.ErrorResultException;

public class ExtensionProcessor {

    private static final String PACKAGE_JSON = "extension/package.json";
    private static final String README = "extension/README";
    private static final String README_MD = "extension/README.md";

    private final byte[] content;
    private JsonNode packageJson;

    public ExtensionProcessor(byte[] content) {
        this.content = content;
    }

    private void loadPackageJson() {
        if (packageJson == null) {
            var bytes = ArchiveUtil.readEntry(content, PACKAGE_JSON);
            if (bytes == null)
                throw new ErrorResultException("Entry not found: " + PACKAGE_JSON);
            try {
                var mapper = new ObjectMapper();
                packageJson = mapper.readTree(bytes);
            } catch (JsonParseException exc) {
                throw new ErrorResultException("Invalid JSON format in " + PACKAGE_JSON
                        + ": " + exc.getMessage());
			} catch (IOException exc) {
				throw new RuntimeException(exc);
			}
        }
    }

    public String getExtensionName() {
        loadPackageJson();
        return packageJson.path("name").asText();
    }

    public String getPublisherName() {
        loadPackageJson();
        return packageJson.path("publisher").asText();
    }

    public List<String> getExtensionDependencies() {
        loadPackageJson();
        var result = getStringList(packageJson.path("extensionDependencies"));
        return result != null ? result : Collections.emptyList();
    }

    public List<String> getBundledExtensions() {
        loadPackageJson();
        var result = getStringList(packageJson.path("extensionPack"));
        return result != null ? result : Collections.emptyList();
    }

    public ExtensionVersion getMetadata() {
        loadPackageJson();
        var extension = new ExtensionVersion();
        extension.setTimestamp(LocalDateTime.now(ZoneId.of("UTC")));
        extension.setVersion(packageJson.path("version").textValue());
        extension.setPreview(packageJson.path("preview").booleanValue());
        extension.setDisplayName(packageJson.path("displayName").textValue());
        extension.setDescription(packageJson.path("description").textValue());
        extension.setCategories(getStringList(packageJson.path("categories")));
        extension.setKeywords(getStringList(packageJson.path("keywords")));
        extension.setLicense(packageJson.path("license").textValue());
        extension.setHomepage(getUrl(packageJson.path("homepage")));
        extension.setRepository(getUrl(packageJson.path("repository")));
        extension.setBugs(getUrl(packageJson.path("bugs")));
        extension.setMarkdown(packageJson.path("markdown").textValue());
        var galleryBanner = packageJson.path("galleryBanner");
        if (galleryBanner.isObject()) {
            extension.setGalleryColor(galleryBanner.path("color").textValue());
            extension.setGalleryTheme(galleryBanner.path("theme").textValue());
        }
        extension.setQna(packageJson.path("qna").textValue());
        return extension;
    }

    private List<String> getStringList(JsonNode node) {
        if (node.isArray()) {
            var list = new ArrayList<String>();
            for (JsonNode element : node) {
                if (element.isTextual())
                    list.add(element.textValue());
            }
            return list;
        }
        return null;
    }

    private String getUrl(JsonNode node) {
        if (node.isTextual())
            return node.textValue();
        if (node.isObject())
            return node.path("url").textValue();
        return null;
    }

    public ExtensionBinary getBinary(ExtensionVersion extension, LobCreator lobCreator) {
        var binary = new ExtensionBinary();
        binary.setExtension(extension);
        binary.setContent(lobCreator.createBlob(content));
        return binary;
    }

    public ExtensionReadme getReadme(ExtensionVersion extension, LobCreator lobCreator) {
        var bytes = ArchiveUtil.readEntry(content, README_MD);
        if (bytes == null)
            bytes = ArchiveUtil.readEntry(content, README);
        if (bytes == null)
            return null;
        var readme = new ExtensionReadme();
        readme.setExtension(extension);
        readme.setContent(lobCreator.createClob(new String(bytes, Charset.forName("UTF-8"))));
        return readme;
    }

    public ExtensionIcon getIcon(ExtensionVersion extension, LobCreator lobCreator) {
        loadPackageJson();
        var iconPath = packageJson.get("icon");
        if (iconPath == null || !iconPath.isTextual())
            return null;
        var bytes = ArchiveUtil.readEntry(content, "extension/" + iconPath.asText());
        if (bytes == null)
            return null;
        var icon = new ExtensionIcon();
        icon.setExtension(extension);
        icon.setContent(lobCreator.createBlob(bytes));
        return icon;
    }

}