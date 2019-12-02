import React from "react";
import styles from "./common.css"

export default function Button(props) {
	return(
		<button onClick={props.closeClick} className={props.style}>
		</button>
	)
}