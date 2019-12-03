/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/

import * as tmp from 'tmp';
import * as denodeify from 'denodeify';
import { createVSIX } from 'vsce';

const tmpName = denodeify<string>(tmp.tmpName);

export interface PublishOptions {
    /**
     * Personal access token.
     */
    pat?: string;
    /**
     * Path to the vsix file to be published. Cannot be used together with `packagePath`.
     */
    packageFile?: string;
    /**
     * Path to the extension to be packaged and published. Cannot be used together
     * with `packageFile`.
     */
    packagePath?: string;
    /**
	 * The base URL for links detected in Markdown files. Only valid with `packagePath`.
	 */
    baseContentUrl?: string;
    /**
	 * The base URL for images detected in Markdown files. Only valid with `packagePath`.
	 */
    baseImagesUrl?: string;
    /**
	 * Should use `yarn` instead of `npm`. Only valid with `packagePath`.
	 */
    yarn?: boolean;
}

export async function publish(options: PublishOptions = {}): Promise<any> {
    if (!options.packageFile) {
        options.packageFile = await tmpName();
        await createVSIX({
            cwd: options.packagePath,
            packagePath: options.packageFile,
            baseContentUrl: options.baseContentUrl,
            baseImagesUrl: options.baseImagesUrl,
            useYarn: options.yarn
        });
    }
    console.log('TODO', options);
}
