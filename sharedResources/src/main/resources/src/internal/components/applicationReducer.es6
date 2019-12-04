import React from "react";
import {SET_DISPLAY_BLOCK, SET_DISPLAY_NONE, SET_BACKGROUND_COLOR} from "./applicationActionTypes.es6";

export const initialState = {
	style: {
		backgroundColor: 'green'
	}
};

export default function applicationReducer(state = initialState, action) {
	
	switch (action.type) {
		case SET_DISPLAY_BLOCK :
			return Object.assign({}, state, {styleDisplay:{display : 'block'}});
		case SET_DISPLAY_NONE :
			return Object.assign({}, state, {styleDisplay:{display: 'none'}});
		case SET_BACKGROUND_COLOR :
			return Object.assign({}, state, {style: {backgroundColor : action.payload.color}});
		default: return state;
	}
}