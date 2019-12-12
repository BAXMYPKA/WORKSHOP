import React from "react";
import styles from "./common.css";

export default function Button(props) {
	return (
		<button onClick={props.onClick} title={props.title} className={props.className} value={props.value}>
			{props.text}
		</button>
	);
}