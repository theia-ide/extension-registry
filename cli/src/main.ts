/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/

import * as commander from 'commander';
import * as didYouMean from 'didyoumean';
import { publish } from './publish';

const pkg = require('../package.json');

module.exports = function (argv: string[]): void {
    const program = new commander.Command();
    program.version(pkg.version);
    program.usage('<command> [options]');

    program
        .command('publish [packageFile]')
        .description('Publishes an extension')
        .option('-r, --registryUrl <url>', 'Use the registry API at this base URL.')
        .option('-p, --pat <token>', 'Personal access token')
        .option('--packagePath <path>', 'Package and publish the extension at the specified path.')
        .option('--baseContentUrl <url>', 'Prepend all relative links in README.md with this URL.')
        .option('--baseImagesUrl <url>', 'Prepend all relative image links in README.md with this URL.')
        .option('--yarn', 'Use yarn instead of npm while packing extension files')
        .action((packageFile: string, { registryUrl, pat, packagePath, baseContentUrl, baseImagesUrl, yarn }) => {
            if (packageFile !== undefined && packagePath !== undefined) {
                console.error('Please specify either a package file or a package path, but not both.');
                program.help();
            }
            if (packageFile !== undefined && baseContentUrl !== undefined)
                console.warn("Ignoring option 'baseContentUrl' for prepackaged extension.");
            if (packageFile !== undefined && baseImagesUrl !== undefined)
                console.warn("Ignoring option 'baseImagesUrl' for prepackaged extension.");
            if (packageFile !== undefined && yarn !== undefined)
                console.warn("Ignoring option 'yarn' for prepackaged extension.");
            publish({ packageFile, registryUrl, pat, packagePath, baseContentUrl, baseImagesUrl, yarn })
                .catch(handleError);
        });

    program
        .command('*', '', { noHelp: true })
        .action((cmd: string) => {
            const suggestion = didYouMean(cmd, program.commands.map((c: any) => c._name));
            if (suggestion)
                console.error(`Unknown command '${cmd}', did you mean '${suggestion}'?`);
            else
                console.error(`Unknown command '${cmd}'`);
            program.help();
        });

    program.parse(argv);

    if (process.argv.length <= 2) {
        program.help();
    }
};

function handleError(reason: any): void {
    console.error(reason);
    process.exit(1);
}
