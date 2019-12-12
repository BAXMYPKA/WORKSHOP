import React from "react";
import style from "./centerContainer.css"
import RightBlockContainer from "./RightBlockContainer.jsx";
import CenterLeftContainer from "./CenterLeftContainer.jsx";
import Button from "../common/Button.jsx";

function mapStateToProps(state) {
	return {
		one: state.g1
	}
}

export default class CenterContainer extends React.Component{
	constructor(props) {
		super(props);
	}
	render() {
		return(
			<div className={style.containerCenter}>
				<CenterLeftContainer/>
				<RightBlockContainer/>
			</div>
		)
	}
}