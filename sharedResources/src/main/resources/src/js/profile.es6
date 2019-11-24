import {deletePhone, addPhone} from "./phoneFetch.es6";
import {phoneNumberCheck, phoneNameCheck} from "./verifications.es6";
import {deleteUserPhoto} from "./photoFetch.es6";

const PHONE_DELETION_ERROR_MESSAGE = "Не удалось удалить телефон!";
const PHONE_NAME_OR_NUMBER_ERROR_MESSAGE = "Имя или номер телефона содержат неверный формат!";
let phoneIdToOperateOn;
let phoneNameInput = document.querySelector('input[name="newPhoneName"]');
let phoneNumberInput = document.querySelector('input[name="newPhoneNumber"]');

document.querySelector("#changePasswordButton").addEventListener("click", (env) => {
	env.preventDefault();
	location.href = "/workshop.pro/password-reset";
});

document.querySelectorAll(".deleteButton").forEach(function (button, key, parent) {
	
	const userMessageContainer = document.querySelector("#userMessageContainer");
	const userMessageSpan = document.querySelector("#userMessage");
	
	if (button.id === 'deletePhotoButton') {
		button.addEventListener('click', (buttonEvent) => {
			buttonEvent.preventDefault();
			const userPhotoImg = document.querySelector("#userPhoto");
			let scr = userPhotoImg.src;
			deleteUserPhoto(scr)
				.then((promise) => {
					if (promise.ok) {
						userMessageContainer.hidden = true;
						let userPhotoImg = document.querySelector("#userPhoto");
						userPhotoImg.src = "../dist/img/bicycle-logo.jpg";
					} else {
						promise.json()
							.then((json) => {
								userMessageContainer.hidden = false;
								userMessageSpan.textContent = JSON.parse(json)['userMessage'];
							});
					}
				})
		});
		return;
	};
	
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

document.querySelector(".addButton").addEventListener("click", (buttonEvent) => {
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
			if (result.ok) {
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
					if (json['phone']) {
						phoneErrorMessageSpan.textContent = "Телефон: " + json['phone'];
					}
					if (json['name']) {
						phoneErrorMessageSpan.textContent = " Имя: " + json['name'];
					}
				});
			}
		})
		.catch((reject) => {
			let phoneErrorTd = document.querySelector("#phoneErrorIdNew");
			phoneErrorTd.hidden = false;
			let phoneErrorMessageSpan = document.querySelector("#phoneErrorNew");
			phoneErrorMessageSpan.textContent = "NETWORK ERROR";
		});
});