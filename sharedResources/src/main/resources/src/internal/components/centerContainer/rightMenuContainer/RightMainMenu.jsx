import React from "react";
import applicationStyle from "../../application.css"
import style from "./rightMainMenu.css"
import {connect} from "react-redux";
import A from "../../common/A.jsx";

const mapStateToProps = (state) => {
	return {
	}
}

class rightMainMenu extends React.Component {
	constructor(props) {
		super(props);
		this.click = this.click.bind(this);
	}
	
	click() {
		console.log("CLICK HAS BEEN OCCURRED");
	}
	
	render() {
		return (
			<div className={style.rightMainMenuContainer} style={this.props.style} >
				<div className={style.rightMainMenu__menuHeader}>
					Main menu
				</div>
				<ul className={style.rightMainMenu}>
					<li>
						<A href={''} onClick={this.click} text={'ORDERS IN PROCESS'}/>
					</li>
					<li>
						<A href={''} onClick={this.click} text={'TASKS IN PROGRESS'}/>
					</li>
				</ul>
			</div>
		);
	}
};

const RightMainMenu = connect(mapStateToProps)(rightMainMenu);

export default RightMainMenu;