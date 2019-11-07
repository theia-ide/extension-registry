/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/

import { ExtensionFilter, Extension, ExtensionRegistryUser, ExtensionReview, ExtensionRaw } from "./extension-registry-types";
import { createURL } from "./utils";

const testMD = `
# Theia CLI

\`theia\` is a command line tool to manage Theia applications.

- [**Getting started**](#getting-started)
- [**Configure**](#configure)
  - [**Build Target**](#build-target)
  - [**Using latest builds**](#using-latest-builds)
- [**Building**](#building)
- [**Rebuilding native modules**](#rebuilding-native-modules)
- [**Running**](#running)
- [**Debugging**](#debugging)

## Getting started

Install \`@theia/cli\` as a dev dependency in your application.

With yarn:

    yarn add @theia/cli@next --dev

With npm:

    npm install @theia/cli@next --save-dev

## Configure

A Theia application is configured via \`theia\` property in package.json.

### Build Target

The following targets are supported: \`browser\` and \`electron\`. By default \`browser\` target is used.
The target can be configured in package.json via \`theia/target\` property, e.g:

json
{
    "theia": {
        "target": "electron"
    },
    "dependencies": {
        "@theia/electron": "latest"
    }
}


For electron target make sure to install required Electron runtime dependenices. The easiest way is to install @theia/electron package.

### Using latest builds

If you set next in your theia config, then Theia will prefer next over latest as the latest tag.

json
{
    "theia": {
        "next": "true"
    }
}


## Building

To build once:

    theia build --mode development

In order to rebuild on each change:

    theia build --watch --mode development

To build for production:

    theia build

In order to clean up the build result:

    theia clean

Arguments are passed directly to [webpack](https://webpack.js.org/), use --help to learn which options are supported.

## Rebuilding native modules

In order to run electron one should rebuild native node modules for an electron version:

    theia rebuild

To rollback native modules change the target to browser and run the command again.

## Running

To run the backend server:

    theia start

For the browser target a server is started on http://localhost:3000 by default.
For the electron target a server is started on localhost host with the dynamically allocated port by default.

Arguments are passed directly to a server, use --help to learn which options are supported.

## Debugging

To debug the backend server:

    theia start --inspect

Theia CLI accepts --inspect node flag: https://nodejs.org/en/docs/inspector/#command-line-options.
`;

const user: ExtensionRegistryUser = {
    firstName: 'Friendly',
    lastName: 'User',
    userName: 'userfriendly',
    email: 'userfriendly@test.ie'
};

const comment = `Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut
         labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et
          ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem
           ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt
            ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et
             ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.`;

const date = '2019/08/15';

const ratings: ExtensionReview[] = [
    {
        rating: 4,
        title: 'Good',
        comment,
        user,
        date
    },
    {
        rating: 5,
        title: 'Wow',
        comment,
        user,
        date
    },
    {
        rating: 3,
        title: 'Kind of okay...ish',
        comment,
        user,
        date
    },
    {
        rating: 1,
        title: 'Evil',
        comment,
        user,
        date
    }
];

const test = {
    publisher: 'ms-python',
    license: 'FREE',
    allVersions: ['1.0.0', '1.2.1', '1.2.2'],
    date: '12.12.2012',
    timestamp: Date.now(),
    extensionFileName: 'extension.vsix',
    preview: false,
    longDescription: testMD,
    icon: '/test.png',
    ratings
};

const extArr: Extension[] = [
    {
        name: 'python',
        description: 'This is a test extension thingy',
        categories: ['Other'],
        ...test
    },
    {
        name: 'Test Ext2',
        description: 'This is a 2nd test extension thingy',
        categories: ['Themes'],
        ...test
    },
    {
        name: 'Test Ext3',
        description: 'This is a 3rd test extension thingy',
        categories: ['Themes', 'Snippets'],
        ...test
    },
    {
        name: 'Test Ext4',
        description: 'This is a 4th test extension thingy',
        categories: ['Themes', 'Extension Packs'],
        ...test
    },
    {
        name: 'Test Ext5',
        description: 'This is a 5th test extension thingy',
        categories: ['Other'],
        ...test
    },
    {
        name: 'Test Ext6',
        description: 'This is a 6th test extension thingy',
        categories: ['Programming Languages'],
        ...test
    },
    {
        name: 'Test Ext7',
        description: 'This is a 7th test extension thingy',
        categories: ['Programming Languages'],
        ...test
    },
    {
        name: 'Test Ext8',
        description: 'This is a 8th test extension thingy',
        categories: ['Other'],
        ...test
    },
    {
        name: 'Test Ext9',
        description: 'This is a 9th test extension thingy',
        categories: ['Programming Languages'],
        ...test
    }
];

export interface ExtensionRegistryAPIRequest<T> {
    endpoint: string,
    operation: (json: any) => T
}

export interface ExtensionRegistryAPIRequestWithoutPayload<T> extends ExtensionRegistryAPIRequest<T> {
    method: 'GET' | 'DELETE'
}

export interface ExtensionRegistryAPIRequestWithPayload<T> extends ExtensionRegistryAPIRequest<T> {
    method: 'POST' | 'PUT',
    payload: any
}

export class ExtensionRegistryAPI {

    async fetch<T>(req: ExtensionRegistryAPIRequestWithPayload<T> | ExtensionRegistryAPIRequestWithoutPayload<T>): Promise<T> {
        let payload: string;

        if (req.method === 'POST' || req.method === 'PUT') {
            payload = JSON.stringify(req.payload);
        }

        const response = await new Promise<string>((resolve, reject) => {
            const request = new XMLHttpRequest();
            request.open(req.method, req.endpoint);
            request.addEventListener('load', () => {
                resolve(request.responseText);
            });
            request.addEventListener('error', (event) => {
                reject(event);
            });
            request.send(payload);
        });

        return req.operation(response);
    }

    async getExtensions(filter?: ExtensionFilter): Promise<Extension[]> {
        if (filter) {
            return extArr.filter(ext => {
                const hasCat = !!filter.category ? ext.categories && !!ext.categories.find(cat => cat === filter.category) : true;
                const containsText = !!filter.fullText ?
                    (ext.name.toLowerCase().includes(filter.fullText.toLowerCase()) ||
                        ext.description && ext.description.toLowerCase().includes(filter.fullText.toLowerCase())) : true;
                const matchesFilter = hasCat && containsText;
                return matchesFilter;
            })
        }
        return extArr;
    }

    async getExtension(extension: ExtensionRaw, url: string): Promise<Extension> {
        const ext = await this.fetch<Extension>({
            method: 'GET',
            endpoint: ExtensionRaw.getExtensionApiUrl(url, extension),
            operation: response => JSON.parse(response)
        });
        return ext;
    }

    async getExtensionReadMe(extension: ExtensionRaw, url: string): Promise<string> {
        const readme = await this.fetch<string>({
            method: 'GET',
            endpoint: createURL([ExtensionRaw.getExtensionApiUrl(url, extension), 'file', 'ReadMe.md']),
            operation: json => json
        });
        return readme;
    }

    async getExtensionReviews(extension: ExtensionRaw, url: string): Promise<ExtensionReview[]> {
        const reviews = await this.fetch<string>({
            method: 'GET',
            endpoint: createURL([ExtensionRaw.getExtensionApiUrl(url, extension), 'reviews']),
            operation: json => json
        });
        console.log("REVIEWS", reviews);
        return [];
    }

    async postReview(rating: ExtensionReview) {
        console.log(rating);
    }

    async getUser(): Promise<ExtensionRegistryUser> {
        return user;
    }
}