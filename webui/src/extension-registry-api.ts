/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/

import { ExtensionFilter, Extension } from "./extension-registry-types";

const lorem = "# Lorem ipsum dolor sit amet \n   " +
    "## _consetetur sadipscing elitr,_  \n " +
    "### sed diam nonumy eirmod \n " +
    " _tempor invidunt_ *ut labore* -et dolore-  \n\n    " +
    "### magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, " +
    "no sea takimata sanctus est Lorem ipsum dolor sit amet." +
    "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore " +
    "magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, " +
    "no sea takimata sanctus est Lorem ipsum dolor sit amet.";

const test = {
    author: 'Thomas Test',
    license: 'FREE',
    version: '1.2.3',
    date: '12.12.2012',
    uri: '/some/path/to/the/extension.vsix',
    categories: [],
    longDescription: lorem,
    icon: '/test.png',
    rating: 4.5,
    comments: ['Super', 'Awesome', 'Yeah, kind of ok.']
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
}