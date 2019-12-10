import React from "react";
import {connect} from "react-redux";

const mapStateToProps = state => {
	return {};
};

class centerLeftOrdersView extends React.Component {
	constructor(props) {
		super(props);
	}
	
	render() {
		return (
			<div>
				ORDERS VIEW
			</div>
		);
	}
};

let CenterLeftOrdersView = connect(mapStateToProps)(centerLeftOrdersView);
export default CenterLeftOrdersView;