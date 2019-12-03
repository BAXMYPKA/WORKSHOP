import React from "react";
import {SET_DISPLAY_BLOCK, SET_DISPLAY_NONE} from "./applicationActionTypes.es6";

export const initialState = {
	// display: 'none'
};

export default function applicationReducer(state = initialState, action) {
	
	const newState = {};
	
	switch (action.type) {
		case SET_DISPLAY_BLOCK :
			return Object.assign({}, state, {display : 'block'});
			// state.style = {
			// 	display: 'block'
			// };
			break;
		case SET_DISPLAY_NONE :
			state.style = {
				display: 'none'
			};
			break;
	}
	
	return state;
}