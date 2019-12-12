import React from "react";
import {connect} from "react-redux";

const mapStateToProps = state => {
	return {};
};

class centerLeftTasksView extends React.Component {
	constructor(props) {
		super(props);
	}
	
	render() {
		return (
			<div>
				TASKS VIEW
			</div>
		);
	}
};

let CenterLeftTasksView = connect(mapStateToProps)(centerLeftTasksView);
export default CenterLeftTasksView;