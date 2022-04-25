const path = require('path');
const webpack = require('webpack');
const TerserJSPlugin = require('terser-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const CssMinimizerPlugin = require('css-minimizer-webpack-plugin');
module.exports = {
  entry: {
    main: './src/index.tsx',
  },
  output: {
    filename: './js/sct.js',
    path: path.resolve(__dirname, '../dist'),
  },
  module: {
    rules: [
      {
        test: /\.css$/,
        use: [
          {
            loader: MiniCssExtractPlugin.loader,
            options: {
              publicPath: '../',
            },
          },
          'css-loader',
        ],
      },
      {
        test: /\.(js|jsx)$/,
        exclude: /node_modules/,
        use: ['babel-loader'],
      },
      {
        test: /\.tsx?$/,
        exclude: /node_modules/,
        use: ['babel-loader', 'ts-loader'],
      },
      {
        test: [/\.bmp$/, /\.gif$/, /\.jpe?g$/, /\.png$/],
        loader: 'url-loader',
        options: {
          limit: 10000,
          name: './images/[name].[ext]',
        },
      },
    ],
  },
  resolve: {
    extensions: ['.ts', '.tsx', '.js', '.jsx'],
    alias: {
      '@src': path.resolve(__dirname, '../src'),
    },
  },
  optimization: {
    minimizer: [
      new TerserJSPlugin({
        terserOptions: {output: {comments: false}},
      }),
      new CssMinimizerPlugin(),
    ],
  },
  plugins: [
    new webpack.DefinePlugin({
      [`process.env.NODE_ENV`]: JSON.stringify(process.env.NODE_ENV),
    }),
    new webpack.optimize.LimitChunkCountPlugin({maxChunks: 1}),
  ],
};
