/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/

import * as React from "react";
import { Button, Dialog, DialogTitle, DialogContent, DialogContentText, TextField, DialogActions, Theme } from "@material-ui/core";
import { withStyles, createStyles, WithStyles } from "@material-ui/styles";
import { ExtensionRegistryService } from "../../extension-registry-service";
import { ExtensionRatingStarSetter } from "./ExtensionRatingStarSetter";
import { ExtensionRegistryUser } from "../../extension-registry-types";

const revivewDialogStyles = (theme: Theme) => createStyles({

});

class ExtensionReviewDialogComponent extends React.Component<ExtensionReviewDialogComponent.Props, ExtensionReviewDialogComponent.State> {

    protected service = ExtensionRegistryService.instance;

    constructor(props: ExtensionReviewDialogComponent.Props) {
        super(props);

        this.state = {
            open: false,
            title: '',
            comment: ''
        }
    }

    protected handleOpenButton = () => this.setState({ open: true });
    protected handleCancel = () => this.setState({open: false});
    protected handleSave = async () => {
        await this.service.postReview({
            rating: 4,
            title: this.state.title,
            comment: this.state.comment,
            date: Date.now().toString(),
            user: this.props.user
        });
        this.setState({open: false, title: '', comment: ''});
    }
    protected handleCommentChange = (event: React.ChangeEvent<HTMLTextAreaElement>) => this.setState({comment: event.target.value});
    protected handleTitleChange = (event: React.ChangeEvent<HTMLInputElement>) => this.setState({title: event.target.value});

    render() {
        return <React.Fragment>
            <Button variant='contained' color='secondary' onClick={this.handleOpenButton}>
                Write a Review
            </Button>
            <Dialog open={this.state.open} onClose={this.handleCancel}>
                <DialogTitle>{this.props.extensionName} Review</DialogTitle>
                <DialogContent>
                    <DialogContentText>
                        Your review will be posted publicly as {this.props.user.userName}
                    </DialogContentText>
                    <ExtensionRatingStarSetter />
                    <TextField fullWidth label='Review Title' onChange={this.handleTitleChange}/>
                    <TextField
                        autoFocus
                        margin="dense"
                        label="Your Review..."
                        fullWidth
                        multiline
                        onChange={this.handleCommentChange}
                    />
                </DialogContent>
                <DialogActions>
                    <Button onClick={this.handleCancel} color="secondary">
                        Cancel
                    </Button>
                    <Button onClick={this.handleSave} color="primary">
                        Post Review
                    </Button>
                </DialogActions>
            </Dialog>
        </React.Fragment>
    }
}

export namespace ExtensionReviewDialogComponent {
    export interface Props extends WithStyles<typeof revivewDialogStyles> {
        extensionName: string,
        user: ExtensionRegistryUser
    }
    export interface State {
        open: boolean,
        comment: string,
        title: string
    }
}

export const ExtensionReviewDialog = withStyles(revivewDialogStyles)(ExtensionReviewDialogComponent);