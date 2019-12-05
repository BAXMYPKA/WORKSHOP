import React from "react";
import {render} from "react-dom";
import {createStore} from "redux";
import {Provider} from "react-redux";
import './i18n.es6';
import applicationReducer, {initialState} from "./applicationReducer.es6";

import MainContainer from "./MainContainer.jsx";
import {setDisplayBlockStyle, setDisplayNoneStyle, setBackgroundColor} from "./applicationActions.es6";
import {whenMergePropsIsOmitted} from "react-redux/lib/connect/mergeProps";

export const store = createStore(applicationReducer, initialState);

////////////////

window.store = store;
window.setDisplayBlock = setDisplayBlockStyle;
window.setDisplayNone = setDisplayNoneStyle;
window.setBackgroundColor = setBackgroundColor;

/////////////

render(
	<Provider store={store}>
		<MainContainer />
	</Provider>,
	document.getElementById("root")
);
