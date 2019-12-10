import React from "react";
import style from "./rightChat.css";
import {connect} from "react-redux";

const mapStateToProps = (state) => {
	return {};
};

class rightChat extends React.Component {
	constructor(props) {
		super(props);
	}
	
	render() {
		return (
			<div className={style.rightChat} style={this.props.style}>
				CHAT
			</div>
		);
	}
};

const RightChat = connect(mapStateToProps)(rightChat);

export default RightChat;