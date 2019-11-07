/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/

import * as React from "react";
import { Theme, createStyles, WithStyles, withStyles, Box, Typography, Divider } from "@material-ui/core";
import { ExtensionReview, ExtensionRegistryUser, ExtensionRaw } from "../../extension-registry-types";
import { TextDivider } from "../../custom-mui-components/text-divider";
import { ExportRatingStars } from "./extension-rating-stars";
import { ExtensionReviewDialog } from "./extension-review-dialog";
import { ExtensionRegistryService } from "../../extension-registry-service";

const ratingStyles = (theme: Theme) => createStyles({
    boldText: {
        fontWeight: 'bold'
    }
});

class ExtensionDetailRatingComponent extends React.Component<ExtensionDetailRatingComponent.Props, ExtensionDetailRatingComponent.State> {

    constructor(props: ExtensionDetailRatingComponent.Props) {
        super(props);

        this.state = {};
    }

    componentDidMount() {
        this.init();
    }

    protected async init() {
        const reviews = await this.props.service.getExtensionReviews(this.props.extension);
        this.setState({ reviews });
    }

    render() {
        if(!this.state.reviews){
            return '';
        }
        return <React.Fragment>
            <Box display='flex' justifyContent='space-between' my={2}>
                <Box>
                    <Typography variant='h5'>
                        User Reviews
                    </Typography>
                </Box>
                {
                    this.props.user ? <Box>
                        <ExtensionReviewDialog extensionName={this.props.extension.name} user={this.props.user} />
                    </Box> : ''
                }
            </Box>
            <Divider />
            <Box>
                {this.state.reviews.map((r: ExtensionReview) => {
                    return <React.Fragment key={r.user.userName + r.title + r.date}>
                        <Box my={2}>
                            <Box display='flex'>
                                <Typography variant='body2'>{r.date}</Typography>
                                <TextDivider />
                                <Typography variant='body2'>{r.user.userName}</Typography>
                            </Box>
                            <Box display='flex'>
                                <Typography className={this.props.classes.boldText}>{r.title}</Typography>
                                <Box ml={4} display='flex' alignItems='center'>
                                    <ExportRatingStars number={r.rating} />
                                </Box>
                            </Box>
                            <Box>
                                <Typography variant='body1'>{r.comment}</Typography>
                            </Box>
                        </Box>
                        <Divider />
                    </React.Fragment>;
                })}
            </Box>
        </React.Fragment>;
    }
}

export namespace ExtensionDetailRatingComponent {
    export interface Props extends WithStyles<typeof ratingStyles> {
        extension: ExtensionRaw,
        user?: ExtensionRegistryUser
        service: ExtensionRegistryService
    }
    export interface State {
        reviews?: ExtensionReview[]
    }
}

export const ExtensionDetailRating = withStyles(ratingStyles)(ExtensionDetailRatingComponent);

