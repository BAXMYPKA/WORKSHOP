import React from "react";
import applicationStyle from "../application.css";
import style from "./rightMainMenu.css";
import {connect} from "react-redux";
import A from "../common/A.jsx";
import {setDisplayCenterLeftOrders, setDisplayCenterLeftTasks} from "../../redux/applicationActions.es6";
import {setDisplayCenterLeftArticles} from "../../redux/applicationActions.es6";

const mapStateToProps = (state) => {
	return {};
};

const mapDispatchToProps = dispatch => {
	return ({
		setDisplayArticles: () => dispatch(setDisplayCenterLeftArticles()),
		setDisplayOrders: () => dispatch(setDisplayCenterLeftOrders()),
		setDisplayTasks: () => dispatch(setDisplayCenterLeftTasks())
	});
};

class rightMainMenu extends React.Component {
	constructor(props) {
		super(props);
	}
	
	render() {
		return (
			<div className={style.rightMainMenuContainer} style={this.props.style}>
				<div className={style.rightMainMenu__menuHeader}>
					Main menu
				</div>
				<ul className={style.rightMainMenu}>
					<li>
						<A href={''} onClick={this.props.setDisplayArticles} text={'MAIN PAGE'}/>
					</li>
					<li>
						<A href={''} onClick={this.props.setDisplayOrders} text={'ORDERS IN PROCESS'}/>
					</li>
					<li>
						<A href={''} onClick={this.props.setDisplayTasks} text={'TASKS IN PROGRESS'}/>
					</li>
				</ul>
			</div>
		);
	}
};

const RightMainMenu = connect(mapStateToProps, mapDispatchToProps)(rightMainMenu);

export default RightMainMenu;