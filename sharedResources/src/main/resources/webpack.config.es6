const path = require('path');
const {CleanWebpackPlugin} = require('clean-webpack-plugin');

module.exports = {
	mode: 'development',
	entry: {
		index: './src/js/index.es6'
	},
	devtool: 'source-map',
	plugins: [
		new CleanWebpackPlugin(),
	],
	output: {
		filename: '[name].es6',
		path: path.resolve(__dirname, 'dist/js'),
	},
	devServer: {
		contentBase: path.join(__dirname, "dist/js"),
		compress: true,
		port: 9000,
		watchContentBase: true,
		progress: true
	},
	watchOptions: {
		aggregateTimeout: 500,
		poll: 800
	},
	module: {
		rules: [
			{
				test: /\.m?js$/,
				exclude: /(node_modules|bower_components)/,
				use: {
					loader: 'babel-loader',
					options: {
						presets: ['@babel/preset-env']
					}
				}
			}
		]
	}
};
