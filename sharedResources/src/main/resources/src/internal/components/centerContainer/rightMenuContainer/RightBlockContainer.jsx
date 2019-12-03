import React from "react";
import style from "./rightBlockContainer.css"
import RightMainMenu from "./RightMainMenu.jsx";
import RightChat from "./RightChat.jsx";

export default class RightBlockContainer extends React.Component{
	constructor(props) {
		super(props);
	}
	
	render() {
		return(
			<div className={style.rightMenuContainer}>
				<RightMainMenu/>
				<RightChat/>
			</div>
		)
	}
}