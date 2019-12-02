import React from "react";
import SearchForm from "./SearchForm.jsx";
import style from "./header.css"

export default function Header(props) {
	return(
		<header className={style.header}>
			HEADER+' '+<br/>
			<SearchForm/>
		</header>
	)
}