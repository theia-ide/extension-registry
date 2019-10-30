/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/

import * as React from "react";
import { Theme, createStyles, WithStyles, withStyles, Box, Typography, Divider } from "@material-ui/core";
import { ExtensionRating, Extension, ExtensionRegistryUser } from "../../extension-registry-types";
import { TextDivider } from "../../custom-mui-components/text-divider";
import { ExportRatingStars } from "./extension-rating-stars";
import { ExtensionReviewDialog } from "./extension-review-dialog";

const ratingStyles = (theme: Theme) => createStyles({
    boldText: {
        fontWeight: 'bold'
    }
});

class ExtensionDetailRatingComponent extends React.Component<ExtensionDetailRatingComponent.Props> {

    render() {
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
                {this.props.ratings.map((r: ExtensionRating) => {
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
        ratings: ExtensionRating[],
        extension: Extension,
        user?: ExtensionRegistryUser
    }
}

export const ExtensionDetailRating = withStyles(ratingStyles)(ExtensionDetailRatingComponent);

