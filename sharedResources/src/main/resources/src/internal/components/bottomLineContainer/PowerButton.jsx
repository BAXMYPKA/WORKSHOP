import React from "react";
import style from "./bottomBlackLine.css"

export default function PowerButton(props) {
	return (
		<div className={props.className}>
			<a href={'/'}>
				<img className={style.powerButton} src={'../img/powerButton.png'}/>
			</a>
		</div>
	);
};