import React from "react";
import style from "./rightTodo.css";
import {connect} from "react-redux";

const mapStateToProps = state => {
	return {
		style: state.displayRightTodo
	};
};

class rightTodo extends React.Component {
	constructor(props) {
		super(props);
	}
	
	render() {
		return (
			<div className={style.rightTodo} style={this.props.style}>
				TODO
			</div>
		);
	}
};

const RightTodo = connect(mapStateToProps)(rightTodo);
export default RightTodo;