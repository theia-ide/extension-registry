/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/

import * as fs from 'fs';
import { promisify } from 'util';
import { Registry } from "./registry";

/**
 * Downloads an extension or its metadata.
 */
export async function getExtension(options: GetOptions): Promise<void> {
    if (!options.registryUrl) {
        options.registryUrl = process.env.OVSX_REGISTRY_URL;
    }
    const registry = new Registry({ url: options.registryUrl });
    const match = /(\w+)\.(\w+)@?(.+)?/.exec(options.extensionId);
    if (!match) {
        throw new Error('The extension identifier must have the form `publisher.extension@version` (the version is optional).');
    }
    const extension = await registry.getMetadata(match[1], match[2], match[3]);
    if (extension.error) {
        throw new Error(extension.error);
    }
    if (options.metadata) {
        const metadata = JSON.stringify(extension, null, 4);
        if (options.output) {
            await promisify(fs.writeFile)(options.output, metadata);
        } else {
            console.log(metadata);
        }
    } else {
        // TODO
    }
}

export interface GetOptions {
    /**
     * Identifier in the form `publisher.extension@version`. The suffix `@version` is optional.
     */
    extensionId: string;
    /**
     * The base URL of the registry API.
     */
    registryUrl?: string;
    /**
     * Save the output in the specified file or directory.
     */
    output?: string;
    /**
     * Print the extension's metadata instead of downloading it.
     */
    metadata?: boolean;
}
