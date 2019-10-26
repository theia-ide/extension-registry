/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/

import React = require("react");
import { Link } from "react-router-dom";
import { ExtensionDetailPages } from "../extension-detail/extension-detail";
import { Paper, Typography, Box, Grid } from "@material-ui/core";
import { withStyles, createStyles, WithStyles, Theme } from '@material-ui/core/styles';
import { Extension } from "../../extension-registry-api";


const itemStyles = (theme: Theme) => createStyles({
    paper: {
        padding: theme.spacing(3, 2)
    },
    link: {
        textDecoration: 'none'
    }
});

interface ExtensionListItemProps extends WithStyles<typeof itemStyles> {
    extension: Extension
}

class ExtensionListItemComp extends React.Component<ExtensionListItemProps> {
    render() {
        const { classes, extension } = this.props;
        return <React.Fragment>
            <Grid item xs={12} sm={3} md={2}>
                <Link to={ExtensionDetailPages.EXTENSION_DETAIL_ROOT + '/' + extension.name} className={classes.link}>
                    <Paper className={classes.paper}>
                        <Box display='flex' justifyContent='center'>
                            <img src='test.png' />
                        </Box>
                        <Box display='flex' justifyContent='center'><Typography variant='h6'>{extension.name}</Typography></Box>
                        <Box display='flex' justifyContent='space-between'>
                            <Typography component='div' variant='caption' noWrap={true} align='left'>
                                {extension.author}
                            </Typography>
                            <Typography component='div' variant='caption' noWrap={true} align='right'>
                                {extension.version}
                            </Typography>
                        </Box>
                    </Paper>
                </Link>
            </Grid>
        </React.Fragment>;
    }
}

export const ExtensionListItem = withStyles(itemStyles)(ExtensionListItemComp);