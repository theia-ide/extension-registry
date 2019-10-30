/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/

import { ExtensionFilter, Extension, ExtensionRegistryUser, ExtensionRating } from "./extension-registry-types";

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
}

const comment = `Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut
         labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et
          ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem
           ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt
            ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et
             ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.`;

const date = '2019/08/15';

const ratings: ExtensionRating[] = [
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
]

const test = {
    author: 'Thomas Test',
    license: 'FREE',
    version: '1.2.3',
    date: '12.12.2012',
    uri: '/some/path/to/the/extension.vsix',
    categories: [],
    longDescription: testMD,
    icon: '/test.png',
    ratings
};

const extArr: Extension[] = [
    {
        name: 'Test Ext',
        description: 'This is a test extension thingy',
        ...test
    },
    {
        name: 'Test Ext2',
        description: 'This is a 2nd test extension thingy',
        ...test
    },
    {
        name: 'Test Ext3',
        description: 'This is a 3rd test extension thingy',
        ...test
    },
    {
        name: 'Test Ext4',
        description: 'This is a 4th test extension thingy',
        ...test
    },
    {
        name: 'Test Ext5',
        description: 'This is a 5th test extension thingy',
        ...test
    },
    {
        name: 'Test Ext6',
        description: 'This is a 6th test extension thingy',
        ...test
    },
    {
        name: 'Test Ext7',
        description: 'This is a 7th test extension thingy',
        ...test
    },
    {
        name: 'Test Ext8',
        description: 'This is a 8th test extension thingy',
        ...test
    },
    {
        name: 'Test Ext9',
        description: 'This is a 9th test extension thingy',
        ...test
    }
];

export class ExtensionRegistryAPI {
    async getExtensions(filter?: ExtensionFilter): Promise<Extension[]> {
        return extArr;
    }

    async getExtension(id: string): Promise<Extension | undefined> {
        const ext = extArr.find(e => e.name === id);
        return ext;
    }

    async postReview(rating: ExtensionRating) {
        console.log(rating);
    }

    async getUser(): Promise<ExtensionRegistryUser> {
        return user;
    }
}