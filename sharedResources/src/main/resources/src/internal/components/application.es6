import React from "react";
import {render} from "react-dom";
import './i18n.es6';

import MainContainer from "./MainContainer.jsx";


import articleProps from "./articleProps.es6";
import htmlProps from "./htmlProps.es6";

import {createStore} from "redux";


render(<MainContainer htmlProps={htmlProps}/>, document.getElementById("root"));
