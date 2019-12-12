import React from "react";
import style from "./mainContainer.css"
import Header from "./header/Header.jsx";
import BottomBlackLine from "./bottomLineContainer/BottomBlackLine.jsx";
import CenterContainer from "./centerContainer/CenterContainer.jsx";


export default class MainContainer extends React.Component {
	constructor(props) {
		super(props);
		
	}
	render() {
		return (
			<div className={style.mainContainer}>
				<Header/>
				
				<CenterContainer/>
				
				<BottomBlackLine/>
			</div>
		);
	}
};