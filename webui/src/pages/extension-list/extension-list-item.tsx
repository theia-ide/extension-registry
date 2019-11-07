/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/

import React = require("react");
import { Link } from "react-router-dom";
import { ExtensionDetailRoutes } from "../extension-detail/extension-detail";
import { Paper, Typography, Box, Grid, Fade } from "@material-ui/core";
import { withStyles, createStyles, WithStyles, Theme } from '@material-ui/core/styles';
import { ExtensionRaw } from "../../extension-registry-types";
import { ExportRatingStars } from "../extension-detail/extension-rating-stars";
import { ExtensionRegistryService } from "../../extension-registry-service";


const itemStyles = (theme: Theme) => createStyles({
    paper: {
        padding: theme.spacing(3, 2)
    },
    link: {
        textDecoration: 'none'
    }
});

interface ExtensionListItemProps extends WithStyles<typeof itemStyles> {
    service: ExtensionRegistryService;
    extension: ExtensionRaw;
    idx: number;
}

class ExtensionListItemComp extends React.Component<ExtensionListItemProps> {
    render() {
        const { classes, extension } = this.props;
        const versionURLPart = extension.version ? '/' + extension.version : '';
        const route = ExtensionDetailRoutes.ROOT + '/' + ExtensionDetailRoutes.OVERVIEW + '/' + extension.publisher + '/' + extension.name + versionURLPart;
        const imgURL = extension.iconFileName ?
            ExtensionRaw.getExtensionApiUrl(this.props.service.apiUrl, extension) + '/file/' + extension.iconFileName :
            '';
        return <React.Fragment>
            <Fade in={true} timeout={{ enter: this.props.idx * 200 }}>
                <Grid item xs={12} sm={3} md={2}>
                    <Link to={route} className={classes.link}>
                        <Paper className={classes.paper}>
                            <Box display='flex' justifyContent='center'>
                                <img src={imgURL} />
                            </Box>
                            <Box display='flex' justifyContent='center'><Typography variant='h6'>{extension.name}</Typography></Box>
                            <Box display='flex' justifyContent='space-between'>
                                <Typography component='div' variant='caption' noWrap={true} align='left'>
                                    {extension.publisher}
                                </Typography>
                                <Typography component='div' variant='caption' noWrap={true} align='right'>
                                    {extension.version}
                                </Typography>
                            </Box>
                            <Box>
                                <ExportRatingStars number={this.props.extension.averageRating || 0} />
                            </Box>
                        </Paper>
                    </Link>
                </Grid>
            </Fade>
        </React.Fragment>;
    }
}

export const ExtensionListItem = withStyles(itemStyles)(ExtensionListItemComp);