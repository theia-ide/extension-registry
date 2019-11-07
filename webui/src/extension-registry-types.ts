import { createURL } from "./utils";

/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/

export interface ExtensionFilter {
    fullText?: string;
    category?: ExtensionCategory;
    size?: number;
    offset?: number;
}

export interface ExtensionRaw {
    readonly name: string;
    readonly publisher: string;
    readonly version?: string;
    readonly averageRating?: number;
    readonly iconFileName?: string;
    readonly timestamp?: number;
}
export namespace ExtensionRaw {
    export function getExtensionApiUrl(root: string, extension: ExtensionRaw) {
        const arr = [root, extension.publisher, extension.name, extension.version || ''];
        return createURL(arr);
    }
}

export interface Extension extends ExtensionRaw {
    readonly allVersions: string[];
    readonly extensionFileName: string;
    readonly readmeFileName?: string;
    readonly description?: string;
    readonly categories?: string[];

    readonly preview: boolean;

    readonly displayName?: string;
    readonly error?: string;

    readonly keywords?: string[];
    readonly license?: string;
    readonly homepage?: string;
    readonly repository?: string;
    readonly bugs?: string;
    readonly markdown?: string;
    readonly galleryColor?: string;
    readonly galleryTheme?: string;
    readonly qna?: string;
    readonly badges?: Badge[];
    readonly dependencies?: ExtensionReference[];
    readonly bundledExtensions?: ExtensionReference[];
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

export type StarNumber = 1 | 2 | 3 | 4 | 5;
export interface ExtensionReview {
    rating: StarNumber;
    title: string;
    comment: string;
    user: ExtensionRegistryUser;
    date: string;
}

export interface ExtensionRegistryUser {
    firstName?: string;
    lastName?: string;
    userName: string;
    email?: string;
}

export type ExtensionCategory =
    '' |
    'Programming Languages' |
    'Snippets' |
    'Linters' |
    'Themes' |
    'Debuggers' |
    'Formatters' |
    'Keymaps' |
    'SCM Providers' |
    'Other' |
    'Extension Packs' |
    'Language Packs'