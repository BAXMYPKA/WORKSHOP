import React from "react";
import applicationStyle from "../../application.css"
import style from "./rightMainMenu.css"
export default class RightMainMenu extends React.Component {
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