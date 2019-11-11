/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/

import * as React from "react";
import { Box, withStyles, Theme, createStyles, WithStyles, Typography, Button, Link } from "@material-ui/core";
import { ExtensionRegistryService } from "../../extension-registry-service";
import { Extension } from "../../extension-registry-types";
import * as MarkdownIt from 'markdown-it';
import { utcToZonedTime } from "date-fns-tz";

const overviewStyles = (theme: Theme) => createStyles({
    markdown: {
        '& img': {
            maxWidth: '100%'
        }
    },
    categoryButton: {
        fontWeight: 'normal',
        textTransform: 'none',
        marginRight: theme.spacing(0.5),
        marginBottom: theme.spacing(0.5),
        padding: '1px 6px'
    }
});

class ExtensionDetailOverviewComponent extends React.Component<ExtensionDetailOverview.Props, ExtensionDetailOverview.State> {

    protected markdownIt: MarkdownIt;

    constructor(props: ExtensionDetailOverview.Props) {
        super(props);
        this.markdownIt = new MarkdownIt('commonmark');
        this.state = {};
    }

    componentDidMount() {
        this.init();
    }

    protected async init() {
        if (this.props.extension.readmeUrl) {
            const readMe = await this.props.service.getExtensionReadMe(this.props.extension.readmeUrl);
            this.setState({ readMe });
        } else {
            this.setState({ readMe: '## No README available' });
        }
    }

    render() {
        if (!this.state.readMe) {
            return '';
        }
        const { classes, extension } = this.props;
        let zonedDate;
        if (extension.timestamp) {
            const date = new Date(extension.timestamp);
            const timeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;
            zonedDate = utcToZonedTime(date, timeZone);
        }
        return <React.Fragment>
            <Box display='flex' >
                <Box className={classes.markdown} flex={5}>
                    {this.renderMarkdown(this.state.readMe)}
                </Box>
                <Box flex={3} display='flex' justifyContent='flex-end'>
                    <Box width='80%'>
                        {extension.categories ?
                            <Box>
                                <Typography variant='h6'>Categories</Typography>
                                {extension.categories.map((cat: string) =>
                                    <Button className={classes.categoryButton} size='small' key={cat} variant='outlined'>{cat}</Button>)}
                            </Box> : ''}
                        <Box mt={2}>
                            {extension.tags ?
                                <Box>
                                    <Typography variant='h6'>Tags</Typography>
                                    {extension.tags.map((tag: string) =>
                                        <Button className={classes.categoryButton} size='small' key={tag} variant='outlined'>{tag}</Button>)}
                                </Box> : ''}
                        </Box>
                        <Box mt={2}>
                            <Typography variant='h6'>Resources</Typography>
                            {this.renderResourceLink('Homepage', extension.homepage)}
                            {this.renderResourceLink('Repository', extension.repository)}
                            {this.renderResourceLink('Bugs', extension.bugs)}
                            {this.renderResourceLink('Q\'n\'A', extension.qna)}
                            {this.renderResourceLink('Download', extension.downloadUrl)}
                        </Box>
                        <Box mt={2}>
                            <Typography variant='h6'>More Info</Typography>
                            {this.renderInfo('Publisher', extension.publisher)}
                            {extension.version ? this.renderInfo('Version', extension.version) : ''}
                            {zonedDate ? this.renderInfo('Date', zonedDate.toLocaleString()) : ''}
                        </Box>
                    </Box>
                </Box>
            </Box>
        </React.Fragment>;
    }

    protected renderResourceLink(label: string, href?: string) {
        return href ? <Box><Link href={href} target='_blank' variant='body2' color='secondary'>{label}</Link></Box> : '';
    }

    protected renderInfo(key: string, value: string) {
        return <Box display='flex'>
            <Box flex='1'>
                <Typography variant='body2'>{key}</Typography>
            </Box>
            <Box flex='1'>
                <Typography variant='body2'>{value}</Typography>
            </Box>
        </Box>
    }

    protected renderMarkdown(md: string) {
        const renderedMd = this.markdownIt.render(md);
        return <span dangerouslySetInnerHTML={{ __html: renderedMd }} />;
    }
}

export namespace ExtensionDetailOverview {
    export interface Props extends WithStyles<typeof overviewStyles> {
        extension: Extension,
        service: ExtensionRegistryService
    }
    export interface State {
        readMe?: string
    }
}

export const ExtensionDetailOverview = withStyles(overviewStyles)(ExtensionDetailOverviewComponent);
