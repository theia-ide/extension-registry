/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/

const extArr = [
    {
        name: 'Test Ext',
        author: 'Thomas Test',
        license: 'FREE',
        version: '1.2.3',
        date: '12.12.2012',
        uri: '/some/path/to/the/extension.vsix',
        categories: [],
        description: 'This is a test extension thingy',
        icon: '/test.png',
        comments: ['Super', 'Awesome', 'Yeah, kind of ok.']
    },
    {
        name: 'Test Ext2',
        author: 'Thomas Test',
        license: 'FREE',
        version: '1.2.3',
        date: '12.12.2012',
        uri: '/some/path/to/the/extension.vsix',
        categories: [],
        description: 'This is a 2nd test extension thingy',
        icon: '/test.png',
        comments: ['Super', 'Awesome', 'Yeah, kind of ok.']
    },
    {
        name: 'Test Ext3',
        author: 'Thomas Test',
        license: 'FREE',
        version: '1.2.3',
        date: '12.12.2012',
        uri: '/some/path/to/the/extension.vsix',
        categories: [],
        description: 'This is a 3rd test extension thingy',
        icon: '/test.png',
        comments: ['Super', 'Awesome', 'Yeah, kind of ok.']
    },
    {
        name: 'Test Ext4',
        author: 'Thomas Test',
        license: 'FREE',
        version: '1.2.3',
        date: '12.12.2012',
        uri: '/some/path/to/the/extension.vsix',
        categories: [],
        description: 'This is a 4th test extension thingy',
        icon: '/test.png',
        comments: ['Super', 'Awesome', 'Yeah, kind of ok.']
    },
    {
        name: 'Test Ext5',
        author: 'Thomas Test',
        license: 'FREE',
        version: '1.2.3',
        date: '12.12.2012',
        uri: '/some/path/to/the/extension.vsix',
        categories: [],
        description: 'This is a 5th test extension thingy',
        icon: '/test.png',
        comments: ['Super', 'Awesome', 'Yeah, kind of ok.']
    },
    {
        name: 'Test Ext6',
        author: 'Thomas Test',
        license: 'FREE',
        version: '1.2.3',
        date: '12.12.2012',
        uri: '/some/path/to/the/extension.vsix',
        categories: [],
        description: 'This is a 6th test extension thingy',
        icon: '/test.png',
        comments: ['Super', 'Awesome', 'Yeah, kind of ok.']
    },
    {
        name: 'Test Ext7',
        author: 'Thomas Test',
        license: 'FREE',
        version: '1.2.3',
        date: '12.12.2012',
        uri: '/some/path/to/the/extension.vsix',
        categories: [],
        description: 'This is a 7th test extension thingy',
        icon: '/test.png',
        comments: ['Super', 'Awesome', 'Yeah, kind of ok.']
    },
    {
        name: 'Test Ext8',
        author: 'Thomas Test',
        license: 'FREE',
        version: '1.2.3',
        date: '12.12.2012',
        uri: '/some/path/to/the/extension.vsix',
        categories: [],
        description: 'This is a 8th test extension thingy',
        icon: '/test.png',
        comments: ['Super', 'Awesome', 'Yeah, kind of ok.']
    },
    {
        name: 'Test Ext9',
        author: 'Thomas Test',
        license: 'FREE',
        version: '1.2.3',
        date: '12.12.2012',
        uri: '/some/path/to/the/extension.vsix',
        categories: [],
        description: 'This is a 9th test extension thingy',
        icon: '/test.png',
        comments: ['Super', 'Awesome', 'Yeah, kind of ok.']
    }
];

export interface ExtensionFilter {
    fullText?: string;
    category?: string;
    author?: string;
}

export interface Extension {
    name: string;
    author: string;
    license: string;
    date: string;
    version: string;
    description: string;
    categories: string[];
    uri: string;
    comments: string[];
    icon: string;
}

export class ExtensionRegistryAPI {
    async getExtensions(filter?: ExtensionFilter): Promise<Extension[]> {
        return extArr;
    }

    async getExtension(id: string): Promise<Extension | undefined> {
        const ext = extArr.find(e => e.name === id);
        return ext;
    }
}