import React from "react";
import applicationStyle from "../../application.css"
import style from "./rightMainMenu.css"
// import mapStateToProps from "react-redux/es/connect/mapStateToProps.js";
import {connect} from "react-redux";

const mapStateToProps = (state) => {
	return {
		style: state.displayRightMenu
	}
}

class rightMainMenu extends React.Component {
	constructor(props) {
		super(props);
	}
	
	componentDidMount() {
		console.log("RIGHT MENU DID MOUNT MENU: " + this.props.displayRightMenu);
		console.log("RIGHT MENU DID MOUNT CHAT: " + this.props.displayRightChat);
	}
	
	render() {
		return (
			<div className={style.rightMainMenuContainer} style={this.props.style} >
				<div className={style.rightMainMenu__menuHeader}>
					Main menu
				</div>
				<ul className={style.rightMainMenu}>
					<li>
						<a href={''}>ITEM ONE</a>
					</li>
					<li>
						<a href={''}> ITEM TWO </a>
					</li>
				</ul>
			</div>
		);
	}
};

const RightMainMenu = connect(mapStateToProps)(rightMainMenu);

export default RightMainMenu;