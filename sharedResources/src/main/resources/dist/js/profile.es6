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
  return fetch("http://localhost:18080/workshop.pro/ajax/phones/".concat(phoneId), {
    method: "DELETE",
    credentials: "same-origin"
  }).then(function (resolve) {
    return resolve;
  })["catch"](function (reject) {
    return reject;
  });
}
function addPhone(phoneNum, phoneName) {
  var phoneForm = new FormData();
  phoneForm.append("name", phoneName);
  phoneForm.append("phone", phoneNum);
  return fetch("http://localhost:18080/workshop.pro/ajax/phones", {
    method: "POST",
    credentials: "same-origin",
    body: phoneForm
  });
}
;

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
  return fetch(src, {
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



var PHONE_DELETION_ERROR_MESSAGE = "Не удалось удалить телефон!";
var PHONE_NAME_OR_NUMBER_ERROR_MESSAGE = "Имя или номер телефона содержат неверный формат!";
var phoneIdToOperateOn;
var phoneNameInput = document.querySelector('input[name="newPhoneName"]');
var phoneNumberInput = document.querySelector('input[name="newPhoneNumber"]');
document.querySelector("#changePasswordButton").addEventListener("click", function (env) {
  env.preventDefault();
  location.href = "/workshop.pro/password-reset";
});
document.querySelectorAll(".deleteButton").forEach(function (button, key, parent) {
  var userMessageContainer = document.querySelector("#userMessageContainer");
  var userMessageSpan = document.querySelector("#userMessage");

  if (button.id === 'deletePhotoButton') {
    button.addEventListener('click', function (buttonEvent) {
      buttonEvent.preventDefault();
      var userPhotoImg = document.querySelector("#userPhoto");
      var scr = userPhotoImg.src;
      Object(_photoFetch_es6__WEBPACK_IMPORTED_MODULE_2__["deleteUserPhoto"])(scr).then(function (promise) {
        if (promise.ok) {
          userMessageContainer.hidden = true;

          var _userPhotoImg = document.querySelector("#userPhoto");

          _userPhotoImg.src = "../dist/img/bicycle-logo.jpg";
        } else {
          promise.json().then(function (json) {
            userMessageContainer.hidden = false;
            userMessageSpan.textContent = JSON.parse(json)['userMessage'];
          });
        }
      });
    });
    return;
  }

  ;
  button.addEventListener("click", function (buttonEvent) {
    buttonEvent.preventDefault();
    phoneIdToOperateOn = buttonEvent.currentTarget.value;
    Object(_phoneFetch_es6__WEBPACK_IMPORTED_MODULE_0__["deletePhone"])(buttonEvent.currentTarget.value).then(function (promise) {
      if (promise.ok) {
        var rowToHide = document.getElementById("phoneId=".concat(phoneIdToOperateOn));
        rowToHide.style.display = "none";
      } else {
        var phoneErrorMessageId = document.getElementById("phoneErrorId=".concat(phoneIdToOperateOn));
        phoneErrorMessageId.innerHTML = PHONE_DELETION_ERROR_MESSAGE;
        phoneErrorMessageId.style.color = "red";
        phoneErrorMessageId.style.fontStyle = "italic";
      }
    });
  });
});
phoneNameInput.addEventListener("input", function (inputEvent) {
  if (Object(_verifications_es6__WEBPACK_IMPORTED_MODULE_1__["phoneNameCheck"])(inputEvent.currentTarget.value)) {
    phoneNameInput.style.color = "green";
  } else {
    phoneNameInput.style.color = "red";
  }
});
phoneNumberInput.addEventListener("input", function (inputNumberEvent) {
  if (Object(_verifications_es6__WEBPACK_IMPORTED_MODULE_1__["phoneNumberCheck"])(inputNumberEvent.currentTarget.value)) {
    phoneNumberInput.style.color = "green";
  } else {
    phoneNumberInput.style.color = "red";
  }
});
document.querySelector(".addButton").addEventListener("click", function (buttonEvent) {
  buttonEvent.preventDefault();

  if (phoneNameInput.style.color.match("red") || phoneNumberInput.style.color.match("red")) {
    var phoneErrorTd = document.querySelector("#phoneErrorIdNew");
    phoneErrorTd.hidden = false;
    var phoneErrorMessageSpan = document.querySelector("#phoneErrorNew");
    phoneErrorMessageSpan.textContent = PHONE_NAME_OR_NUMBER_ERROR_MESSAGE;
    return;
  } else {
    var _phoneErrorTd = document.querySelector("#phoneErrorIdNew");

    _phoneErrorTd.hidden = true;
  }

  Object(_phoneFetch_es6__WEBPACK_IMPORTED_MODULE_0__["addPhone"])(phoneNumberInput.value.toString().trim(), phoneNameInput.value).then(function (result) {
    if (result.ok) {
      var _phoneErrorTd2 = document.querySelector("#phoneErrorIdNew");

      _phoneErrorTd2.hidden = true;

      var _phoneErrorMessageSpan = document.querySelector("#phoneErrorNew");

      _phoneErrorMessageSpan.textContent = "";
      window.location.reload();
    } else {
      result.json().then(function (json) {
        var phoneErrorTd = document.querySelector("#phoneErrorIdNew");
        phoneErrorTd.hidden = false;
        var phoneErrorMessageSpan = document.querySelector("#phoneErrorNew");

        if (json['phone']) {
          phoneErrorMessageSpan.textContent = "Телефон: " + json['phone'];
        }

        if (json['name']) {
          phoneErrorMessageSpan.textContent = " Имя: " + json['name'];
        }
      });
    }
  })["catch"](function (reject) {
    var phoneErrorTd = document.querySelector("#phoneErrorIdNew");
    phoneErrorTd.hidden = false;
    var phoneErrorMessageSpan = document.querySelector("#phoneErrorNew");
    phoneErrorMessageSpan.textContent = "NETWORK ERROR";
  });
});

/***/ }),

