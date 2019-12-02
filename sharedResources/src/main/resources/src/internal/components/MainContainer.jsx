import React from "react";
import style from "./mainContainer.css"
import Header from "./header/Header.jsx";
import Article from "./article/Article.jsx";
import BottomBlackLine from "./common/BottomBlackLine.jsx";

export default class MainContainer extends React.Component {
	constructor(props) {
		super(props);
		
	}
	render() {
		return (
			<div className={style.mainContainer}>
				<Header/>
				<Article/>
				<BottomBlackLine/>
			</div>
		);
	}
};