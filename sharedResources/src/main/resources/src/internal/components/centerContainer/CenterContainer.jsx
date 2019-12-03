import React from "react";
import style from "./centerContainer.css"
import RightBlockContainer from "./rightMenuContainer/RightBlockContainer.jsx";
import LeftCenterContainer from "./LeftCenterContainer.jsx";

export default class CenterContainer extends React.Component{
	constructor(props) {
		super(props);
	}
	render() {
		return(
			<div className={style.containerCenter}>
				<LeftCenterContainer/>
				<RightBlockContainer/>
			</div>
		)
	}
}