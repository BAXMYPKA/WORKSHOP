import React from "react";
import style from "./rightBlockContainer.css";
import RightMainMenu from "./RightMainMenu.jsx";
import RightChat from "./RightChat.jsx";
import RightTodo from "./RightTodo.jsx";
import {setDisplayRightMenu} from "../../applicationActions.es6";
import {connect} from "react-redux";

function mapDispatchToProps(dispatch) {
	return {
		setDisplayRightMenu: () => dispatch(setDisplayRightMenu())
	};
};

const mapStateToProps = (state) => {
	return {
		displayRightMenu: state.displayRightMenu,
		displayRightChat: state.displayRightChat,
		displayRightTodo: state.displayRightTodo,
	};
};

class rightBlockContainer extends React.Component {
	constructor(props) {
		super(props);
		this.props.setDisplayRightMenu();
	}
	
	render() {
		return (
			<div className={style.rightMenuContainer}>
				{this.props.displayRightMenu ? <RightMainMenu/> :
				this.props.displayRightChat ? <RightChat/> :
				this.props.displayRightTodo ? <RightTodo/> : null}
			</div>
		);
	}
}

const RightBlockContainer = connect(mapStateToProps, mapDispatchToProps)(rightBlockContainer);
export default RightBlockContainer;