/***/ "./src/js/verifications.es6":
/*!**********************************!*\
  !*** ./src/js/verifications.es6 ***!
  \**********************************/
/*! exports provided: emailRegexpCheck, isUserEmailExist, isNonEnabledUserEmailExist, passwordCheck, phoneNumberCheck, phoneNameCheck */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "emailRegexpCheck", function() { return emailRegexpCheck; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "isUserEmailExist", function() { return isUserEmailExist; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "isNonEnabledUserEmailExist", function() { return isNonEnabledUserEmailExist; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "passwordCheck", function() { return passwordCheck; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "phoneNumberCheck", function() { return phoneNumberCheck; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "phoneNameCheck", function() { return phoneNameCheck; });
/* harmony import */ var _workshopEntitiesFetches_es6__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./workshopEntitiesFetches.es6 */ "./src/js/workshopEntitiesFetches.es6");

/**
 * Still in development as I don't know all the permitted symbols in the email string
 * @param email
 * @returns {boolean}
 */

function emailRegexpCheck(email) {
  var emailRegexp = /^([^\s][\d]|[\w-]){3,25}@([^\s][\d]|[\w]){2,15}\.([^\s][\d]|[\w]){2,15}\.?([^\s][\d]|[\w]){0,10}$/i;

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

function isUserEmailExist(userEmail) {
  return Object(_workshopEntitiesFetches_es6__WEBPACK_IMPORTED_MODULE_0__["checkWorkshopEntityExist"])("User", "email", userEmail).then(function (promise) {
    promise.exist = promise.ok;
    return promise;
  });
}
/**
 * Async function!
 *
 * @param userEmail
 * @returns {Promise<unknown>} with '.exist' additional boolean property.
 */

function isNonEnabledUserEmailExist(userEmail) {
  return Object(_workshopEntitiesFetches_es6__WEBPACK_IMPORTED_MODULE_0__["checkNonEnabledUserExist"])(userEmail).then(function (promise) {
    promise.exist = promise.ok;
    return promise;
  });
}
function passwordCheck(password) {
  if (typeof password !== "string" && typeof password !== "number" || password.length < 5) {
    return false;
  } else {
    return true;
  }
}
function phoneNumberCheck(phoneNumber) {
  var phoneNumberRegexp = /^[+(]?\s?[\d()\-^\s]{10,20}$/;

  if (typeof phoneNumber !== "string") {
    return false;
  } else {
    var stringNumber = phoneNumber.toString();
    return stringNumber.match(phoneNumberRegexp);
  }
}
function phoneNameCheck(phoneName) {
  var phoneNameRegexp = /^[\w\sа-яЁёА-Я]{2,15}$/;

  if (typeof phoneName !== "string") {
    return false;
  } else {
    return phoneName.toString().match(/^$/) || phoneName.match(phoneNameRegexp);
  }
}

/***/ }),

/***/ "./src/js/workshopEntitiesFetches.es6":
/*!********************************************!*\
  !*** ./src/js/workshopEntitiesFetches.es6 ***!
  \********************************************/
/*! exports provided: checkWorkshopEntityExist, checkNonEnabledUserExist, passwordResetEmail */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "checkWorkshopEntityExist", function() { return checkWorkshopEntityExist; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "checkNonEnabledUserExist", function() { return checkNonEnabledUserExist; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "passwordResetEmail", function() { return passwordResetEmail; });
/**
 *
 * @param workshopEntityType String representations for WorkshopEntityType to be found
 * @param propertyName The name of any existing property of that WorkshopEntity
 * @param propertyValue A value of that property
 * @returns {Promise<Response>} with status.ok === true or status.ok === false
 */
function checkWorkshopEntityExist() {
  var workshopEntityType = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : "default";
  var propertyName = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : "default";
  var propertyValue = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : "default";
  var formData = new FormData();
  formData.append("workshopEntityType", workshopEntityType);
  formData.append("propertyName", propertyName);
  formData.append("propertyValue", propertyValue);
  return fetch("http://localhost:18080/workshop.pro/ajax/entity-exist", {
    method: "POST",
    body: formData // credentials: "same-origin"
    // headers: new Headers({
    // 	"Content-Type": "application/x-www-form-urlencoded"
    // })

  }).then(function (promise) {
    return promise;
  });
}
/**
 * Checks if the given email exist AND belongs to non-enabled User
 * @param nonEnabledUserEmail
 * @returns {Promise<Response>}
 */

function checkNonEnabledUserExist() {
  var nonEnabledUserEmail = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : "default";
  var formData = new FormData();
  formData.append("email", nonEnabledUserEmail);
  return fetch("http://localhost:18080/workshop.pro/ajax/registration/repeated-activation-link", {
    method: "POST",
    body: formData // credentials: "same-origin"

  }).then(function (promise) {
    return promise;
  });
}
function passwordResetEmail() {
  var email = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : "";
  var formData = new FormData();
  formData.append("email", email);
  return fetch(location.origin + "/workshop.pro/ajax/password-reset/email", {
    method: "POST",
    body: formData
  }).then(function (promise) {
    return promise;
  });
}

/***/ })

/******/ });