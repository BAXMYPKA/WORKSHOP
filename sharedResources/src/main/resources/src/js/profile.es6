import {deletePhone} from "./phoneFetch.es6";

const PHONE_DELETION_ERROR_MESSAGE = "Не удалось удалить телефон!";
const deleteButtons = document.querySelectorAll(".deleteButton");
let phoneIdToOperateOn;

deleteButtons.forEach(function (button, key, parent) {
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
