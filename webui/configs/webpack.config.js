var webpack = require('webpack');
var path = require('path');

module.exports = {
    mode: 'development',
    devtool: 'source-map',

    entry: [
        './dev/index.tsx'
    ],
    devServer: {
        contentBase: './dev',
        port: 3000,
        historyApiFallback: {
            index: '/'
        }
    },
    output: {
        filename: 'bundle.js',
        path: path.resolve(__dirname, '../dev'),
        publicPath: '/'
    },

    resolve: {
        extensions: ['.ts', '.tsx', '.js']
    },
    module: {
        rules: [
            {
                test: /\.tsx?$/,
                use: [{
                    loader: 'ts-loader',
                    options: {
                        configFile: path.resolve(__dirname, 'examples.tsconfig.json')
                    }
                }]
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
        new webpack.ProgressPlugin()
    ]
};
