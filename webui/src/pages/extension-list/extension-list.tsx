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
import { Extension, ExtensionFilter } from "../../extension-registry-types";
import { ExtensionRegistryService } from "../../extension-registry-service";

export class ExtensionList extends React.Component<ExtensionList.Props, ExtensionList.State> {

    protected service: ExtensionRegistryService = ExtensionRegistryService.instance;

    protected extensions: Extension[];

    constructor(props: ExtensionList.Props) {
        super(props);

        this.state = {
            extensions: []
        }
    }

    componentDidMount() {
        this.service.getExtensions(this.props.filter).then(extensions => this.setState({ extensions }));
    }

    componentDidUpdate(prevProps: ExtensionList.Props, prevState: ExtensionList.State) {
        const prevFilter = prevProps.filter;
        const newFilter = this.props.filter;
        if(prevFilter.category !== newFilter.category || prevFilter.fullText !== newFilter.fullText) {
            this.service.getExtensions(newFilter).then(extensions => this.setState({extensions}));
        }
    }

    render() {
        const extensionList = this.state.extensions.map((ext, idx) => {
            const rating = ext.ratings.map(r => r.rating as number).reduce((prev, curr) => prev + curr) / ext.ratings.length;
            return <ExtensionListItem idx={idx} extension={ext} key={ext.name} rating={rating} />;
        });
        return <Grid container spacing={2}>
            {extensionList}
        </Grid>
    }
}

export namespace ExtensionList {
    export interface Props {
        filter: ExtensionFilter
    }
    export interface State {
        extensions: Extension[]
    }
}

