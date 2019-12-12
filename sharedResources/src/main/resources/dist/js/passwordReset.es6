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
/******/ 	return __webpack_require__(__webpack_require__.s = "./src/js/passwordReset.es6");
/******/ })
/************************************************************************/
/******/ ({

/***/ "./src/js/passwordReset.es6":
/*!**********************************!*\
  !*** ./src/js/passwordReset.es6 ***!
  \**********************************/
/*! no exports provided */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var _verifications_es6__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./verifications.es6 */ "./src/js/verifications.es6");
/* harmony import */ var _workshopEntitiesFetches_es6__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ./workshopEntitiesFetches.es6 */ "./src/js/workshopEntitiesFetches.es6");
/* harmony import */ var _userMessaging_es6__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ./userMessaging.es6 */ "./src/js/userMessaging.es6");



/**
 * If an authenticated or Users with UUID requested the page
 */

if (document.querySelector("#loggedUsersResetForm") !== null) {
  var passwordInput = document.querySelector("#inputPassword");
  var passwordConfirmInput = document.querySelector("#inputConfirmPassword");
  var userErrorPassword = document.querySelector("#userErrorPasswordMessage");
  var userErrorConfirmPassword = document.querySelector("#userErrorConfirmPasswordMessage");
  var errorMessageNull = "Все поля должны быть заполнены!";
  var errorMessagePasswordsCoincidence = "Пароли должны совпадать!";
  var errorMessagePasswordRules = "Пароль должен содержать минимум 5 знаков без пробелов!";
  passwordInput.addEventListener("input", function (evt) {
    userErrorPassword.style.display = "none";

    if (!Object(_verifications_es6__WEBPACK_IMPORTED_MODULE_0__["passwordCheck"])(evt.currentTarget.value)) {
      passwordInput.style.color = "red";
    } else {
      passwordInput.style.color = "green";
    }
  });
  passwordConfirmInput.addEventListener("input", function (evt) {
    userErrorConfirmPassword.style.display = "none";

    if (!Object(_verifications_es6__WEBPACK_IMPORTED_MODULE_0__["passwordCheck"])(passwordConfirmInput.value)) {
      passwordConfirmInput.style.color = "red";
    } else {
      passwordConfirmInput.style.color = "green";
    }
  });
  document.querySelector(".buttonLogin").addEventListener("click", function (evt) {
    evt.preventDefault();

    if (passwordInput.value === null || passwordConfirmInput.value === null || passwordInput.value === "" || passwordConfirmInput.value === "") {
      userErrorPassword.style.display = "block";
      userErrorPassword.textContent = errorMessageNull;
      return;
    } else if (passwordInput.value !== passwordConfirmInput.value) {
      userErrorConfirmPassword.style.display = "block";
      userErrorConfirmPassword.textContent = errorMessagePasswordsCoincidence;
      return;
    } else if (passwordInput.style.color !== "green" && passwordConfirmInput.style.color !== "green") {
      userErrorPassword.style.display = "block";
      userErrorPassword.textContent = errorMessagePasswordRules;
      return;
    }

    document.querySelector("#loggedUsersResetForm").submit();
  });
}
/**
 * If a not logged User requested the page for get an email with password reset UUID
 */


if (document.querySelector("#notLoggedUsersForm") !== null) {
  var emailInput = document.querySelector("#userEmail");
  var buttonLogin = document.querySelector(".buttonLogin");
  var userErrorMessageEmail = document.querySelector("#userErrorMessageEmail");
  var emailNotExistMessage = "Такого адреса в базе нет!";
  document.querySelector("#userEmail").addEventListener("input", function (evn) {
    if (!Object(_verifications_es6__WEBPACK_IMPORTED_MODULE_0__["emailRegexpCheck"])(emailInput.value)) {
      emailInput.style.color = "red";
      buttonLogin.disabled = true;
    } else {
      emailInput.style.color = "green";
      buttonLogin.disabled = false;
    }
  });
  document.querySelector(".buttonLogin").addEventListener("click", function (env) {
    env.preventDefault();
    Object(_workshopEntitiesFetches_es6__WEBPACK_IMPORTED_MODULE_1__["checkWorkshopEntityExist"])('User', 'email', emailInput.value).then(function (promise) {
      if (promise.ok) {
        Object(_workshopEntitiesFetches_es6__WEBPACK_IMPORTED_MODULE_1__["passwordResetEmail"])(emailInput.value).then(function (promise) {
          promise.json().then(function (json) {
            emailInput.style.color = "green";
            emailInput.value = "";
            userErrorMessageEmail.textContent = "";
            userErrorMessageEmail.style.display = "none";
            var userMessage = JSON.stringify(json);
            Object(_userMessaging_es6__WEBPACK_IMPORTED_MODULE_2__["setUserMessage"])(JSON.parse(userMessage)['userMessage']);
          });
        });
      } else {
        emailInput.style.color = "red";
        userErrorMessageEmail.style.display = "block";
        userErrorMessageEmail.textContent = emailNotExistMessage;
      }
    });
  });
}

/***/ }),

/***/ "./src/js/userMessaging.es6":
/*!**********************************!*\
  !*** ./src/js/userMessaging.es6 ***!
  \**********************************/
/*! exports provided: deleteUserMessage, setUserMessage, addUserMessage */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "deleteUserMessage", function() { return deleteUserMessage; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "setUserMessage", function() { return setUserMessage; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "addUserMessage", function() { return addUserMessage; });
function deleteUserMessage() {
  var userMessageDiv = document.querySelector("#userMessage");
  userMessageDiv.innerHTML = "";
  userMessageDiv.style.display = "none";
}
function setUserMessage() {
  var userMessage = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : "";
  var userMessageDiv = document.querySelector("#userMessage");
  userMessageDiv.style.display = "block";
  userMessageDiv.innerHTML = userMessage;
}
function addUserMessage() {
  var additionalUserMessage = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : "";
  var userMessageDiv = document.querySelector("#userMessage");
  userMessageDiv.style.display = "block";
  userMessageDiv.innerHTML.concat("<br>").concat(additionalUserMessage);
}

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