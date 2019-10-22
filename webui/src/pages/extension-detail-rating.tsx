/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/

import * as React from "react";
import { Typography } from "@material-ui/core";
import { RouteComponentProps } from "react-router-dom";
import { ExtensionDetailParams } from "./extension-detail";

export class ExtensionDetailRating extends React.Component<RouteComponentProps> {

    render() {
        const detailParams = this.props.match.params as ExtensionDetailParams;
        return <React.Fragment>
            <Typography variant='h3'>ExtensionDetailRating for {detailParams.extid}</Typography>
        </React.Fragment>;
    }
}
