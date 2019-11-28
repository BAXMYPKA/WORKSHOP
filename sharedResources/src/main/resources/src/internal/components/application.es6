import React from "react";
import {render} from "react-dom";
import {createStore} from "redux";

import MainContainer from "./MainContainer.jsx";

import articleProps from "./articleProps.es6";
import htmlProps from "./htmlProps.es6";

const store = createStore(languageReducer);

function languageReducer(state = [], action) {
	console.log(`LANGUAGE ACTION = ${action.type} ${action.payload}`);
	if (action.type === 'CHANGE_LANG_EN') {
		return [
			...state,
			action.payload
		];
	}
	if (action.type === 'CHANGE_LANG_RU') {
		return [
			...state,
			action.payload
		];
	}
	return state;
}

console.log('STORE INIT STATE = ' + store.getState());

store.subscribe(() => {
	console.log(`STORE SUBSCRIBE = ${store.getState()}`);
	let testDiv = document.querySelector("#testDiv");
	testDiv.innerHTML = ' !!! ' + store.getState();
});

store.dispatch({type: 'CHANGE_LANG_EN', payload: 'eng'});
store.dispatch({type: 'CHANGE_LANG_RU', payload: 'ru'});

let testInput = document.querySelector("#testInput").addEventListener('input', (evt) => {
	// let testDiv = document.querySelector("#testDiv");
	// testDiv.innerHTML = evt.currentTarget.value;
	store.dispatch({type: 'CHANGE_LANG_RU', payload: evt.currentTarget.value})
});


render(<MainContainer htmlProps={htmlProps}/>, document.getElementById("root"));
