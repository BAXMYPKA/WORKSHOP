import React from "react";
import {render} from "react-dom";

import MainContainer from "./MainContainer.jsx";

import articleProps from "./articleProps.es6";
import htmlProps from "./htmlProps.es6";

import {createStore} from "redux";

///////////////////////

let initialState = {count: 0};
const store = createStore(reducer, initialState);
const incrementAction = {type: 'INCREMENT', payload: 1};
const decrementAction = {type: 'DECREMENT', payload: -1};

function reducer(state = {count: 0}, action) {
	if (action.type === 'INCREMENT') {
		// console.log(state.count);
		return {count: state.count + Number(action.payload)};
	} else if (action.type === 'DECREMENT') {
		// console.log(state.count);
		return {count: state.count + Number(action.payload)};
	} else if (action.type === 'RESET') {
		return {count: 0};
	}
}

class Counter extends React.Component {
	constructor(props) {
		super(props);
		this.props = props;
		this.increment = this.increment.bind(this);
		this.decrement = this.decrement.bind(this);
	}
	
	increment() {
		store.dispatch(incrementAction);
	}
	
	decrement() {
		store.dispatch(decrementAction);
	}
	
	reset() {
		store.dispatch({type: 'RESET'});
	}
	
	componentDidMount() {
		store.subscribe(() => this.forceUpdate());
	}
	
	render() {
		const counter = store.getState() ? store.getState().count : 0;
		if (store.getState()) {
			console.log(store.getState().count);
			
		}
		return (
			<div>
				<div>{counter}</div>
				<Button text={'-'} value={-1} onclick={this.decrement}/>
				<Button text={'+'} value={1} onclick={this.increment}/>
				<Button text={'reset'} onclick={this.reset}/>
			</div>
		);
	}
}

class Button extends React.Component {
	constructor(props) {
		super(props);
	}
	
	render() {
		return <button value={this.props.value} onClick={this.props.onclick}>
			{this.props.text}
		</button>;
	}
}

render(<Counter/>, document.querySelector("#root"));

//////////////////////

// render(<MainContainer htmlProps={htmlProps}/>, document.getElementById("root"));
