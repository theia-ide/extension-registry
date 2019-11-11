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
import { ExtensionDetailReviews } from "./extension-detail-reviews";
import { ExtensionRegistryService } from "../../extension-registry-service";
import { Extension, ExtensionRegistryUser, ExtensionRaw } from "../../extension-registry-types";
import { TextDivider } from "../../custom-mui-components/text-divider";
import { ExtensionDetailTabs } from "./extension-detail-tabs";
import { ExportRatingStars } from "./extension-rating-stars";

export namespace ExtensionDetailRoutes {
    export const ROOT = '/extension-detail';
    export const TAB = '/:tab';
    export const PARAMS = '/:publisher/:name';
    export const OVERVIEW = 'overview';
    export const RATING = 'rating';
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
    protected params: Extension;

    constructor(props: ExtensionDetailComponent.Props) {
        super(props);

        this.state = {};
        this.params = this.props.match.params as Extension;
    }

    componentDidMount() {
        this.init();
    }

    protected async init() {
        const extensionUrl = ExtensionRaw.getExtensionApiUrl(this.service.apiUrl, this.params);
        const extension = await this.service.getExtensionDetail(extensionUrl);
        const user = await this.service.getUser();
        this.setState({ extension, user });
    }

    protected onReviewUpdate = () => this.init();

    render() {
        if (!this.state.extension) {
            return '';
        }
        const { extension } = this.state;
        return <React.Fragment>
            <Box className={this.props.classes.head}>
                <Container>
                    <Box display='flex' py={4}>
                        <Box display='flex' justifyContent='center' alignItems='center' mr={4}>
                            <img src={extension.iconUrl} width='auto' height='120px' />
                        </Box>
                        <Box>
                            <Typography variant='h6' className={this.props.classes.row}>{extension.displayName || extension.name}</Typography>
                            <Box display='flex' className={this.props.classes.row}>
                                <Box className={this.props.classes.alignVertically}>{extension.publisher}</Box>
                                <TextDivider />
                                <Box className={this.props.classes.alignVertically}><ExportRatingStars number={extension.averageRating || 0} /></Box>
                                <TextDivider />
                                <Box className={this.props.classes.alignVertically}>{extension.license}</Box>
                            </Box>
                            <Box className={this.props.classes.row}>{extension.description}</Box>
                            <Box className={this.props.classes.row}>
                                <Button variant='contained' color='secondary' href={extension.downloadUrl}>
                                    Download
                                </Button>
                            </Box>
                        </Box>
                    </Box>
                </Container>
            </Box>
            <Container>
                <Box>
                    <Box>
                        <ExtensionDetailTabs history={this.props.history} location={this.props.location} match={this.props.match} />
                    </Box>
                    <Box>
                        <Switch>
                            <Route path={ExtensionDetailRoutes.ROOT + '/' + ExtensionDetailRoutes.OVERVIEW + ExtensionDetailRoutes.PARAMS}>
                                <ExtensionDetailOverview extension={this.state.extension} service={this.props.service} />
                            </Route>
                            <Route path={ExtensionDetailRoutes.ROOT + '/' + ExtensionDetailRoutes.RATING + ExtensionDetailRoutes.PARAMS}>
                                <ExtensionDetailReviews extension={this.state.extension} reviewsDidUpdate={this.onReviewUpdate} service={this.props.service} user={this.state.user} />
                            </Route>
                        </Switch>
                    </Box>
                </Box>
            </Container>
        </React.Fragment>;

    }
}

export namespace ExtensionDetailComponent {
    export interface Props extends WithStyles<typeof detailStyles>, RouteComponentProps {
        service: ExtensionRegistryService
    }
    export interface State {
        extension?: Extension,
        user?: ExtensionRegistryUser
    }
}

export const ExtensionDetail = withStyles(detailStyles)(ExtensionDetailComponent);
