/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/
package io.typefox.extreg.upload;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.google.common.io.ByteStreams;

import org.hibernate.engine.jdbc.LobCreator;

import io.typefox.extreg.entities.Extension;
import io.typefox.extreg.entities.ExtensionBinary;

public class ExtensionProcessor {

    private static final String PACKAGE_JSON = "extension/package.json";

    private final byte[] content;
    private String packageJson;

    public ExtensionProcessor(byte[] content) {
        this.content = content;
        try {
            var zipStream = new ZipInputStream(new ByteArrayInputStream(content));
            ZipEntry entry;
            while ((entry = zipStream.getNextEntry()) != null) {
                switch (entry.getName()) {
                    case PACKAGE_JSON:
                        packageJson = new String(ByteStreams.toByteArray(zipStream), Charset.forName("UTF-8"));
                        break;
                }
            }
            zipStream.close();
        } catch (ZipException exc) {
            throw new WebApplicationException("Could not read zip file: " + exc.getMessage(), Response.Status.BAD_REQUEST);
        } catch (IOException exc) {
            throw new RuntimeException(exc);
        }
    }

    public Extension getMetadata() {
        if (packageJson == null) {
            throw new WebApplicationException("Entry not found: " + PACKAGE_JSON, Response.Status.BAD_REQUEST);
        }
        var extension = new Extension();
        // TODO: parse package.json and copy content to extension
        return extension;
    }

    public ExtensionBinary getBinary(Extension extension, LobCreator lobCreator) {
        var binary = new ExtensionBinary();
        binary.extension = extension;
        binary.content = lobCreator.createBlob(content);
        return binary;
    }

}