import {emailRegexpCheck} from "./verifications.es6";
import {checkWorkshopEntityExist, passwordResetEmail} from "./workshopEntitiesFetches.es6";

if (document.querySelector("#loggedUsersResetForm") !== null) {
	//
}

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
									console.log(json);
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

