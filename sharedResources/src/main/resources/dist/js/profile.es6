/******/ (function(modules) { // webpackBootstrap
/******/ 	// The module cache
/******/ 	var installedModules = {};
/******/
/******/ 	// The require function
/******/ 	function __webpack_require__(moduleId) {
/******/
/******/ 		// Check if module is in cache
/******/ 		if(installedModules[moduleId]) {
/******/ 			return installedModules[moduleId].exports;
/******/ 		}
/******/ 		// Create a new module (and put it into the cache)
/******/ 		var module = installedModules[moduleId] = {
/******/ 			i: moduleId,
/******/ 			l: false,
/******/ 			exports: {}
/******/ 		};
/******/
/******/ 		// Execute the module function
/******/ 		modules[moduleId].call(module.exports, module, module.exports, __webpack_require__);
/******/
/******/ 		// Flag the module as loaded
/******/ 		module.l = true;
/******/
/******/ 		// Return the exports of the module
/******/ 		return module.exports;
/******/ 	}
/******/
/******/
/******/ 	// expose the modules object (__webpack_modules__)
/******/ 	__webpack_require__.m = modules;
/******/
/******/ 	// expose the module cache
/******/ 	__webpack_require__.c = installedModules;
/******/
/******/ 	// define getter function for harmony exports
/******/ 	__webpack_require__.d = function(exports, name, getter) {
/******/ 		if(!__webpack_require__.o(exports, name)) {
/******/ 			Object.defineProperty(exports, name, { enumerable: true, get: getter });
/******/ 		}
/******/ 	};
/******/
/******/ 	// define __esModule on exports
/******/ 	__webpack_require__.r = function(exports) {
/******/ 		if(typeof Symbol !== 'undefined' && Symbol.toStringTag) {
/******/ 			Object.defineProperty(exports, Symbol.toStringTag, { value: 'Module' });
/******/ 		}
/******/ 		Object.defineProperty(exports, '__esModule', { value: true });
/******/ 	};
/******/
/******/ 	// create a fake namespace object
/******/ 	// mode & 1: value is a module id, require it
/******/ 	// mode & 2: merge all properties of value into the ns
/******/ 	// mode & 4: return value when already ns object
/******/ 	// mode & 8|1: behave like require
/******/ 	__webpack_require__.t = function(value, mode) {
/******/ 		if(mode & 1) value = __webpack_require__(value);
/******/ 		if(mode & 8) return value;
/******/ 		if((mode & 4) && typeof value === 'object' && value && value.__esModule) return value;
/******/ 		var ns = Object.create(null);
/******/ 		__webpack_require__.r(ns);
/******/ 		Object.defineProperty(ns, 'default', { enumerable: true, value: value });
/******/ 		if(mode & 2 && typeof value != 'string') for(var key in value) __webpack_require__.d(ns, key, function(key) { return value[key]; }.bind(null, key));
/******/ 		return ns;
/******/ 	};
/******/
/******/ 	// getDefaultExport function for compatibility with non-harmony modules
/******/ 	__webpack_require__.n = function(module) {
/******/ 		var getter = module && module.__esModule ?
/******/ 			function getDefault() { return module['default']; } :
/******/ 			function getModuleExports() { return module; };
/******/ 		__webpack_require__.d(getter, 'a', getter);
/******/ 		return getter;
/******/ 	};
/******/
/******/ 	// Object.prototype.hasOwnProperty.call
/******/ 	__webpack_require__.o = function(object, property) { return Object.prototype.hasOwnProperty.call(object, property); };
/******/
/******/ 	// __webpack_public_path__
/******/ 	__webpack_require__.p = "";
/******/
/******/
/******/ 	// Load entry module and return exports
/******/ 	return __webpack_require__(__webpack_require__.s = "./src/js/profile.es6");
/******/ })
/************************************************************************/
/******/ ({

/***/ "./src/js/phoneFetch.es6":
/*!*******************************!*\
  !*** ./src/js/phoneFetch.es6 ***!
  \*******************************/
/*! exports provided: deletePhone, addPhone */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "deletePhone", function() { return deletePhone; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "addPhone", function() { return addPhone; });
function deletePhone(phoneId) {
	
	return fetch(`http://localhost:18080/workshop.pro/ajax/phones/${phoneId}`,
		{
			method: "DELETE",
			credentials: "same-origin"
		})
		.then((resolve) => {
			return resolve;
		})
		.catch((reject) => {
			return reject;
		});
}

function addPhone(phoneNum, phoneName) {
	let phoneForm = new FormData;
	phoneForm.append("name", phoneName);
	phoneForm.append("phone", phoneNum);
	
	return fetch("http://localhost:18080/workshop.pro/ajax/phones",
		{
			method: "POST",
			credentials: "same-origin",
			body: phoneForm
		});
};



/***/ }),

/***/ "./src/js/photoFetch.es6":
/*!*******************************!*\
  !*** ./src/js/photoFetch.es6 ***!
  \*******************************/
/*! exports provided: deleteUserPhoto */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "deleteUserPhoto", function() { return deleteUserPhoto; });
function deleteUserPhoto(src) {
	return fetch(src,
		{
			method: "delete",
			credentials: "same-origin"
		});
}

/***/ }),

/***/ "./src/js/profile.es6":
/*!****************************!*\
  !*** ./src/js/profile.es6 ***!
  \****************************/
/*! no exports provided */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var _phoneFetch_es6__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./phoneFetch.es6 */ "./src/js/phoneFetch.es6");
/* harmony import */ var _verifications_es6__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ./verifications.es6 */ "./src/js/verifications.es6");
/* harmony import */ var _photoFetch_es6__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ./photoFetch.es6 */ "./src/js/photoFetch.es6");




