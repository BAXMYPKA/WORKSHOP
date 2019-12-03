import React from "react";
import style from "./rightChat.css"

export default class RightChat extends React.Component {
	constructor(props) {
		super(props);
	}
	render() {
		return(
			<div className={style.rightChat} style={this.props.style}>
				CHAT
			</div>
		)
	}
};