import React from "react";
import style from "./rightChat.css";
import {connect} from "react-redux";

const mapStateToProps = (state) => {
	return {
		displayRightChat: state.displayRightChat
	};
};

class rightChat extends React.Component {
	constructor(props) {
		super(props);
		console.log("CHAT CONSTRUCTOR: "+this.props.style.display)
	}
	
	componentDidMount() {
		console.log("CHAT DID MOUNT: "+this.props.style.display)
	}
	
	render() {
		console.log("CHAT RENDER DISPLAY: "+this.props.style.display)
		console.log("CHAT RENDER DISPLAY CHAT: "+this.props.displayRightChat.display)
		return (
			<div className={style.rightChat} style={this.props.style}>
				CHAT
			</div>
		);
	}
};

const RightChat = connect(mapStateToProps)(rightChat);

export default RightChat;