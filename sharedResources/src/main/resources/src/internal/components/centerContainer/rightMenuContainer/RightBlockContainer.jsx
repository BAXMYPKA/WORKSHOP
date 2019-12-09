import React from "react";
import style from "./rightBlockContainer.css";
import RightMainMenu from "./RightMainMenu.jsx";
import RightChat from "./RightChat.jsx";
import RightTodo from "./RightTodo.jsx";
import {setDisplayRightMenu} from "../../applicationActions.es6";
import {connect} from "react-redux";

function mapDispatchToProps (dispatch) {
	return {
		setDisplayRightMenu: () => dispatch(setDisplayRightMenu())
	}
};

const mapStateToProps = (state) => {
	return {
		displayRightMenu: state.displayRightMenu,
		displayRightChat: state.displayRightChat,
		displayRightTodo: state.displayRightTodo,
	}
}

class rightBlockContainer extends React.Component {
	constructor(props) {
		super(props);
		console.log("TTTTTTTTTTTTTTTTTTTTTTTTTTTTT");
		this.props.setDisplayRightMenu();
	}
	
	componentDidMount() {
		console.log("RIGHT BLOCK DID MOUNT CHAT: " + this.props.displayRightChat.display);
		console.log("RIGHT BLOCK DID MOUNT MENU: " + this.props.displayRightMenu.display);
		
	}
	
	render() {
		console.log("RIGHT BLOCK RENDER CHAT: " + this.props.displayRightChat.display);
		console.log("RIGHT BLOCK RENDER MENU: " + this.props.displayRightMenu.display);
		
		return (
			<div className={style.rightMenuContainer}>
				<RightMainMenu style={this.props.displayRightMenu}/>
				<RightChat style={this.props.displayRightChat}/>
				<RightTodo style={this.props.displayRightTodo}/>
			</div>
		);
	}
}

const RightBlockContainer = connect(mapStateToProps, mapDispatchToProps)(rightBlockContainer);
export default RightBlockContainer;