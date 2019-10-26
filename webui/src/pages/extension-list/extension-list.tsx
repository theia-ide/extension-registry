/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/

import * as React from "react";
import { Grid } from "@material-ui/core";
import { ExtensionListItem } from "./extension-list-item";
import { ExtensionRegistryService } from "../../extension-registry-service";
import { ExtensionListHeader } from "./extension-list-header";
import { Extension } from "../../extension-registry-api";

export class ExtensionList extends React.Component<ExtensionList.Props, ExtensionList.State> {

    protected service: ExtensionRegistryService = ExtensionRegistryService.instance;

    constructor(props: ExtensionList.Props){
        super(props);
        this.state = {
            extensions: []
        }
    }

    componentDidMount(){
        this.service.getExtensions().then(extensions => this.setState({extensions}));
    }

    render() {
        const extensionList = this.state.extensions.map(ext => <ExtensionListItem extension={ext} key={ext.name} />);
        return <React.Fragment>
            <ExtensionListHeader />
            <Grid container spacing={2}>
                {extensionList}
            </Grid>
        </React.Fragment>;
    }
}

export namespace ExtensionList {
    export interface Props { }
    export interface State {
        extensions: Extension[]
    }
}