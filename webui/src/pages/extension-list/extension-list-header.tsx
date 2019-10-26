/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/

import React = require("react");
import { Typography, Box, WithStyles, createStyles, Theme, withStyles, Paper, InputBase, IconButton } from "@material-ui/core";
import SearchIcon from '@material-ui/icons/Search';

const headerStyles = (theme: Theme) => createStyles({
    paper: {
        width: '50%',
        display: 'flex'
    },
    inputBase: {
        flex: 1,
        paddingLeft: theme.spacing(1)
    },
    iconButton: {
        backgroundColor: theme.palette.secondary.main,
        borderRadius: '0 4px 4px 0',
        padding: theme.spacing(1)
    },
    typo: {
        marginBottom: theme.spacing(2),
        fontWeight: theme.typography.fontWeightLight,
        letterSpacing: 4
    }
});

interface ExtensionListHeaderItemProps extends WithStyles<typeof headerStyles> { }

class ExtensionListHeaderComp extends React.Component<ExtensionListHeaderItemProps> {

    render() {
        const { classes } = this.props;
        return <React.Fragment>
            <Box display='flex' flexDirection='column' alignItems='center' py={6}>
                <Typography variant='h4' classes={{root: classes.typo}}>
                    Extensions for the Eclipse theia IDE
                </Typography>
                <Paper className={classes.paper}>
                    <InputBase className={classes.inputBase}></InputBase>
                    <IconButton color='primary' classes={{root: classes.iconButton}}>
                        <SearchIcon />
                    </IconButton>
                </Paper>
            </Box>
        </React.Fragment>;
    }
}

export const ExtensionListHeader = withStyles(headerStyles)(ExtensionListHeaderComp);