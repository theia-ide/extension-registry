/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/

import * as React from "react";
import Markdown from 'markdown-to-jsx';
import { Box } from "@material-ui/core";
import { ExtensionRaw } from "../../extension-registry-types";
import { ExtensionRegistryService } from "../../extension-registry-service";


export class ExtensionDetailOverview extends React.Component<ExtensionDetailOverview.Props, ExtensionDetailOverview.State> {

    constructor(props: ExtensionDetailOverview.Props) {
        super(props);

        this.state = {};
    }

    componentDidMount() {
        this.init();
    }

    protected async init() {
        const readMe = await this.props.service.getExtensionReadMe(this.props.extension);
        this.setState({ readMe });
    }

    render() {
        if(!this.state.readMe) {
            return '';
        }
        return <React.Fragment>
            <Box>
                <Markdown>
                    {this.state.readMe}
                </Markdown>
            </Box>
        </React.Fragment>;
    }
}

export namespace ExtensionDetailOverview {
    export interface Props {
        extension: ExtensionRaw,
        service: ExtensionRegistryService
    }
    export interface State {
        readMe?: string
    }
}
