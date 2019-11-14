import {emailRegexpCheck} from "./verifications.es6";

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
	
});