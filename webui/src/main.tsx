/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/

import * as React from 'react';
import { Container, AppBar, Toolbar, Typography, IconButton, InputBase } from '@material-ui/core';
import AccountBoxIcon from '@material-ui/icons/AccountBox';
import ExitToAppIcon from '@material-ui/icons/ExitToAppOutlined';
import SearchIcon from '@material-ui/icons/Search';
import { Route, Link, Switch } from 'react-router-dom';
import { ExtensionList } from './pages/extension-list';
import { UserProfile } from './pages/user-profile';
import { ExtensionDetailPages, ExtensionDetail } from './pages/extension-detail';

export namespace ExtensionRegistryPages {
    export const EXTENSION_LIST = '/extension-list';

    export const EXTENSION_REGISTRY = '/extension-registry';
    export const EXTENSION_UPDATE = '/extension-update';
    export const LOGIN = '/login';
    export const USER_PROFILE = '/user-profile';
    export const USER_REGISTRY = '/user-registry';
}

export class Main extends React.Component {

    render() {
        return <React.Fragment>
            <AppBar position='sticky'>
                <Toolbar>
                    <Link to={ExtensionRegistryPages.EXTENSION_LIST}><Typography variant='h6' noWrap>Theia Extension Registry</Typography></Link>
                    <Link to={ExtensionRegistryPages.USER_PROFILE}><IconButton><AccountBoxIcon /></IconButton></Link>
                    <IconButton><ExitToAppIcon /></IconButton>
                    <SearchIcon />
                    <InputBase placeholder='Search…' />
                </Toolbar>
            </AppBar>
            <Container maxWidth='md'>
                <Switch>
                    <Route exact path='/' component={ExtensionList} />
                    <Route path={ExtensionRegistryPages.EXTENSION_LIST} component={ExtensionList} />
                    <Route path={ExtensionRegistryPages.USER_PROFILE} component={UserProfile} />
                    <Route path={ExtensionDetailPages.EXTENSION_DETAIL} component={ExtensionDetail} />
                </Switch>
            </Container>
            <footer>
                <Container maxWidth='md'>
                    <Typography>footer stuff</Typography>
                </Container>
            </footer>
        </React.Fragment>;
    }
}
