import React from "react";
import styles from "./common.css";
import {store} from "../application.es6";
import {setBackgroundColor} from "../applicationActions.es6";
import {SET_BACKGROUND_COLOR} from "../applicationActionTypes.es6";

export default function Button(props) {
	return (
		<button onClick={setBackgroundColor}>
			{props.text}
		</button>
	);
}