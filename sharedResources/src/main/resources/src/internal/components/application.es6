import React from "react";
import {render} from "react-dom";
import Header from "./header/Header.jsx";

function InternalWorkshop() {
	return (
		<div>
			<Header/>
			<h1>Hello Workshop Internal!</h1>
			
		</div>
	);
};

render(<InternalWorkshop/>, document.getElementById("root"));