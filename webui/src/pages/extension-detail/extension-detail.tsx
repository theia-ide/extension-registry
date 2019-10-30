/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/

import * as React from "react";
import { Typography, Box, createStyles, Theme, WithStyles, withStyles, Button, Container } from "@material-ui/core";
import { RouteComponentProps, Switch, Route } from "react-router-dom";
import { ExtensionDetailOverview } from "../extension-detail/extension-detail-overview";
import { ExtensionDetailRating } from "./extension-detail-rating";
import { ExtensionRegistryService } from "../../extension-registry-service";
import { Extension, ExtensionRegistryUser } from "../../extension-registry-types";
import { TextDivider } from "../../custom-mui-components/text-divider";
import { ExtensionDetailTabs } from "./extension-detail-tabs";
import { ExportRatingStars } from "./extension-rating-stars";

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
    row: {
        marginBottom: theme.spacing(1)
    },
    head: {
        backgroundColor: theme.palette.grey[200]
    },
    alignVertically: {
        display: 'flex',
        alignItems: 'center'
    }
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
        this.init();
    }

    protected async init() {
        const extension = await this.service.getExtensionById(this.params.extid);
        const user = await this.service.getUser();
        this.setState({ extension, user });
    }

    render() {
        if (!this.state.extension) {
            return '';
        }
        const { extension } = this.state;
        const rating = extension.ratings.map(r => r.rating as number).reduce((prev, curr) => prev + curr) / extension.ratings.length;
        return <React.Fragment>
            <Box className={this.props.classes.head}>
                <Container>
                    <Box display='flex' py={4}>
                        <Box display='flex' justifyContent='center' alignItems='center' mr={4}>
                            <img src={extension.icon} width='auto' height='120px' />
                        </Box>
                        <Box>
                            <Typography variant='h6' className={this.props.classes.row}>{extension.name}</Typography>
                            <Box display='flex' className={this.props.classes.row}>
                                <Box className={this.props.classes.alignVertically}>{extension.author}</Box>
                                <TextDivider />
                                <Box className={this.props.classes.alignVertically}><ExportRatingStars number={rating} /></Box>
                                <TextDivider />
                                <Box className={this.props.classes.alignVertically}>{extension.license}</Box>
                            </Box>
                            <Box className={this.props.classes.row}>{extension.description}</Box>
                            <Box className={this.props.classes.row}>
                                <Button variant='contained' color='secondary'>
                                    Install
                                </Button>
                            </Box>
                        </Box>
                    </Box>
                </Container>
            </Box>
            <Container>
                <Box>
                    <Box>
                        <ExtensionDetailTabs extid={this.params.extid} history={this.props.history} location={this.props.location} match={this.props.match} />
                    </Box>
                    <Box>
                        <Switch>
                            <Route path={ExtensionDetailPages.EXTENSION_DETAIL + ExtensionDetailPages.EXTENSION_DETAIL_OVERVIEW}>
                                <ExtensionDetailOverview longDescription={this.state.extension.longDescription} />
                            </Route>
                            <Route path={ExtensionDetailPages.EXTENSION_DETAIL + ExtensionDetailPages.EXTENSION_DETAIL_RATING}>
                                <ExtensionDetailRating ratings={this.state.extension.ratings} extension={this.state.extension} user={this.state.user} />
                            </Route>
                        </Switch>
                    </Box>
                </Box>
            </Container>
        </React.Fragment>;

    }
}

export namespace ExtensionDetailComponent {
    export interface Props extends WithStyles<typeof detailStyles>, RouteComponentProps { }
    export interface State {
        extension?: Extension,
        user?: ExtensionRegistryUser
    }
}

export const ExtensionDetail = withStyles(detailStyles)(ExtensionDetailComponent);
