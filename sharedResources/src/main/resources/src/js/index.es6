import {passwordCheck} from './passwordCheck.es6';
import {emailRegexpCheck, userEmailExist} from "./emailCheck.es6";

const PASSWORD_ERROR_MESSAGE = "Требуется минимум 5 знаков!";
const USER_EMAIL_ERROR_MESSAGE = "Имя должно соответствовать формату электронного адреса!";
const USER_NOT_FOUND = "Такого пользователя в базе не обнаружено!";
const usernameInput = document.querySelector("#inputUsername");
const passwordInput = document.querySelector("#inputPassword");
const passwordErrorMessageSpan = document.querySelector("#passwordErrorMessage");
const userErrorMessageSpan = document.querySelector("#userErrorMessage");

passwordInput.addEventListener("input", (env) => {
	if (passwordCheck(passwordInput.value) === true) {
		passwordInput.style.color = "green";
		passwordInput.removeAttribute("title");
		passwordErrorMessageSpan.style.display = "none";
	} else {
		passwordInput.style.color = "red";
		passwordInput.setAttribute("title", PASSWORD_ERROR_MESSAGE);
		passwordErrorMessageSpan.style.display = "block";
		passwordErrorMessageSpan.innerHTML = PASSWORD_ERROR_MESSAGE;
	}
});

usernameInput.addEventListener("input", (evt) => {
	if (!emailRegexpCheck(usernameInput.value)) {
		usernameInput.setAttribute("title", USER_EMAIL_ERROR_MESSAGE);
		usernameInput.style.color = "red";
	} else if (emailRegexpCheck(usernameInput.value) && !userEmailExist(usernameInput.value)) {
		usernameInput.removeAttribute("title");
		usernameInput.style.color = "green";
		passwordErrorMessageSpan.style.display = "block";
		passwordErrorMessageSpan.innerHTML = USER_NOT_FOUND;
	} else if (userEmailExist(usernameInput.value)){
		usernameInput.removeAttribute("title");
		usernameInput.style.color = "green";
		passwordErrorMessageSpan.style.display = "none";
	}
});