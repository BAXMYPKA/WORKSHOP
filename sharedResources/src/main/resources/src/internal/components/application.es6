import React from "react";
import {render} from "react-dom";

import Header from "./header/Header.jsx";
import Article from "./article/Article.jsx";

import articleProps from "./articleProps.es6";
import htmlProps from "./htmlProps.es6";

function Application() {
	return (
		<div>
			<Header/>
			<h1>Hello Workshop Internal!</h1>
			<Article article={articleProps[0]} html={htmlProps}/>
			<Article article={articleProps[1]} html={htmlProps}/>
		</div>
	);
};

render(<Application/>, document.getElementById("root"));
