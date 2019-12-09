import React from "react";
import style from "./bottomBlackLine.css";
import {connect} from "react-redux";
import Button from "../common/Button.jsx";
import PowerButton from "./PowerButton.jsx";
import Span from "../common/Span.jsx";
import {setDisplayRightChat, setDisplayRightMenu, setDisplayRightTodo} from "../applicationActions.es6";

function mapDispatchToProps(dispatch) {
	return {
		setDisplayRightMenu: () => {
			dispatch(setDisplayRightMenu());
		},
		setDisplayRightChat: () => {
			dispatch(setDisplayRightChat());
		},
		setDisplayRightTodo: () => {
			dispatch(setDisplayRightTodo());
		}
	};
}

class rightBottomMenu extends React.Component {
	constructor(props) {
		super(props);
	}
	
	render() {
		return <div className={style.rightBottomMenu}>
			<div className={style.menuButtonDiv}>
				<Span className={style.menuName} text={'Menu'}/>
				<Button className={style.menuButton} onClick={this.props.setDisplayRightMenu}/>
			</div>
			<div className={style.menuButtonDiv}>
				<Span className={style.menuName} text={'Todos'}/>
				<Button className={style.menuButton} onClick={this.props.setDisplayRightTodo}/>
			</div>
			<div className={style.menuButtonDiv}>
				<Span className={style.menuName} text={'Chat'}/>
				<Button className={style.menuButton} onClick={this.props.setDisplayRightChat}/>
			</div>
			<div className={style.separator}>
			</div>
			<PowerButton className={style.powerButton}/>
		</div>;
	}
};
const RightBottomMenu = connect(null, mapDispatchToProps)(rightBottomMenu);
export default RightBottomMenu;
