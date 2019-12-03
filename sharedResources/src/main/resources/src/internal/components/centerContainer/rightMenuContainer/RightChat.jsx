import React from "react";
import style from "./rightChat.css"

export default class RightChat extends React.Component {
	constructor(props) {
		super(props);
	}
	render() {
		let displayBlock = {display: 'block'};
		let displayNone = {display: 'none'};
		return(
			<div className={style.rightChat} style={displayBlock}>
				CHAT
			</div>
		)
	}
};