/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/

import { Extension, ExtensionRegistryUser, ExtensionReview, ExtensionRaw, ExtensionReviewList } from "./extension-registry-types";

const user: ExtensionRegistryUser = {
    firstName: 'Friendly',
    lastName: 'User',
    userName: 'userfriendly',
    email: 'userfriendly@test.ie'
};

export interface ExtensionRegistryAPIRequest<T> {
    endpoint: string,
    operation: (response: Response) => Promise<T>
}

export interface ExtensionRegistryAPIRequestWithoutPayload<T> extends ExtensionRegistryAPIRequest<T> {
    method: 'GET' | 'DELETE'
}

export interface ExtensionRegistryAPIRequestWithPayload<T> extends ExtensionRegistryAPIRequest<T> {
    method: 'POST' | 'PUT',
    payload: any,
    contentType: string
}

export class ExtensionRegistryAPI {

    async run<T>(req: ExtensionRegistryAPIRequestWithPayload<T> | ExtensionRegistryAPIRequestWithoutPayload<T>): Promise<T> {
        const headers: { [key: string]: any } = { 'Content-Type': 'application/json' };
        const param: { [key: string]: any } = {
            method: req.method,
            credentials: 'include',
            headers
        }

        if (req.method === 'POST' || req.method === 'PUT') {
            param.body = JSON.stringify(req.payload);
            param.headers['Accept'] = req.contentType;
        }

        const response = await fetch(req.endpoint, param);

        return await req.operation(response);
    }

    async getExtensions(endpoint: string): Promise<ExtensionRaw[]> {
        const extensions = await this.run<ExtensionRaw[]>({
            method: 'GET',
            endpoint,
            operation: async response => {
                const resp = await response.json() as { offset: number; extensions: ExtensionRaw[] };
                return resp.extensions;
            }
        });
        return extensions;
    }

    async getExtension(endpoint: string): Promise<Extension> {
        const ext = await this.run<Extension>({
            method: 'GET',
            endpoint,
            operation: async response => await response.json()
        });
        return ext;
    }

    async getExtensionReadMe(endpoint: string): Promise<string> {
        const readme = await this.run<string>({
            method: 'GET',
            endpoint,
            operation: async response => await response.text()
        });
        return readme;
    }

    async getExtensionReviews(endpoint: string): Promise<ExtensionReviewList> {
        const reviews = await this.run<ExtensionReviewList>({
            method: 'GET',
            endpoint,
            operation: async response => await response.json()
        });
        return reviews;
    }

    async postReview(payload: ExtensionReview, endpoint: string) {
        await this.run({
            method: 'POST',
            payload,
            contentType: 'application/json;charset=UTF-8',
            endpoint,
            operation: async response => await response.json()
        });
    }

    async getUser(): Promise<ExtensionRegistryUser> {
        return user;
    }
}