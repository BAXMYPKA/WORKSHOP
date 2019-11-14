import {emailRegexpCheck, isNonEnabledUserEmailExist} from "./verifications.es6";

const nonEmailMessage = "Введенный адрес не похож на правильный!";
const repeatedActivationEmailInput = document.querySelector("#repeatedActivationEmail");
const buttonResendActivation = document.querySelector("#buttonResendActivation");

repeatedActivationEmailInput.addEventListener("input", (inputEvent) => {
	
	if (!emailRegexpCheck(inputEvent.currentTarget.value)) {
		repeatedActivationEmailInput.style.color = "red";
	} else {
		repeatedActivationEmailInput.style.color = "green";
	}
});

buttonResendActivation.addEventListener("click", (clickEvent) => {
	clickEvent.preventDefault();
	const emailReactivationMessageSpan = document.querySelector("#emailReactivationUserMessage");
	
	if (repeatedActivationEmailInput.style.color === "red") {
		emailReactivationMessageSpan.style.display = "block";
		emailReactivationMessageSpan.innerHTML = nonEmailMessage;
		return
	}
	isNonEnabledUserEmailExist(repeatedActivationEmailInput.value)
		.then(promise => {
			if (promise.exist) {
				emailReactivationMessageSpan.style.display = "none";
				console.log("EXIST");
			} else {
				console.log("NOT EXIST");
				promise.json()
					.then((json) => {
						let userMessage = JSON.stringify(json);
						emailReactivationMessageSpan.style.display = "block";
						emailReactivationMessageSpan.innerHTML = JSON.parse(userMessage)['userMessage'];
					})
			}
		})
	
});