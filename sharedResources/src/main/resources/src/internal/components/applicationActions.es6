import React from "react";
import {SET_DISPLAY_BLOCK, SET_DISPLAY_NONE, SET_BACKGROUND_COLOR} from "./applicationActionTypes.es6";

export function setDisplayNoneStyle() {
	return {
		type: SET_DISPLAY_NONE,
		payload: {
			display: 'none'
		}
	};
}

export function setDisplayBlockStyle() {
	return {
		type: SET_DISPLAY_BLOCK,
		payload: {
			display: 'block'
		}
	};
}

export function setBackgroundColor(color = "black") {
	return {
		type: SET_BACKGROUND_COLOR,
		payload: {
			color: color
		}
	}
}