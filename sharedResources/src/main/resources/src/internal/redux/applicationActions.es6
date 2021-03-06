import React from "react";
import {
	SET_DISPLAY_BLOCK,
	SET_DISPLAY_NONE,
	SET_BACKGROUND_COLOR,
	SET_DISPLAY_RIGHT_CHAT,
	SET_DISPLAY_RIGHT_MENU,
	SET_DISPLAY_RIGHT_TODO,
	SET_DISPLAY_CENTER_LEFT_ORDERS,
	SET_DISPLAY_CENTER_LEFT_TASKS,
	SET_DISPLAY_CENTER_LEFT_ARTICLES
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
	};
}

export function setDisplayRightMenu() {
	return {
		type: SET_DISPLAY_RIGHT_MENU
	};
}

export function setDisplayRightChat() {
	return {
		type: SET_DISPLAY_RIGHT_CHAT
	};
}

export function setDisplayRightTodo() {
	return {
		type: SET_DISPLAY_RIGHT_TODO
	};
}

export function setDisplayCenterLeftOrders() {
	return {
		type: SET_DISPLAY_CENTER_LEFT_ORDERS
	};
}

export function setDisplayCenterLeftTasks() {
	return {
		type: SET_DISPLAY_CENTER_LEFT_TASKS
	};
}

export function setDisplayCenterLeftArticles() {
	return {
		type: SET_DISPLAY_CENTER_LEFT_ARTICLES
	};
}