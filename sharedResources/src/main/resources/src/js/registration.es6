import {emailRegexpCheck, isNonEnabledUserEmailExist} from "./verifications.es6";
import {setUserMessage} from "./userMessaging.es6";

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
	
	if (repeatedActivationEmailInput.style.color === "red" || !repeatedActivationEmailInput.value) {
		emailReactivationMessageSpan.style.display = "block";
		emailReactivationMessageSpan.innerHTML = nonEmailMessage;
		return
	}
	isNonEnabledUserEmailExist(repeatedActivationEmailInput.value)
		.then(promise => {
			if (promise.exist) {
				promise.json()
					.then((json) => {
						emailReactivationMessageSpan.style.display = "none";
						let userMessage = JSON.stringify(json);
						setUserMessage(JSON.parse(userMessage)['userMessage']);
						console.log(JSON.parse(userMessage)['userMessage']);
						history.back();
					});
			} else {
				promise.json()
					.then((json) => {
						let userMessage = JSON.stringify(json);
						emailReactivationMessageSpan.style.display = "block";
						emailReactivationMessageSpan.innerHTML = JSON.parse(userMessage)['userMessage'];
					})
			}
		})
});