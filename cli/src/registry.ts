/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/

import * as http from 'http';
import * as fs from 'fs';
import * as querystring from 'querystring';
import { URL } from 'url';

export const DEFAULT_URL = 'http://localhost:8080';

export class Registry {

    readonly url: string;

    constructor(options: RegistryOptions = {}) {
        if (options.url && options.url.endsWith('/'))
            this.url = options.url.substring(0, options.url.length - 1);
        else if (options.url)
            this.url = options.url;
        else
            this.url = DEFAULT_URL;
    }

    publish(file: string, pat?: string): Promise<Extension> {
        let query = undefined;
        if (pat) {
            query = { token: pat };
        }
        return this.post(file, '/api/-/publish', query, {
            'Content-Type': 'application/octet-stream'
        });
    }

    protected post<T extends Response>(file: string, path: string, query?: { [key: string]: string },
            headers?: http.OutgoingHttpHeaders): Promise<T> {
        return new Promise((resolve, reject) => {
            try {
                const stream = fs.createReadStream(file);
                const request = http.request(
                    this.getRequestOptions('POST', path, query, headers),
                    this.getJsonResponse<T>(resolve, reject)
                );
                stream.on('error', err => {
                    request.abort();
                    reject(err);
                });
                request.on('error', err => {
                    stream.close();
                    reject(err);
                });
                stream.on('open', () => stream.pipe(request));
            } catch (err) {
                reject(err);
            }
        });
    }

    private getRequestOptions(method: 'GET' | 'POST', path: string, query?: { [key: string]: string },
            headers?: http.OutgoingHttpHeaders): http.RequestOptions {
        const requestUrl = new URL(this.url + path);
        const options: http.RequestOptions = {
            method,
            hostname: requestUrl.hostname,
            port: requestUrl.port,
            path: requestUrl.pathname,
            headers
        };
        if (query) {
            options.path = options.path + '?' + querystring.stringify(query);
        }
        return options;
    }

    private getJsonResponse<T extends Response>(resolve: (value: T) => void, reject: (reason: Error) => void): (res: http.IncomingMessage) => void {
        return response => {
            response.setEncoding('UTF-8');
            let json = '';
            response.on('data', chunk => json += chunk);
            response.on('end', () => {
                if (response.statusCode !== undefined && (response.statusCode < 200 || response.statusCode > 299)) {
                    reject(new Error(`The server responded with status ${response.statusCode}: ${response.statusMessage}`));
                } else {
                    resolve(JSON.parse(json));
                }
            });
        };
    }

}

export interface RegistryOptions {
    url?: string;
}

export interface Response {
    error?: string;
}

export interface Extension extends Response {
    name: string;
    publisher: string;
    displayName?: string;
    version: string;
    preview?: boolean;
    averageRating?: number;
    timestamp?: string;
    description?: string;

    url: string;
    iconUrl?: string;
    publisherUrl: string;
    reviewsUrl: string;
    downloadUrl: string;
    readmeUrl?: string;

    allVersions: { [key: string]: string };
    categories?: string[];
    tags?: string[];
    license?: string;
    homepage?: string;
    repository?: string;
    bugs?: string;
    markdown?: string;
    galleryColor?: string;
    galleryTheme?: string;
    qna?: string;
    badges?: Badge[];
    dependencies?: ExtensionReference[];
    bundledExtensions?: ExtensionReference[];
}

export interface Badge {
    url: string;
    href: string;
    description: string;
}

export interface ExtensionReference {
    publisher: string;
    extension: string;
    version?: string;
}
