/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/

import * as React from 'react';
import { Container, AppBar, Toolbar, Typography, IconButton, CssBaseline, Box, Theme } from '@material-ui/core';
import AccountBoxIcon from '@material-ui/icons/AccountBox';
import ExitToAppIcon from '@material-ui/icons/ExitToAppOutlined';
import { Route, Link, Switch } from 'react-router-dom';
import { ExtensionListContainer } from './pages/extension-list/extension-list-container';
import { UserProfile } from './pages/user-profile';
import { ExtensionDetailRoutes, ExtensionDetail } from './pages/extension-detail/extension-detail';
import { WithStyles, createStyles, withStyles } from '@material-ui/styles';
import { ExtensionRegistryService } from './extension-registry-service';

export namespace ExtensionRegistryPages {
    export const EXTENSION_LIST = '/extension-list';

    export const EXTENSION_REGISTRY = '/extension-registry';
    export const EXTENSION_UPDATE = '/extension-update';
    export const LOGIN = '/login';
    export const USER_PROFILE = '/user-profile';
    export const USER_REGISTRY = '/user-registry';
}

const mainStyles = (theme: Theme) => createStyles({
    link: {
        textDecoration: 'none',
        color: theme.palette.text.primary
    },
    toolbar: {
        justifyContent: 'space-between'
    }
});

interface ExtensionRegistryMainProps extends WithStyles<typeof mainStyles> {
    apiUrl: string;
    pageTitle: string;
    listHeaderTitle: string;
    logoURL: string;
}

class MainComponent extends React.Component<ExtensionRegistryMainProps> {

    protected service: ExtensionRegistryService;

    constructor(props: ExtensionRegistryMainProps) {
        super(props);

        this.service = ExtensionRegistryService.instance;
        this.service.apiUrl = props.apiUrl;
    }

    render() {
        return <React.Fragment>
            <CssBaseline />
            <Box display='flex' flexDirection='column' minHeight='100vh'>
                <AppBar position='sticky'>
                    <Toolbar classes={{ root: this.props.classes.toolbar }}>
                        <Box>
                            <Link to={ExtensionRegistryPages.EXTENSION_LIST} className={this.props.classes.link}>
                                <Box display='flex'>
                                    <Box width={120} display='flex' alignItems='center' marginRight={1}>
                                        <img src={this.props.logoURL} width='100%' />
                                    </Box>
                                    <Typography variant='h6' noWrap>{this.props.pageTitle}</Typography>
                                </Box>
                            </Link>
                        </Box>
                        <Box>
                            <Link to={ExtensionRegistryPages.USER_PROFILE}>
                                <IconButton>
                                    <AccountBoxIcon />
                                </IconButton>
                            </Link>
                            <IconButton>
                                <ExitToAppIcon />
                            </IconButton>
                        </Box>
                    </Toolbar>
                </AppBar>
                <Box flex='1'>
                    <Switch>
                        <Route exact path={['/', ExtensionRegistryPages.EXTENSION_LIST]}>
                            <ExtensionListContainer service={this.service} listHeaderTitle={this.props.listHeaderTitle} />
                        </Route>
                        <Route path={ExtensionRegistryPages.USER_PROFILE} component={UserProfile} />
                        <Route path={ExtensionDetailRoutes.ROOT + ExtensionDetailRoutes.TAB + ExtensionDetailRoutes.PARAMS}
                            render={routeProps => <ExtensionDetail {...routeProps} service={this.service} />} />
                    </Switch>
                </Box>
                <footer>
                    <Container>
                        <Typography>footer stuff</Typography>
                    </Container>
                </footer>
            </Box>
        </React.Fragment>;
    }
}

export const Main = withStyles(mainStyles)(MainComponent);
