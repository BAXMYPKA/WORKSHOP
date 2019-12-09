import React from "react";
import {
	SET_DISPLAY_BLOCK,
	SET_DISPLAY_NONE,
	SET_BACKGROUND_COLOR,
	SET_DISPLAY_RIGHT_CHAT,
	SET_DISPLAY_RIGHT_MENU,
	SET_DISPLAY_RIGHT_TODO
} from "./applicationActionTypes.es6";

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

export function setDisplayRightMenu() {
	return {
		type: SET_DISPLAY_RIGHT_MENU
	}
}

export function setDisplayRightChat() {
	return {
		type: SET_DISPLAY_RIGHT_CHAT
	}
}

export function setDisplayRightTodo() {
	return {
		type: SET_DISPLAY_RIGHT_TODO
	}
}