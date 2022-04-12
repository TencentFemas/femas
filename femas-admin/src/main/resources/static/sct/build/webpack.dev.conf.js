const base = require('./webpack.base.conf');
const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');

module.exports = Object.assign({}, base, {
  output: {
    filename: './js/sct.js',
    path: path.resolve(__dirname, '../dist'),
  },
  devServer: {
    port: 9010,
    historyApiFallback: true,
  },
  mode: 'development',
  plugins: [
    new HtmlWebpackPlugin({
      filename: 'index.html',
      template: './src/assets/index.html',
      minify: false,
      publicPath: '/',
    }),
    new MiniCssExtractPlugin({
      filename: `./css/sct.css`,
    }),
    ...base.plugins,
  ],
});
