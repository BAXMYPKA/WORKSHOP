import React from "react";
import {
	SET_DISPLAY_BLOCK,
	SET_DISPLAY_NONE,
	SET_BACKGROUND_COLOR,
	SET_DISPLAY_RIGHT_CHAT,
	SET_DISPLAY_RIGHT_TODO,
	SET_DISPLAY_RIGHT_MENU,
	SET_DISPLAY_CENTER_LEFT_ORDERS,
	SET_DISPLAY_CENTER_LEFT_TASKS,
	SET_DISPLAY_CENTER_LEFT_ARTICLES
} from "./applicationActionTypes.es6";

export const initialState = {
	style: {
		backgroundColor: 'green',
	},
	displayRightMenu: false,
	displayRightChat: false,
	displayRightTodo: false,
	displayCenterLeftArticles: true,
	displayCenterLeftOrders: false,
	displayCenterLeftTasks: false
};

export default function applicationReducer(state = initialState, action) {
	
	switch (action.type) {
		case SET_DISPLAY_BLOCK :
			return Object.assign({}, state, {styleDisplay: {display: 'block'}});
		case SET_DISPLAY_NONE :
			return Object.assign({}, state, {styleDisplay: {display: 'none'}});
		case SET_BACKGROUND_COLOR :
			return Object.assign({}, state, {style: {backgroundColor: action.payload.color}});
		case SET_DISPLAY_RIGHT_MENU :
			return Object.assign({}, state, {displayRightMenu: true},
				{displayRightChat: false}, {displayRightTodo: false});
		case SET_DISPLAY_RIGHT_CHAT :
			return Object.assign({}, state, {displayRightChat: true},
				{displayRightMenu: false}, {displayRightTodo: false});
		case SET_DISPLAY_RIGHT_TODO :
			return Object.assign({}, state, {displayRightTodo: true},
				{displayRightMenu: false}, {displayRightChat: false});
		case SET_DISPLAY_CENTER_LEFT_ARTICLES :
			return Object.assign({}, state, {displayCenterLeftArticles: true},
				{displayCenterLeftTasks: false}, {displayCenterLeftOrders: false});
		case SET_DISPLAY_CENTER_LEFT_ORDERS :
			return Object.assign({}, state, {displayCenterLeftOrders: true},
				{displayCenterLeftTasks: false}, {displayCenterLeftArticles: false});
		case SET_DISPLAY_CENTER_LEFT_TASKS :
			return Object.assign({}, state, {displayCenterLeftTasks: true},
				{displayCenterLeftOrders: false}, {displayCenterLeftArticles: false});
		default:
			return state;
	}
}