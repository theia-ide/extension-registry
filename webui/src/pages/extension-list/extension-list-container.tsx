/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/

import * as React from "react";
import { Container } from "@material-ui/core";
import { ExtensionListHeader } from "./extension-list-header";
import { ExtensionCategory } from "../../extension-registry-types";
import { ExtensionList } from "./extension-list";
import { ExtensionRegistryService } from "../../extension-registry-service";


export class ExtensionListContainer extends React.Component<ExtensionListContainer.Props, ExtensionListContainer.State> {

    constructor(props: ExtensionListContainer.Props) {
        super(props);
        this.state = {
            searchTerm: '',
            category: ''
        };
    }

    protected onSearchChanged = async (searchTerm: string) => {
        this.setState({ searchTerm });
    };
    protected onCategoryChanged = async (category: ExtensionCategory) => {
        this.setState({ category });
    };

    render() {
        return <React.Fragment>
            <Container>
                <ExtensionListHeader onSearchChanged={this.onSearchChanged} onCategoryChanged={this.onCategoryChanged} />
                <ExtensionList service={this.props.service} filter={{fullText: this.state.searchTerm, category: this.state.category}} />
            </Container>
        </React.Fragment>;
    }
}

export namespace ExtensionListContainer {
    export interface Props {
        service: ExtensionRegistryService
    }
    export interface State {
        searchTerm: string,
        category: ExtensionCategory
    }
}