import React from "react";
import Header from "./header/Header.jsx";
import Article from "./article/Article.jsx";

export default class MainContainer extends React.Component {
	constructor(props) {
		super(props);
		
	}
	render() {
		return (
			<div>
				<Header/>
				<Article/>
			</div>
		);
	}
};