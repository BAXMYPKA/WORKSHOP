import React from "react";
import {render} from "react-dom";

function HelloWorkshop() {
	return (
		<div><h1>Hello Workshop Internal!</h1></div>
	)
};

render(<HelloWorkshop/>, document.getElementById("root"));