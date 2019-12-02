import React from "react";
import style from "./centerContainer.css"

export default class CenterContainer extends React.Component{
	constructor(props) {
		super(props);
	}
	render() {
		return(
			<div className={style.containerCenter}>
			CENTER
			</div>
		)
	}
}