/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/

import * as tmp from 'tmp';

export function createTempFile(options: tmp.TmpNameOptions): Promise<string> {
    return new Promise((resolve, reject) => {
        tmp.tmpName(options, (err, name) => {
            if (err)
                reject(err);
            else
                resolve(name);
        });
    });
}

export function handleError(reason: any): void {
    if (reason instanceof Error) {
        console.error(`\u274c  ${reason.message}`);
    } else if (typeof reason === 'string') {
        console.error(`\u274c  ${reason}`);
    } else {
        console.error(reason);
    }
    process.exit(1);
}
