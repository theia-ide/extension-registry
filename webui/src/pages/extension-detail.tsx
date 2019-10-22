/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/

import * as React from "react";
import { Typography, Box } from "@material-ui/core";
import { RouteComponentProps, Link, Switch, Route } from "react-router-dom";
import { ExtensionDetailOverview } from "./extension-detail-overview";
import { ExtensionDetailRating } from "./extension-detail-rating";

export namespace ExtensionDetailPages {
    export const EXTENSION_DETAIL_ROOT = '/extension-detail';
    export const EXTENSION_DETAIL = EXTENSION_DETAIL_ROOT + '/:extid';
    export const EXTENSION_DETAIL_OVERVIEW = '/overview';
    export const EXTENSION_DETAIL_RATING = '/rating';
}

export interface ExtensionDetailParams {
    extid: string;
}

export class ExtensionDetail extends React.Component<RouteComponentProps> {

    render() {
        const detailParams = this.props.match.params as ExtensionDetailParams;

        return <React.Fragment>
            <Typography variant='h3'>ExtensionDetail</Typography>
            <Typography variant='h5'>{detailParams.extid}</Typography>
            <Link to={ExtensionDetailPages.EXTENSION_DETAIL_ROOT + '/' + detailParams.extid + ExtensionDetailPages.EXTENSION_DETAIL_OVERVIEW}>Overview</Link> | 
            <Link to={ExtensionDetailPages.EXTENSION_DETAIL_ROOT + '/' + detailParams.extid + ExtensionDetailPages.EXTENSION_DETAIL_RATING}>Rating &amp; Review</Link>
            <Box>
                <Switch>
                    <Route path={ExtensionDetailPages.EXTENSION_DETAIL + ExtensionDetailPages.EXTENSION_DETAIL_OVERVIEW} component={ExtensionDetailOverview} />
                    <Route path={ExtensionDetailPages.EXTENSION_DETAIL + ExtensionDetailPages.EXTENSION_DETAIL_RATING} component={ExtensionDetailRating} />
                </Switch>
            </Box>
        </React.Fragment>;

    }
}
