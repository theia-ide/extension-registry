/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/

export type ExtensionRegistryTokenStore = { [key: string]: string };
export let tokenStore: ExtensionRegistryTokenStore = {};

export class MockTokenAPI {

    protected count = 0;

    async getTokens(): Promise<ExtensionRegistryTokenStore> {
        return tokenStore;
    }

    async generateToken(): Promise<void> {
        tokenStore['t' + this.count] = `Token ${this.count}: Some token which was generated for some app`;
        this.count++;
    }

    async deleteToken(tokenId: string): Promise<void> {
        delete tokenStore[tokenId];
    }

    async deleteTokens(): Promise<void> {
        tokenStore = {};
    }
}