/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/

import * as React from "react";
import Markdown from 'markdown-to-jsx';


export class ExtensionDetailOverview extends React.Component<ExtensionDetailOverview.Props> {

    render() {
        return <React.Fragment>
                <Markdown>
                    {this.props.longDescription}
                </Markdown>
        </React.Fragment>;
    }
}

export namespace ExtensionDetailOverview {
    export interface Props {
        longDescription: string
    }
}
