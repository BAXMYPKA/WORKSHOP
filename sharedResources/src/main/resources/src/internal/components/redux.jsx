import React from "react";

export function createStore(reducer, initialState) {
	
	let state = initialState;
	let callbacks = [];
	
	const getState = () => state;
	
	const dispatch = (action) => {
		state = reducer(state, action);
		callbacks.forEach(callback => callback())
	};
	
	const subscribe = (callback) => {
		callbacks.push(callback);
		return callbacks.filter(func => func !== callback);
	};
	
	// dispatch({});
	
	return {getState, dispatch, subscribe}
}