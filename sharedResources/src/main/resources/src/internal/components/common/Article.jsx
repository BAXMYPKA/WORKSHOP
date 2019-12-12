import React from "react";
import Button from "./Button.jsx";

export default class Article extends React.Component {
	constructor(props) {
		super(props);
		
			this.state = {
			isOpen: false
		};
		this.closeClick = () => {
			this.setState({isOpen: !this.state.isOpen});
		};
	}
	
	render() {
		
		return (
			<article>
				<p><i>
					Текст
				</i></p>
			</article>
		);
	}
	
}