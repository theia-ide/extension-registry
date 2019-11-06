/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/
package io.typefox.extreg.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import com.google.common.io.ByteStreams;

public final class ArchiveUtil {

    // Limit the size of fetched zip entries to 32 MB
    private static final long MAX_ENTRY_SIZE = 33_554_432;

    private ArchiveUtil() {}

    public static byte[] readEntry(byte[] archive, String entryName) {
        try (var zipStream = new ZipInputStream(new ByteArrayInputStream(archive))) {
            ZipEntry entry;
            while ((entry = zipStream.getNextEntry()) != null) {
                String name = entry.getName().replace('\\', '/');
                if (name.equalsIgnoreCase(entryName)) {
                    if (entry.getSize() > MAX_ENTRY_SIZE)
                        throw new ErrorResultException("The file " + entryName + " exceeds the size limit of 32 MB.");
                    return ByteStreams.toByteArray(zipStream);
                }
            }
            return null;
        } catch (ZipException exc) {
            throw new ErrorResultException("Could not read zip file: " + exc.getMessage(), exc);
        } catch (IOException exc) {
            throw new RuntimeException(exc);
        }
    }

}