import React from "react";
import style from "./bottomBlackLine.css";

import Button from "../common/Button.jsx";
import PowerButton from "./PowerButton.jsx";
import Span from "../common/Span.jsx";

export default class RightBottomMenu extends React.Component {
	constructor(props) {
		super(props);
	}
	
	render() {
		return <div className={style.rightBottomMenu}>
			<div className={style.menuButtonDiv}>
				<Span style={style.menuName} text={'Menu'}/>
				<Button style={style.menuButton}/>
			</div>
			<div className={style.menuButtonDiv}>
				<Span style={style.menuName} text={'Todos'}/>
				<Button style={style.menuButton}/>
			</div>
			<div className={style.menuButtonDiv}>
				<Span style={style.menuName} text={'Chat'}/>
				<Button style={style.menuButton}/>
			</div>
			<div className={style.separator}>
			</div>
			<PowerButton style={style.powerButton}/>
		</div>;
	}
};