/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/

import { ExtensionFilter, Extension, ExtensionRegistryUser, ExtensionReview, ExtensionRaw, ExtensionReviewList } from "./extension-registry-types";

const user: ExtensionRegistryUser = {
    firstName: 'Friendly',
    lastName: 'User',
    userName: 'userfriendly',
    email: 'userfriendly@test.ie'
};

const extArr: ExtensionRaw[] = [
    {
        name: 'python',
        publisher: 'ms-python',
        version: '2019.10.44104',
        averageRating: 3.4,
        iconUrl: 'https://8080-c1b8f7e4-95a3-4d7e-9168-51b9530254eb.ws-eu01.gitpod.io/api/ms-python/python/file/icon.png',
        displayName: 'Python',
        extensionUrl: 'https://8080-c1b8f7e4-95a3-4d7e-9168-51b9530254eb.ws-eu01.gitpod.io/api/ms-python/python'
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
    payload: any,
    contentType: string
}

export class ExtensionRegistryAPI {

    async run<T>(req: ExtensionRegistryAPIRequestWithPayload<T> | ExtensionRegistryAPIRequestWithoutPayload<T>): Promise<T> {
        let payload: string;
        let contentType: string;

        if (req.method === 'POST' || req.method === 'PUT') {
            payload = JSON.stringify(req.payload);
            contentType = req.contentType;
        }

        const response = await new Promise<string>((resolve, reject) => {
            const request = new XMLHttpRequest();
            request.open(req.method, req.endpoint);
            if (contentType) {
                request.setRequestHeader("Content-Type", contentType);
            }
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

    async getExtensions(filter?: ExtensionFilter): Promise<ExtensionRaw[]> {

        return extArr;
    }

    async getExtension(endpoint: string): Promise<Extension> {
        const ext = await this.run<Extension>({
            method: 'GET',
            endpoint,
            operation: response => JSON.parse(response)
        });
        return ext;
    }

    async getExtensionReadMe(endpoint: string): Promise<string> {
        const readme = await this.run<string>({
            method: 'GET',
            endpoint,
            operation: json => json
        });
        return readme;
    }

    async getExtensionReviews(endpoint: string): Promise<ExtensionReviewList> {
        const reviews = await this.run<ExtensionReviewList>({
            method: 'GET',
            endpoint,
            operation: response => JSON.parse(response)
        });
        return reviews;
    }

    async postReview(payload: ExtensionReview, endpoint: string) {
        await this.run({
            method: 'POST',
            payload,
            contentType: 'application/json;charset=UTF-8',
            endpoint,
            operation: response => response
        });
    }

    async getUser(): Promise<ExtensionRegistryUser> {
        return user;
    }
}