/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/

import * as React from "react";
import { Theme, createStyles, WithStyles, withStyles, Typography, Box, Paper, Button } from "@material-ui/core";
import { ExtensionRegistryUser } from "../../extension-registry-types";
import { ExtensionRegistryService } from "../../extension-registry-service";

const tokensStyle = (theme: Theme) => createStyles({
    boldText: {
        fontWeight: 'bold'
    },
    deleteBtn: {
        color: theme.palette.error.main
    }
});

class UserSettingsTokensComponent extends React.Component<UserSettingsTokensComponent.Props, UserSettingsTokensComponent.State> {

    constructor(props: UserSettingsTokensComponent.Props) {
        super(props);

        this.state = {};
    }



    render() {
        return <React.Fragment>
            <Box display='flex' justifyContent='space-between'>
                <Box>
                    <Typography variant='h5' gutterBottom>Tokens</Typography>
                </Box>
                <Box display='flex'>
                    <Box mr={1}>
                        <Button variant='outlined'>Generate new token</Button>
                    </Box>
                    <Box>
                        <Button variant='outlined' classes={{ root: this.props.classes.deleteBtn }}>Delete all</Button>
                    </Box>
                </Box>
            </Box>
            <Box my={2}>
                <Typography variant='body1'>Tokens you have generated.</Typography>
            </Box>
            <Box>
                <Paper>
                    <Box p={2} display='flex' justifyContent='space-between'>
                        <Box display='flex' alignItems='center'>
                            <Typography classes={{ root: this.props.classes.boldText }}>Some token which was generated for some app</Typography>
                        </Box>
                        <Button variant='outlined' classes={{ root: this.props.classes.deleteBtn }}>Delete</Button>
                    </Box>
                    <Box p={2} display='flex' justifyContent='space-between'>
                        <Box display='flex' alignItems='center'>
                            <Typography classes={{ root: this.props.classes.boldText }}>Some token which was generated for some app</Typography>
                        </Box>
                        <Button variant='outlined' classes={{ root: this.props.classes.deleteBtn }}>Delete</Button>
                    </Box>
                    <Box p={2} display='flex' justifyContent='space-between'>
                        <Box display='flex' alignItems='center'>
                            <Typography classes={{ root: this.props.classes.boldText }}>Some token which was generated for some app</Typography>
                        </Box>
                        <Button variant='outlined' classes={{ root: this.props.classes.deleteBtn }}>Delete</Button>
                    </Box>

                </Paper>
            </Box>
        </React.Fragment>;
    }
}

export namespace UserSettingsTokensComponent {
    export interface Props extends WithStyles<typeof tokensStyle> {
        user: ExtensionRegistryUser
        service: ExtensionRegistryService
    }

    export interface State {

    }
}

export const UserSettingsTokens = withStyles(tokensStyle)(UserSettingsTokensComponent);