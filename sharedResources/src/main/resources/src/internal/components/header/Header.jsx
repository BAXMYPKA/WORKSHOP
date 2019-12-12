import React from "react";
import style from "./header.css";
import Button from "../common/Button.jsx";
import SearchInput from "./SearchInput.jsx";
import {connect} from "react-redux";


export default class Header extends React.Component {
	constructor(props) {
		super(props);
	}
	

	render() {
		return (
			<header className={style.header}>
				<SearchInput/>
				<Button text={'BUTTON'}/>
			</header>
		);
	}
}


