/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/

import * as React from "react";
import { Box } from "@material-ui/core";
import { ExtensionRegistryService } from "../../extension-registry-service";
import { Extension } from "../../extension-registry-types";
import * as MarkdownIt from 'markdown-it';


export class ExtensionDetailOverview extends React.Component<ExtensionDetailOverview.Props, ExtensionDetailOverview.State> {

    protected markdownIt: MarkdownIt;

    constructor(props: ExtensionDetailOverview.Props) {
        super(props);
        this.markdownIt = new MarkdownIt('commonmark');
        this.state = {};
    }

    componentDidMount() {
        this.init();
    }

    protected async init() {
        if (this.props.extension.readmeUrl) {
            const readMe = await this.props.service.getExtensionReadMe(this.props.extension.readmeUrl);
            this.setState({ readMe });
        } else {
            this.setState({ readMe: '## No README available' });
        }
    }

    render() {
        if (!this.state.readMe) {
            return '';
        }
        return <React.Fragment>
            <Box>
                {this.renderMarkdown(this.state.readMe)}
            </Box>
        </React.Fragment>;
    }

    protected renderMarkdown(md: string) {
        const renderedMd = this.markdownIt.render(md);
        return <span dangerouslySetInnerHTML={{ __html: renderedMd }} />;
    }
}

export namespace ExtensionDetailOverview {
    export interface Props {
        extension: Extension,
        service: ExtensionRegistryService
    }
    export interface State {
        readMe?: string
    }
}
