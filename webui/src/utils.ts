/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/

export function createURL(arr: string[], queries?: {key: string, value: string | number}[]): string {
    const url = arr.reduce((acc, curr) => acc + (curr ? '/' + curr : ''));
    const queryString = queries ? '?' + queries.map<string>((obj) => obj.key + '=' + obj.value).join('&') : '';
    return url + queryString;
}