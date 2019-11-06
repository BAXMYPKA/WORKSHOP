import {deletePhone, addPhone} from "./phoneFetch.es6";
import {phoneNumberCheck, phoneNameCheck} from "./verifications.es6";

const PHONE_DELETION_ERROR_MESSAGE = "Не удалось удалить телефон!";
const PHONE_NAME_OR_NUMBER_ERROR_MESSAGE = "Имя или номер телефона содержат неверный формат!";
const deletePhoneButton = document.querySelectorAll(".deleteButton");
const addPhoneButton = document.querySelector(".addButton");
let phoneIdToOperateOn;
let phoneNameInput = document.querySelector('input[name="newPhoneName"]');
let phoneNumberInput = document.querySelector('input[name="newPhoneNumber"]');


deletePhoneButton.forEach(function (button, key, parent) {
	button.addEventListener("click", (buttonEvent) => {
		buttonEvent.preventDefault();
		phoneIdToOperateOn = buttonEvent.currentTarget.value;
		deletePhone(buttonEvent.currentTarget.value)
			.then((promise) => {
				if (promise.ok) {
					let rowToHide = document.getElementById(`phoneId=${phoneIdToOperateOn}`);
					rowToHide.style.display = "none";
				} else {
					let phoneErrorMessageId = document.getElementById(`phoneErrorId=${phoneIdToOperateOn}`);
					phoneErrorMessageId.innerHTML = PHONE_DELETION_ERROR_MESSAGE;
					phoneErrorMessageId.style.color = "red";
					phoneErrorMessageId.style.fontStyle = "italic";
				}
			});
	});
});

phoneNameInput.addEventListener("input", (inputEvent) => {
	if (phoneNameCheck(inputEvent.currentTarget.value)) {
		phoneNameInput.style.color = "green";
	} else {
		phoneNameInput.style.color = "red";
	}
});

phoneNumberInput.addEventListener("input", (inputNumberEvent) => {
	if (phoneNumberCheck(inputNumberEvent.currentTarget.value)) {
		phoneNumberInput.style.color = "green";
	} else {
		phoneNumberInput.style.color = "red";
	}
});

addPhoneButton.addEventListener("click", (buttonEvent) => {
	buttonEvent.preventDefault();
	
	if (phoneNameInput.style.color.match("red") || phoneNumberInput.style.color.match("red")) {
		let phoneErrorTd = document.querySelector("#phoneErrorIdNew");
		phoneErrorTd.hidden = false;
		let phoneErrorMessageSpan = document.querySelector("#phoneErrorNew");
		phoneErrorMessageSpan.textContent = PHONE_NAME_OR_NUMBER_ERROR_MESSAGE;
		return;
	} else {
		let phoneErrorTd = document.querySelector("#phoneErrorIdNew");
		phoneErrorTd.hidden = true;
	}
	
	addPhone(phoneNumberInput.value.toString().trim(), phoneNameInput.value)
		.then((result) => {
			if (result.ok){
				let phoneErrorTd = document.querySelector("#phoneErrorIdNew");
				phoneErrorTd.hidden = true;
				let phoneErrorMessageSpan = document.querySelector("#phoneErrorNew");
				phoneErrorMessageSpan.textContent = "";
				window.location.reload();
			} else {
				result.json().then(json => {
					let phoneErrorTd = document.querySelector("#phoneErrorIdNew");
					phoneErrorTd.hidden = false;
					let phoneErrorMessageSpan = document.querySelector("#phoneErrorNew");
					if (json['phone']){
						phoneErrorMessageSpan.textContent = "Телефон: "+json['phone'];
					}
					if (json['name']) {
						phoneErrorMessageSpan.textContent += " Имя: "+json['name'];
					}
				});
			}
		})
		.catch((reject) => {
			let phoneErrorTd = document.querySelector("#phoneErrorIdNew");
			phoneErrorTd.hidden = false;
			let phoneErrorMessageSpan = document.querySelector("#phoneErrorNew");
			phoneErrorMessageSpan.textContent = "NETWORK ERROR";
		})
});