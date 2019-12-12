import React from "react";
import {connect} from "react-redux";

const mapStateToProps = state => {
	return {};
};

class centerLeftArticlesView extends React.Component {
	constructor(props) {
		super(props);
	}
	
	render() {
		return (
			<div>
				ARTICLES VIEW
			</div>
		);
	}
};

let CenterLeftArticlesView = connect(mapStateToProps)(centerLeftArticlesView);
export default CenterLeftArticlesView;