const PHONE_DELETION_ERROR_MESSAGE = "Не удалось удалить телефон!";
const PHONE_NAME_OR_NUMBER_ERROR_MESSAGE = "Имя или номер телефона содержат неверный формат!";
const deletePhoneButton = document.querySelectorAll(".deleteButton");
const addPhoneButton = document.querySelector(".addButton");
// const deletePhotoButton = document.querySelector("#deletePhotoButton");
let phoneIdToOperateOn;
let phoneNameInput = document.querySelector('input[name="newPhoneName"]');
let phoneNumberInput = document.querySelector('input[name="newPhoneNumber"]');


deletePhoneButton.forEach(function (button, key, parent) {
	
	const userMessageContainer = document.querySelector("#userMessageContainer");
	const userMessageSpan = document.querySelector("#userMessage");
	
	if (button.id === 'deletePhotoButton') {
		button.addEventListener('click', (buttonEvent) => {
			buttonEvent.preventDefault();
			const userPhotoImg = document.querySelector("#userPhoto");
			let scr = userPhotoImg.src;
			Object(_photoFetch_es6__WEBPACK_IMPORTED_MODULE_2__["deleteUserPhoto"])(scr)
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
		Object(_phoneFetch_es6__WEBPACK_IMPORTED_MODULE_0__["deletePhone"])(buttonEvent.currentTarget.value)
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
	if (Object(_verifications_es6__WEBPACK_IMPORTED_MODULE_1__["phoneNameCheck"])(inputEvent.currentTarget.value)) {
		phoneNameInput.style.color = "green";
	} else {
		phoneNameInput.style.color = "red";
	}
});

phoneNumberInput.addEventListener("input", (inputNumberEvent) => {
	if (Object(_verifications_es6__WEBPACK_IMPORTED_MODULE_1__["phoneNumberCheck"])(inputNumberEvent.currentTarget.value)) {
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
	
	Object(_phoneFetch_es6__WEBPACK_IMPORTED_MODULE_0__["addPhone"])(phoneNumberInput.value.toString().trim(), phoneNameInput.value)
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

/***/ }),

/***/ "./src/js/verifications.es6":
/*!**********************************!*\
  !*** ./src/js/verifications.es6 ***!
  \**********************************/
/*! exports provided: emailRegexpCheck, userEmailExist, passwordCheck, phoneNumberCheck, phoneNameCheck */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "emailRegexpCheck", function() { return emailRegexpCheck; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "userEmailExist", function() { return userEmailExist; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "passwordCheck", function() { return passwordCheck; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "phoneNumberCheck", function() { return phoneNumberCheck; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "phoneNameCheck", function() { return phoneNameCheck; });
/* harmony import */ var _workshopEntityExist_es6__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./workshopEntityExist.es6 */ "./src/js/workshopEntityExist.es6");


function emailRegexpCheck(email) {
	
	let emailRegexp = /^([^\s][\d]|[\w]){3,25}@([^\s][\d]|[\w]){2,15}\.([^\s][\d]|[\w]){2,10}$/i;
	
	if (typeof email === "string" && email.match(emailRegexp)) {
		return true;
	} else {
		return false;
	}
}

/**
 * Async function!
 *
 * @param userEmail
 * @returns {Promise<unknown>} with '.exist' additional boolean property.
 */
function userEmailExist(userEmail) {
	
	return Object(_workshopEntityExist_es6__WEBPACK_IMPORTED_MODULE_0__["default"])("User", "email", userEmail)
		.then((promise) => {
			promise.exist = promise.ok;
			return promise;
		});
}

function passwordCheck(password) {
	if ((typeof password === "string" || typeof password === "number") && password.length < 5) {
		return false;
	} else {
		return true;
	}
}

function phoneNumberCheck(phoneNumber) {
	
	let phoneNumberRegexp = /^[+(]?\s?[\d()\-^\s]{10,20}$/;
	
	if (typeof phoneNumber !== "string") {
		return false;
	} else {
		let stringNumber = phoneNumber.toString();
		return stringNumber.match(phoneNumberRegexp);
	}
}

function phoneNameCheck(phoneName) {
	
	let phoneNameRegexp = /^[\w\sа-яЁёА-Я]{2,15}$/;
	
	if (typeof phoneName !== "string") {
		return false;
	} else {
		return phoneName.toString().match(/^$/) || phoneName.match(phoneNameRegexp);
	}
}

/***/ }),

/***/ "./src/js/workshopEntityExist.es6":
/*!****************************************!*\
  !*** ./src/js/workshopEntityExist.es6 ***!
  \****************************************/
/*! exports provided: default */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "default", function() { return workshopEntityExist; });
/**
 *
 * @param workshopEntityType String representations for WorkshopEntityType to be found
 * @param propertyName The name of any existing property of that WorkshopEntity
 * @param propertyValue A value of that property
 * @returns {Promise<Response>} with status.ok === true or status.ok === false
 */
function workshopEntityExist(workshopEntityType = "default", propertyName = "default", propertyValue = "default") {
	
	const formData = new FormData();
	formData.append("workshopEntityType", workshopEntityType);
	formData.append("propertyName", propertyName);
	formData.append("propertyValue", propertyValue);
	
	return  fetch("http://localhost:18080/workshop.pro/ajax/entity-exist", {
		method: "POST",
		body: formData,
		// headers: new Headers({
		// 	"Content-Type": "application/x-www-form-urlencoded"
		// })
	}).then(function (promise) {
		return promise;
	});
}

/***/ })

/******/ });