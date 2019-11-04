/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/
package io.typefox.extreg.upload;

import java.nio.charset.Charset;

import javax.json.bind.JsonbBuilder;
import javax.ws.rs.WebApplicationException;

import org.hibernate.engine.jdbc.LobCreator;

import io.typefox.extreg.entities.ExtensionBinary;
import io.typefox.extreg.entities.ExtensionIcon;
import io.typefox.extreg.entities.ExtensionReadme;
import io.typefox.extreg.entities.ExtensionVersion;
import io.typefox.extreg.util.ArchiveUtil;

public class ExtensionProcessor {

    private static final String PACKAGE_JSON = "extension/package.json";
    private static final String README = "extension/README";
    private static final String README_MD = "extension/README.md";

    private final byte[] content;
    private Package packageJson;

    public ExtensionProcessor(byte[] content) {
        this.content = content;
    }

    private void loadPackageJson() {
        if (packageJson == null) {
            var bytes = ArchiveUtil.readEntry(content, PACKAGE_JSON);
            if (bytes == null)
                throw new WebApplicationException("Entry not found: " + PACKAGE_JSON);
            var jsonb = JsonbBuilder.create();
            packageJson = jsonb.fromJson(new String(bytes, Charset.forName("UTF-8")), Package.class);
        }
    }

    public ExtensionVersion getMetadata() {
        loadPackageJson();
        var extension = new ExtensionVersion();
        // TODO copy metadata
        return extension;
    }

    public ExtensionBinary getBinary(ExtensionVersion extension, LobCreator lobCreator) {
        var binary = new ExtensionBinary();
        binary.extension = extension;
        binary.content = lobCreator.createBlob(content);
        return binary;
    }

    public ExtensionReadme getReadme(ExtensionVersion extension, LobCreator lobCreator) {
        var bytes = ArchiveUtil.readEntry(content, README_MD);
        if (bytes == null)
            bytes = ArchiveUtil.readEntry(content, README);
        if (bytes == null)
            return null;
        var readme = new ExtensionReadme();
        readme.extension = extension;
        readme.content = lobCreator.createClob(new String(bytes, Charset.forName("UTF-8")));
        return readme;
    }

    public ExtensionIcon getIcon(ExtensionVersion extension, LobCreator lobCreator) {
        loadPackageJson();
        if (packageJson.icon == null || packageJson.icon.isEmpty())
            return null;
        var bytes = ArchiveUtil.readEntry(content, "extension/" + packageJson.icon);
        if (bytes == null)
            return null;
        var icon = new ExtensionIcon();
        icon.extension = extension;
        icon.content = lobCreator.createBlob(bytes);
        return icon;
    }

}