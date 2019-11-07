/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/

import { ExtensionRegistryAPI } from "./extension-registry-api";
import { ExtensionFilter, Extension, ExtensionReview, ExtensionRegistryUser, ExtensionCategory, ExtensionRaw } from "./extension-registry-types";

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

    async getExtensions(filter?: ExtensionFilter): Promise<Extension[]> {
        return this.api.getExtensions(filter);
    }

    async getExtensionDetail(extension: ExtensionRaw): Promise<Extension> {
        return this.api.getExtension(extension, this._apiUrl);
    }

    async getExtensionReadMe(extension: ExtensionRaw): Promise<string> {
        return this.api.getExtensionReadMe(extension, this._apiUrl);
    }

    async getExtensionReviews(extension: ExtensionRaw): Promise<ExtensionReview[]> {
        return this.api.getExtensionReviews(extension, this._apiUrl);
    }

    async postReview(rating: ExtensionReview): Promise<void> {
        await this.api.postReview(rating);
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