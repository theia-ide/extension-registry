/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/

import { ExtensionRegistryAPI } from "./extension-registry-api";
import { ExtensionFilter, Extension, ExtensionRating, ExtensionRegistryUser } from "./extension-registry-types";

export class ExtensionRegistryService {
    private static _instance: ExtensionRegistryService;
    private api: ExtensionRegistryAPI;
    private constructor() {
        this.api = new ExtensionRegistryAPI();
    }

    static get instance(): ExtensionRegistryService {
        if (!ExtensionRegistryService._instance) {
            ExtensionRegistryService._instance = new ExtensionRegistryService();
        }
        return ExtensionRegistryService._instance;
    }

    async getExtensions(filter?: ExtensionFilter): Promise<Extension[]> {
        return this.api.getExtensions(filter);
    }

    async getExtensionById(id: string): Promise<Extension | undefined> {
        return this.api.getExtension(id);
    }

    async postReview(rating: ExtensionRating): Promise<void> {
        await this.api.postReview(rating);
    }

    async getUser(): Promise<ExtensionRegistryUser> {
        return await this.api.getUser();
    }
}