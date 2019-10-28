/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/

import * as React from "react";
import { Tabs, Tab } from "@material-ui/core";
import { ExtensionDetailPages } from "./extension-detail";
import { RouteComponentProps } from "react-router-dom";

export class ExtensionDetailTabs extends React.Component<ExtensionDetailTabs.Props, ExtensionDetailTabs.State> {

    constructor(props: ExtensionDetailTabs.Props) {
        super(props);

        this.state = { tab: ExtensionDetailPages.EXTENSION_DETAIL_OVERVIEW };
    }

    componentDidMount() {
        this.props.history.push(ExtensionDetailPages.EXTENSION_DETAIL_ROOT + '/' + this.props.extid + ExtensionDetailPages.EXTENSION_DETAIL_OVERVIEW);
    }

    protected handleChange = (event: React.ChangeEvent, newTab: string) => {
        this.props.history.push(ExtensionDetailPages.EXTENSION_DETAIL_ROOT + '/' + this.props.extid + newTab);
        this.setState({tab: newTab});
    }

    render() {
        return <React.Fragment>
            <Tabs value={this.state.tab} onChange={this.handleChange}>
                <Tab value={ExtensionDetailPages.EXTENSION_DETAIL_OVERVIEW} label='Overview' />
                <Tab value={ExtensionDetailPages.EXTENSION_DETAIL_RATING} label='Rating &amp; Review' />
            </Tabs>
        </React.Fragment>;
    }
}

export namespace ExtensionDetailTabs {
    export interface Props extends RouteComponentProps {
        extid: string
    }
    export interface State {
        tab: string
    }
}
