import React from "react";

export default function Button(props) {
	return(
		<button onClick={props.closeClick}>
			{props.buttonText === true ? props.button.textCollapse : props.button.textExpand}
		</button>
	)
}