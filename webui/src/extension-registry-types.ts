/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/

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
    longDescription: string;
    categories: string[];
    uri: string;
    ratings: ExtensionRating[];
    icon: string;
}

export interface ExtensionRating {
    rating: 1 | 2 | 3 | 4 | 5;
    title: string;
    comment: string;
    user: ExtensionRegistryUser;
    date: string;
}

export interface ExtensionRegistryUser {
    firstName: string;
    lastName: string;
    userName: string;
    email: string;
}