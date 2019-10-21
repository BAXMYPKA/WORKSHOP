const path = require('path');
const { CleanWebpackPlugin } = require('clean-webpack-plugin');

module.exports = {
	mode: 'development',
	entry: {
	index: './src/js/index.js'
	},
	devtool: 'inline-source-map',
	plugins: [
		new CleanWebpackPlugin(),
	],
	output: {
		filename: '[name].js',
		path: path.resolve(__dirname, 'dist/js'),
	},
};