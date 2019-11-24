import {emailRegexpCheck, isUserEmailExist, passwordCheck} from "./verifications.es6";

const inputUsername = document.querySelector("#inputUsername");
const inputPassword = document.querySelector("#inputPassword");

inputUsername.addEventListener("input", (evt) => {
	const USER_EMAIL_INCORRECT_ERROR_MESSAGE = "Имя должно соответствовать\nформату электронного адреса!";
	const USER_NOT_FOUND_ERROR_MESSAGE = "Пользователь не найден!";
	const userErrorMessageSpan = document.querySelector("#userErrorMessage");
	
	if (!emailRegexpCheck(inputUsername.value)) {
		inputUsername.setAttribute("title", USER_EMAIL_INCORRECT_ERROR_MESSAGE);
		inputUsername.style.color = "red";
		userErrorMessageSpan.style.display = "none";
		return;
	} else {
		inputUsername.removeAttribute("title");
		inputUsername.style.color = "green";
		userErrorMessageSpan.style.display = "none";
	}
	isUserEmailExist(inputUsername.value)
		.then((exist) => {
			if (exist.exist) {
				userErrorMessageSpan.style.display = "none";
			} else {
				userErrorMessageSpan.style.display = "block";
				userErrorMessageSpan.innerHTML = USER_NOT_FOUND_ERROR_MESSAGE;
			}
		});
});

inputPassword.addEventListener("input", (evt) => {
	const PASSWORD_INCORRECT_ERROR_MESSAGE = "Требуется минимум 5 знаков!";
	const passwordErrorMessageSpan = document.querySelector("#passwordErrorMessage");
	
	if (passwordCheck(inputPassword.value) === true) {
		inputPassword.style.color = "green";
		inputPassword.removeAttribute("title");
		passwordErrorMessageSpan.style.display = "none";
	} else {
		inputPassword.style.color = "red";
		inputPassword.setAttribute("title", PASSWORD_INCORRECT_ERROR_MESSAGE);
		passwordErrorMessageSpan.style.display = "block";
		passwordErrorMessageSpan.innerHTML = PASSWORD_INCORRECT_ERROR_MESSAGE;
	}
});

document.querySelector(".buttonResetPassword").addEventListener("click", (evt) => {
	evt.preventDefault();
	location.href = location.origin + "/workshop.pro/password-reset";
});

document.querySelector("#buttonRegistration").addEventListener("click", evt => {
	evt.preventDefault();
	location.href = location.origin + "/workshop.pro/registration";
});

