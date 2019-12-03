import React from "react";
import {render} from "react-dom";
import {createStore} from "redux";
import './i18n.es6';
import applicationReducer, {initialState} from "./applicationReducer.es6";

import MainContainer from "./MainContainer.jsx";

const store = createStore(applicationReducer, initialState);

render(<MainContainer/>, document.getElementById("root"));
