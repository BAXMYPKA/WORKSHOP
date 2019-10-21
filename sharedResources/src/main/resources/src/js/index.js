import {passwordCheck} from './passwordCheck';

const usernameInput = document.querySelector("#inputUsername");
const passwordInput = document.querySelector("#inputPassword");

passwordInput.addEventListener("input", (env) => {
	if (passwordCheck(passwordInput.value) === true) {
		passwordInput.style.color = "green";
		passwordInput.removeAttribute("title");
	} else {
		passwordInput.style.color = "red";
		passwordInput.setAttribute("title", "At least 5 symbols required!");
	}
});
