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
/******/ 	return __webpack_require__(__webpack_require__.s = "./src/js/index.es6");
/******/ })
/************************************************************************/
/******/ ({

/***/ "./src/js/index.es6":
/*!**************************!*\
  !*** ./src/js/index.es6 ***!
  \**************************/
/*! no exports provided */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var _verifications_es6__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./verifications.es6 */ "./src/js/verifications.es6");

var inputUsername = document.querySelector("#inputUsername");
var inputPassword = document.querySelector("#inputPassword");
inputUsername.addEventListener("input", function (evt) {
  var USER_EMAIL_INCORRECT_ERROR_MESSAGE = "Имя должно соответствовать\nформату электронного адреса!";
  var USER_NOT_FOUND_ERROR_MESSAGE = "Пользователь не найден!";
  var userErrorMessageSpan = document.querySelector("#userErrorMessage");

  if (!Object(_verifications_es6__WEBPACK_IMPORTED_MODULE_0__["emailRegexpCheck"])(inputUsername.value)) {
    inputUsername.setAttribute("title", USER_EMAIL_INCORRECT_ERROR_MESSAGE);
    inputUsername.style.color = "red";
    userErrorMessageSpan.style.display = "none";
    return;
  } else {
    inputUsername.removeAttribute("title");
    inputUsername.style.color = "green";
    userErrorMessageSpan.style.display = "none";
  }

  Object(_verifications_es6__WEBPACK_IMPORTED_MODULE_0__["isUserEmailExist"])(inputUsername.value).then(function (exist) {
    if (exist.exist) {
      userErrorMessageSpan.style.display = "none";
    } else {
      userErrorMessageSpan.style.display = "block";
      userErrorMessageSpan.innerHTML = USER_NOT_FOUND_ERROR_MESSAGE;
    }
  });
});
inputPassword.addEventListener("input", function (evt) {
  var PASSWORD_INCORRECT_ERROR_MESSAGE = "Требуется минимум 5 знаков!";
  var passwordErrorMessageSpan = document.querySelector("#passwordErrorMessage");

  if (Object(_verifications_es6__WEBPACK_IMPORTED_MODULE_0__["passwordCheck"])(inputPassword.value) === true) {
    inputPassword.style.color = "green";
    inputPassword.removeAttribute("title");
    passwordErrorMessageSpan.style.display = "none";
  } else {
    inputPassword.style.color = "red";
    inputPassword.setAttribute("title", PASSWORD_INCORRECT_ERROR_MESSAGE);
    passwordErrorMessageSpan.style.display = "block";
    passwordErrorMessageSpan.innerHTML = PASSWORD_INCORRECT_ERROR_MESSAGE;
  }
});
document.querySelector(".buttonResetPassword").addEventListener("click", function (evt) {
  evt.preventDefault();
  location.href = location.origin + "/workshop.pro/password-reset";
});
document.querySelector("#buttonRegistration").addEventListener("click", function (evt) {
  evt.preventDefault();
  location.href = location.origin + "/workshop.pro/registration";
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