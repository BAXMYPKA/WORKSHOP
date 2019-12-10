import React from "react";
import style from "./centerLeftContainer.css";
import {setDisplayCenterLeftOrders} from "../applicationActions.es6";
import CenterLeftOrdersView from "./CenterLeftOrdersView.jsx";
import {connect} from "react-redux";

const mapDispatchToProps = dispatch => {
	return {
		setDisplayCenterLeftOrders: () => dispatch(setDisplayCenterLeftOrders())
	};
};

const mapStateToProps = state => {
	return {
		displayCenterLeftOrders: state.displayCenterLeftOrders
	};
};

class CenterLeftContainer extends React.Component {
	constructor(props) {
		super(props);
	}
	
	render() {
		return (
			<div className={style.centerLeftContainer}>
				{this.props.displayCenterLeftOrders ? <CenterLeftOrdersView/> : null}
			</div>
		);
	}
}

export default connect(mapStateToProps)(CenterLeftContainer);