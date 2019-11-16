import {emailRegexpCheck} from "./verifications.es6";
import {checkWorkshopEntityExist, passwordResetEmail} from "./workshopEntitiesFetches.es6";
import {setUserMessage} from "./userMessaging.es6";

/**
 * If a logged and authenticated Users requested the page
 */
if (document.querySelector("#loggedUsersResetForm") !== null) {
	//
}

/**
 * If a not logged User requested the page
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
								})
						})
				} else {
					emailInput.style.color = "red";
					userErrorMessageEmail.style.display = "block";
					userErrorMessageEmail.textContent = emailNotExistMessage;
				}
			})
	})
}

