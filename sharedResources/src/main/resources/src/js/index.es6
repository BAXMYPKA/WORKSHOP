import {emailRegexpCheck, isUserEmailExist, passwordCheck} from "./verifications.es6";

const usernameInput = document.querySelector("#inputUsername");
const passwordInput = document.querySelector("#inputPassword");

usernameInput.addEventListener("input", (evt) => {
	const USER_EMAIL_INCORRECT_ERROR_MESSAGE = "Имя должно соответствовать\nформату электронного адреса!";
	const USER_NOT_FOUND_ERROR_MESSAGE = "Пользователь не найден!";
	const userErrorMessageSpan = document.querySelector("#userErrorMessage");
	
	if (!emailRegexpCheck(usernameInput.value)) {
		usernameInput.setAttribute("title", USER_EMAIL_INCORRECT_ERROR_MESSAGE);
		usernameInput.style.color = "red";
		userErrorMessageSpan.style.display = "none";
		return;
	} else {
		usernameInput.removeAttribute("title");
		usernameInput.style.color = "green";
		userErrorMessageSpan.style.display = "none";
	}
	isUserEmailExist(usernameInput.value)
		.then((exist) => {
			if (exist.exist) {
				userErrorMessageSpan.style.display = "none";
			} else {
				userErrorMessageSpan.style.display = "block";
				userErrorMessageSpan.innerHTML = USER_NOT_FOUND_ERROR_MESSAGE;
			}
		});
});

passwordInput.addEventListener("input", (env) => {
	const PASSWORD_INCORRECT_ERROR_MESSAGE = "Требуется минимум 5 знаков!";
	const passwordErrorMessageSpan = document.querySelector("#passwordErrorMessage");
	
	if (passwordCheck(passwordInput.value) === true) {
		passwordInput.style.color = "green";
		passwordInput.removeAttribute("title");
		passwordErrorMessageSpan.style.display = "none";
	} else {
		passwordInput.style.color = "red";
		passwordInput.setAttribute("title", PASSWORD_INCORRECT_ERROR_MESSAGE);
		passwordErrorMessageSpan.style.display = "block";
		passwordErrorMessageSpan.innerHTML = PASSWORD_INCORRECT_ERROR_MESSAGE;
	}
});

document.querySelector(".buttonResetPassword").addEventListener("click", (env) => {
	env.preventDefault();
	location.href = location.origin + "/workshop.pro/password-reset";
});

