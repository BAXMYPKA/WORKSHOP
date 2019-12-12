import React from "react";
import SearchInput from "./SearchInput.jsx";

export default class SearchForm extends React.Component {
	constructor(props){
		super(props);
		
		this.state = {
			result: "THE RESULT"
		};
		
		this.search = (event) => {
			this.setState({result: event.currentTarget.value});
		};
		this.search.bind(this);
	}
	
	
	render() {
		return (
			<div id={'searchForm'}>
				<SearchInput search={this.search} result={this.state.result}/>
			</div>
		);
	}
};