const path = require('path');
// const {CleanWebpackPlugin} = require('clean-webpack-plugin');

module.exports = {
	mode: 'development',
	entry: {
		'js/index': './src/js/index.es6',
		'js/profile': './src/js/profile.es6',
		'js/userOrder': './src/js/userOrder.es6',
		'js/registration': './src/js/registration.es6',
		'js/passwordReset': './src/js/passwordReset.es6',
		'internal/js/application': './src/internal/components/application.es6'
	},
	devtool: 'source-map',
	plugins: [
		// new CleanWebpackPlugin(),
	],
	output: {
		filename: '[name].es6',
		path: path.resolve(__dirname, 'dist'),
	},
	devServer: {
		contentBase: path.join(__dirname, 'dist/internal/'),
		publicPath: '/',
		compress: true,
		port: 9000,
		liveReload: true,
		hot: true,
		watchContentBase: true, //to force page reload
		progress: true,
		watchOptions: {
			poll: true
		}
	},
	watchOptions: {
		aggregateTimeout: 500,
		poll: 1000
	},
	module: {
		rules: [
			{
				test: /\.m?es6|jsx$/,
				exclude: /(node_modules|bower_components)/,
				use: {
					loader: 'babel-loader',
					options: {
						presets: [
							'@babel/preset-env',
							'@babel/preset-react'
						]
					}
				}
			},
			{
				test: /\.css$/,
				use: [
					'style-loader',
					{
						loader: 'css-loader',
						options: {
							importLoaders: 1,
							modules: true
						}
					}
				]
			}
		]
	}
};
