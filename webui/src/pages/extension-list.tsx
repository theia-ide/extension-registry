/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/

import * as React from "react";
import { Typography, Grid, Paper, Box } from "@material-ui/core";
import CloudDownloadIcon from '@material-ui/icons/CloudDownload';
import { Link } from "react-router-dom";
import { ExtensionDetailPages } from "./extension-detail";

export class ExtensionList extends React.Component {

    protected testList = ['eins', 'zwei', 'drei', 'vier', 'fuenf', 'sechs', 'sieben', 'acht', 'neun', 'zehn'];

    render() {
        const extensionList = this.testList.map(ext =>
            <Grid item xs={12} sm={3} md={2} key={ext}>
                <Link to={ExtensionDetailPages.EXTENSION_DETAIL_ROOT + '/' + ext}>
                    <Paper>
                        <img src='test.png' />
                        <Typography variant='h6'>Title</Typography>
                        <Box>
                            <Typography variant='body1'>
                                Author Name
                            </Typography>
                            <Typography variant='body1'>
                                <CloudDownloadIcon /> 123K
                            </Typography>
                        </Box>
                    </Paper>
                </Link>
            </Grid>
        );

        return <React.Fragment>
            <Typography variant='h3'>ExtensionList</Typography>
            <Grid container spacing={2}>
                {extensionList}
            </Grid>
        </React.Fragment>;
    }
}
