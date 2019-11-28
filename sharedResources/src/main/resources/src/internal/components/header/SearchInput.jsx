import React from "react";

export default class SearchInput extends React.Component {
	constructor(props) {
		super(props);
	}
	
	render() {
		return (
			<input id={'searchInput'}
				type={'text'} placeholder={'search'} title={this.props.result} onInput={this.props.search}/>
		);
	}
};