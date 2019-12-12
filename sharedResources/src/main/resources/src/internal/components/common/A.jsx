import React from "react";

export default function A(props) {
	
	function handleClick(e) {
		e.preventDefault();
		props.onClick();
	}
	
	return (
		<a href={props.href} onClick={handleClick}>
			{props.text}
		</a>
	);
};