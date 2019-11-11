/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/

import { ExtensionRegistryAPI } from "./extension-registry-api";
import { ExtensionFilter, Extension, ExtensionReview, ExtensionRegistryUser, ExtensionCategory, ExtensionRaw, ExtensionReviewList } from "./extension-registry-types";
import { createURL } from "./utils";

export class ExtensionRegistryService {
    private static _instance: ExtensionRegistryService;
    private api: ExtensionRegistryAPI;
    private _apiUrl: string;
    private constructor() {
        this.api = new ExtensionRegistryAPI();
    }

    static get instance(): ExtensionRegistryService {
        if (!ExtensionRegistryService._instance) {
            ExtensionRegistryService._instance = new ExtensionRegistryService();
        }
        return ExtensionRegistryService._instance;
    }

    set apiUrl(url: string) {
        this._apiUrl = url;
    }

    get apiUrl(): string {
        return this._apiUrl;
    }

    async getExtensions(filter?: ExtensionFilter): Promise<ExtensionRaw[]> {
        let query: { key: string, value: string | number }[] | undefined;
        if (filter) {
            query = [];
            for (const key in filter) {
                if (filter[key]) {
                    const value = filter[key];
                    if (!!value) {
                        query.push({ key, value });
                    }
                }
            }
        }
        const endpoint = createURL([this._apiUrl, '-', 'search'], query);
        return this.api.getExtensions(endpoint);
    }

    async getExtensionDetail(extensionURL: string): Promise<Extension> {
        return this.api.getExtension(extensionURL);
    }

    async getExtensionReadMe(readMeUrl: string): Promise<string> {
        return this.api.getExtensionReadMe(readMeUrl);
    }

    async getExtensionReviews(reviewsUrl: string): Promise<ExtensionReviewList> {
        return this.api.getExtensionReviews(reviewsUrl);
    }

    async postReview(rating: ExtensionReview, postUrl: string): Promise<void> {
        await this.api.postReview(rating, postUrl);
    }

    async getUser(): Promise<ExtensionRegistryUser> {
        return this.api.getUser();
    }

    getCategories(): ExtensionCategory[] {
        return [
            'Programming Languages',
            'Snippets',
            'Linters',
            'Themes',
            'Debuggers',
            'Formatters',
            'Keymaps',
            'SCM Providers',
            'Other',
            'Extension Packs',
            'Language Packs'
        ];
    }
}