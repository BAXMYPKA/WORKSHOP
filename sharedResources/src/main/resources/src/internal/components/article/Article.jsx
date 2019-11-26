import React from "react";
import Button from "../common/Button.jsx";

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
		
		let textSection = this.state.isOpen ? <section>{this.props.article.text}</section> : null;
		
		return (
			<article>
				<h2>
					{this.props.article.header}
				</h2>
				<Button button={this.props.html[0]} closeClick={this.closeClick} buttonText={this.state.isOpen}/>
				{textSection}
				<p><i>
					{new Date(this.props.article.date).toDateString()}
				</i></p>
			</article>
		);
	}
	
}