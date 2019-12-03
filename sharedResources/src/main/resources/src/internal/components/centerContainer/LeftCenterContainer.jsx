import React from "react";
import style from "./leftCenterContainer.css"

export default class LeftCenterContainer extends React.Component{
	constructor(props) {
		super(props);
	}
	
	render() {
		return <div className={style.LeftCenterContainer}>
			LEFT
		</div>
	}
}