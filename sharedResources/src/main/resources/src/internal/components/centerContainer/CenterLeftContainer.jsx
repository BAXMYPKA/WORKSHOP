import React from "react";
import style from "./centerLeftContainer.css";
import {setDisplayCenterLeftOrders} from "../../redux/applicationActions.es6";
import CenterLeftOrdersView from "./CenterLeftOrdersView.jsx";
import CenterLeftTasksView from "./CenterLeftTasksView.jsx";
import {connect} from "react-redux";
import CenterLeftArticlesView from "./CenterLeftArticlesView.jsx";

const mapDispatchToProps = dispatch => {
	return {
		setDisplayCenterLeftOrders: () => dispatch(setDisplayCenterLeftOrders())
	};
};

const mapStateToProps = state => {
	return {
		displayCenterLeftArticles: state.displayCenterLeftArticles,
		displayCenterLeftOrders: state.displayCenterLeftOrders,
		displayCenterLeftTasks: state.displayCenterLeftTasks
	};
};

class CenterLeftContainer extends React.Component {
	constructor(props) {
		super(props);
	}
	
	render() {
		return (
			<div className={style.centerLeftContainer}>
				{this.props.displayCenterLeftArticles ? <CenterLeftArticlesView/> :
					this.props.displayCenterLeftOrders ? <CenterLeftOrdersView/> :
					this.props.displayCenterLeftTasks ? <CenterLeftTasksView/> : null}
			</div>
		);
	}
}

export default connect(mapStateToProps, mapDispatchToProps)(CenterLeftContainer);