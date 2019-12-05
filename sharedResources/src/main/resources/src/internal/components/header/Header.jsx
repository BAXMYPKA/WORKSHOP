import React from "react";
import SearchForm from "./SearchForm.jsx";
import style from "./header.css";
import Button from "../common/Button.jsx";
import SearchInput from "./SearchInput.jsx";
import {connect} from "react-redux";


import {setBackgroundColor} from "../applicationActions.es6";
import {store} from "../application.es6";


const mapStateToProps = state => {
	return {
		styles: {
			backgroundColor: state.style.backgroundColor
		}
	}
};

const mapDispatchToProps = function () {
	return {
		toggleBackground: function () {
			store.dispatch(
				setBackgroundColor('yellow')
			)
		}
	}
}

class header extends React.Component {
	constructor(props) {
		super(props);
		this.state = {
			styles: {
				backgroundColor: store.getState().style.backgroundColor
			}
		};
		this.changeBackground = this.changeBackground.bind(this);
	}
	

changeBackground() {
		store.dispatch(setBackgroundColor('yellow'));
		this.setState({styles: store.getState().style});
	}
	
	render() {
		return (
			<header className={style.header} style={this.state.styles}>
				<SearchInput/>
				<Button text={'BUTTON'} click={this.props.toggleBackground}/>
			</header>
		);
	}
}

const Header = connect(mapStateToProps, mapDispatchToProps)(header);

export default Header;

