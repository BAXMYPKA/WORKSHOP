import {emailRegexpCheck, passwordCheck} from "./verifications.es6";
import {checkWorkshopEntityExist, passwordResetEmail} from "./workshopEntitiesFetches.es6";
import {setUserMessage} from "./userMessaging.es6";

/**
 * If an authenticated or Users with UUID requested the page
 */
if (document.querySelector("#loggedUsersResetForm") !== null) {
	
	const passwordInput = document.querySelector("#inputPassword");
	const passwordConfirmInput = document.querySelector("#inputConfirmPassword");
	const userErrorPassword = document.querySelector("#userErrorPasswordMessage");
	const userErrorConfirmPassword = document.querySelector("#userErrorConfirmPasswordMessage");
	const errorMessageNull = "Все поля должны быть заполнены!";
	const errorMessagePasswordsCoincidence = "Пароли должны совпадать!";
	const errorMessagePasswordRules = "Пароль должен содержать минимум 5 знаков без пробелов!";
	
	passwordInput.addEventListener("input", evt => {
		
		userErrorPassword.style.display = "none";
		
		if (!passwordCheck(evt.currentTarget.value)) {
			passwordInput.style.color = "red";
		} else {
			passwordInput.style.color = "green";
		}
	});
	
	passwordConfirmInput.addEventListener("input", evt => {
		
		userErrorConfirmPassword.style.display = "none";
		
		if (!passwordCheck(passwordConfirmInput.value)) {
			passwordConfirmInput.style.color = "red";
		} else {
			passwordConfirmInput.style.color = "green";
		}
	});
	
	document.querySelector(".buttonLogin").addEventListener("click", evt => {
		evt.preventDefault();
		if (passwordInput.value === null || passwordConfirmInput.value === null ||
			passwordInput.value === "" || passwordConfirmInput.value === "") {
			userErrorPassword.style.display = "block";
			userErrorPassword.textContent = errorMessageNull;
			return;
		} else if (passwordInput.value !== passwordConfirmInput.value) {
			userErrorConfirmPassword.style.display = "block";
			userErrorConfirmPassword.textContent = errorMessagePasswordsCoincidence;
			return;
		} else if (userErrorPassword.style.color !== "green" && userErrorConfirmPassword.style.color !== "green") {
			userErrorPassword.style.display = "block";
			userErrorPassword.textContent = errorMessagePasswordRules;
			return;
		}
		document.querySelector("#loggedUsersResetForm").submit();
	});
}

/**
 * If a not logged User requested the page for get an email with password reset UUID
 */
if (document.querySelector("#notLoggedUsersForm") !== null) {
	
	const emailInput = document.querySelector("#userEmail");
	const buttonLogin = document.querySelector(".buttonLogin");
	const userErrorMessageEmail = document.querySelector("#userErrorMessageEmail");
	const emailNotExistMessage = "Такого адреса в базе нет!";
	
	document.querySelector("#userEmail").addEventListener("input", (evn) => {
		if (!emailRegexpCheck(emailInput.value)) {
			emailInput.style.color = "red";
			buttonLogin.disabled = true;
		} else {
			emailInput.style.color = "green";
			buttonLogin.disabled = false;
		}
	});
	
	document.querySelector(".buttonLogin").addEventListener("click", (env) => {
		env.preventDefault();
		checkWorkshopEntityExist('User', 'email', emailInput.value)
			.then(promise => {
				if (promise.ok) {
					passwordResetEmail(emailInput.value)
						.then(promise => {
							promise.json()
								.then(json => {
									emailInput.style.color = "green";
									userErrorMessageEmail.textContent = "";
									userErrorMessageEmail.style.display = "none";
									let userMessage = JSON.stringify(json);
									setUserMessage(JSON.parse(userMessage)['userMessage']);
								});
						});
				} else {
					emailInput.style.color = "red";
					userErrorMessageEmail.style.display = "block";
					userErrorMessageEmail.textContent = emailNotExistMessage;
				}
			});
	});
}

