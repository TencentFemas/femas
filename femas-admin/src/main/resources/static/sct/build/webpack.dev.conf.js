const base = require("./webpack.base.conf");
const path = require("path");
const HtmlWebpackPlugin = require("html-webpack-plugin");
const MiniCssExtractPlugin = require("mini-css-extract-plugin");

module.exports = Object.assign({}, base, {
  output: {
    filename: "./js/sct.js",
    path: path.resolve(__dirname, "../dist"),
  },
  devServer: {
    port: 9010,
    historyApiFallback: true,
    proxy: {
      // "/atom/v1/lane": {
      //   target: "http://mock.apifox.cn/",
      //   pathRewrite: {
      //     "^/atom/v1/lane": "/m1/1547256-0-default/atom/v1/lane",
      //   },
      //   secure: false,
      //   changeOrigin: true,
      // },
      "/": {
        target: "http://106.53.107.83:8080",
        pathRewrite: { "^/": "" },
      },
    },
  },
  mode: "development",
  plugins: [
    new HtmlWebpackPlugin({
      filename: "index.html",
      template: "./src/assets/index.html",
      minify: false,
      publicPath: "/",
    }),
    new MiniCssExtractPlugin({
      filename: `./css/sct.css`,
    }),
    ...base.plugins,
  ],
});
