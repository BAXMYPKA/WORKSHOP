import React from "react";
import {SET_DISPLAY_BLOCK, SET_DISPLAY_NONE} from "./applicationActionTypes.es6";

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