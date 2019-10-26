/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/

import * as React from "react";
import { Typography, Box, Divider, createStyles, Theme, WithStyles, withStyles } from "@material-ui/core";
import { RouteComponentProps, Link, Switch, Route } from "react-router-dom";
import { ExtensionDetailOverview } from "../extension-detail/extension-detail-overview";
import { ExtensionDetailRating } from "./extension-detail-rating";
import { ExtensionRegistryService } from "../../extension-registry-service";
import { Extension } from "../../extension-registry-api";

export namespace ExtensionDetailPages {
    export const EXTENSION_DETAIL_ROOT = '/extension-detail';
    export const EXTENSION_DETAIL = EXTENSION_DETAIL_ROOT + '/:extid';
    export const EXTENSION_DETAIL_OVERVIEW = '/overview';
    export const EXTENSION_DETAIL_RATING = '/rating';
}

export interface ExtensionDetailParams {
    extid: string;
}

const detailStyles = (theme: Theme) => createStyles({

});

export class ExtensionDetailComponent extends React.Component<ExtensionDetailComponent.Props, ExtensionDetailComponent.State> {
    protected service = ExtensionRegistryService.instance;
    protected params: ExtensionDetailParams;

    constructor(props: ExtensionDetailComponent.Props) {
        super(props);

        this.state = {};
        this.params = this.props.match.params as ExtensionDetailParams;
    }

    componentDidMount() {
        this.service.getExtensionById(this.params.extid).then(extension => this.setState({ extension }));
    }

    render() {
        if (!this.state.extension) {
            return '';
        }
        const { extension } = this.state;
        return <React.Fragment>
            <Box display='flex'>
                <Box display='flex' justifyContent='center'>
                    <img src='test.png' />
                </Box>
                <Box>
                    <Typography variant='h4'>{extension.name}</Typography>
                    <Box>{extension.author}</Box>
                    <Box>{extension.description}</Box>
                    <Box>{extension.license}</Box>
                </Box>
            </Box>
            <Box>
                <Box>
                    <Link to={ExtensionDetailPages.EXTENSION_DETAIL_ROOT + '/' + this.params.extid + ExtensionDetailPages.EXTENSION_DETAIL_OVERVIEW}>Overview</Link>
                    <Divider orientation='vertical' />
                    <Link to={ExtensionDetailPages.EXTENSION_DETAIL_ROOT + '/' + this.params.extid + ExtensionDetailPages.EXTENSION_DETAIL_RATING}>Rating &amp; Review</Link>
                </Box>
                <Box>
                    <Switch>
                        <Route path={ExtensionDetailPages.EXTENSION_DETAIL + ExtensionDetailPages.EXTENSION_DETAIL_OVERVIEW}>
                            <ExtensionDetailOverview {...this.params} />
                        </Route>
                        <Route path={ExtensionDetailPages.EXTENSION_DETAIL + ExtensionDetailPages.EXTENSION_DETAIL_RATING}
                            render={props => <ExtensionDetailRating {...props} />} />
                    </Switch>
                </Box>
            </Box>
        </React.Fragment>;

    }
}

export namespace ExtensionDetailComponent {
    export interface Props extends WithStyles<typeof detailStyles>, RouteComponentProps { };
    export interface State {
        extension?: Extension
    };
}

export const ExtensionDetail = withStyles(detailStyles)(ExtensionDetailComponent);
