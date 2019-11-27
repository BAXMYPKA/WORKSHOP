import React from "react";
import {render} from "react-dom";

import MainContainer from "./MainContainer.jsx";

import articleProps from "./articleProps.es6";
import htmlProps from "./htmlProps.es6";

render(<MainContainer htmlProps={htmlProps}/>, document.getElementById("root"));
