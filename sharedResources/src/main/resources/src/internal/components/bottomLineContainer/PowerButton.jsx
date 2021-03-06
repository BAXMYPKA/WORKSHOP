import React from "react";
import style from "./bottomBlackLine.css"

export default function PowerButton(props) {
	return (
		<div className={props.className}>
			<a href={'../shutdown'}>
				<img alt={props.alt} className={style.powerButton} src={'../dist/internal/img/powerButton.png'} />
			</a>
		</div>
	);
};