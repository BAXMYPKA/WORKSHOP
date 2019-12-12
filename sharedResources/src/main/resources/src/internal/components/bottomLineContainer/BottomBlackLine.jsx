import React from "react";
import style from "./bottomBlackLine.css"
import RightBottomMenu from "./RightBottomMenu.jsx";

export default class BottomBlackLine extends React.Component {
	constructor(props) {
		super(props);
	}
	
	render() {
		return <div className={style.bottomBlackLine}>
			<div></div>
			<div className={style.bottomBlackLine__centerLogoDiv}>WORKSHOP</div>
			<RightBottomMenu/>
		</div>
	}
};