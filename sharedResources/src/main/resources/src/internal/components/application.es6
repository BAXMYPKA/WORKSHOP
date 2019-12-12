import React from "react";
import {render} from "react-dom";
import {createStore} from "redux";
import {Provider} from "react-redux";
import './i18n.es6';
import applicationReducer, {initialState} from "../redux/applicationReducer.es6";
import MainContainer from "./MainContainer.jsx";


export const store = createStore(applicationReducer, initialState);


render(
	<Provider store={store}>
		<MainContainer/>
	</Provider>,
	document.getElementById("root")
);
