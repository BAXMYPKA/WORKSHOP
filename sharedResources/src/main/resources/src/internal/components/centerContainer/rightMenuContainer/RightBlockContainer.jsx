import React from "react";
import style from "./rightBlockContainer.css";
import RightMainMenu from "./RightMainMenu.jsx";
import RightChat from "./RightChat.jsx";

export default class RightBlockContainer extends React.Component {
	constructor(props) {
		super(props);
		this.state = {
			style: {
				display: 'none'
			}
		};
	}
	
	render() {
		return (
			<div className={style.rightMenuContainer}>
				<RightMainMenu/>
				<RightChat style={this.state.style}/>
			</div>
		);
	}
}