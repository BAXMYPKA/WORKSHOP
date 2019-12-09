import React from "react";
import {
	SET_DISPLAY_BLOCK,
	SET_DISPLAY_NONE,
	SET_BACKGROUND_COLOR,
	SET_DISPLAY_RIGHT_CHAT,
	SET_DISPLAY_RIGHT_TODO,
	SET_DISPLAY_RIGHT_MENU
} from "./applicationActionTypes.es6";

export const initialState = {
	style: {
		backgroundColor: 'green',
	},
	displayRightMenu: {
		display: 'none'
	},
	displayRightChat: {
		display: 'none'
	},
	displayRightTodo: {
		display: 'none'
	}
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
			return Object.assign({}, state, {displayRightMenu: {display: 'box'}},
				{displayRightChat: {display: 'none'}}, {displayRightTodo: {display: 'none'}});
		case SET_DISPLAY_RIGHT_CHAT :
			return Object.assign({}, state, {displayRightChat: {display: 'box'}},
				{displayRightMenu: {display: 'none'}}, {displayRightTodo: {display: 'none'}});
		case SET_DISPLAY_RIGHT_TODO :
			return Object.assign({}, state, {displayRightTodo: {display: 'box'}},
				{displayRightMenu: {display: 'none'}}, {displayRightChat: {display: 'none'}});
		
		default:
			return state;
	}
}