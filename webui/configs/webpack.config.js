const webpack = require('webpack');
const path = require('path');
const CopyPlugin = require('copy-webpack-plugin');

const outputPath = path.resolve(__dirname, '../../server/src/main/resources/static')

module.exports = {
    mode: 'development',
    devtool: 'source-map',
    devServer: {
        contentBase: './dev',
        port: 3000,
        historyApiFallback: {
            index: '/'
        }
    },

    entry: [
        './dev/index.tsx'
    ],
    output: {
        filename: 'bundle.js',
        path: outputPath,
        publicPath: '/'
    },

    resolve: {
        extensions: ['.ts', '.tsx', '.js']
    },
    module: {
        rules: [
            {
                test: /\.tsx?$/,
                use: ['ts-loader']
            },
            {
                test: /\.js$/,
                use: ['source-map-loader'],
                enforce: 'pre'
            },
            {
                test: /\.css$/,
                exclude: /\.useable\.css$/,
                use: ['style-loader', 'css-loader']
            },
            {
                test: /\.(png|svg|jpg|gif)$/,
                use: ['file-loader']
            }
        ]
    },
    node: { fs: 'empty', net: 'empty' },

    plugins: [
        new webpack.WatchIgnorePlugin([
            /\.js$/,
            /\.d\.ts$/
        ]),
        new webpack.ProgressPlugin(),
        new CopyPlugin([
            { from: 'dev', to: outputPath }
        ]),
    ]
};
