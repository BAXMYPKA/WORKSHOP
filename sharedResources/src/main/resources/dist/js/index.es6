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

/***/ "./src/js/emailCheck.es6":
/*!*******************************!*\
  !*** ./src/js/emailRegexpCheck.es6 ***!
  \*******************************/
/*! exports provided: emailRegexpCheck, userEmailExist */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "emailCheck", function() { return emailCheck; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "userEmailExist", function() { return userEmailExist; });
function emailCheck(email) {
	
	let regexp = /^user@email.pro$/i;
	
	if (typeof email === "string" && email.match(regexp)) {
		return true;
	} else {
		return false;
	}
};

function userEmailExist(userEmail) {
	return true;
}

/***/ }),

/***/ "./src/js/index.es6":
/*!**************************!*\
  !*** ./src/js/index.es6 ***!
  \**************************/
/*! no exports provided */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var _passwordCheck_es6__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./passwordCheck.es6 */ "./src/js/passwordCheck.es6");
/* harmony import */ var _emailCheck_es6__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ./emailRegexpCheck.es6 */ "./src/js/emailCheck.es6");



const PASSWORD_ERROR_MESSAGE = "Требуется минимум 5 знаков!";
const USER_EMAIL_ERROR_MESSAGE = "Имя должно соответствовать формату электронного адреса!";
const USER_NOT_FOUND = "Такого пользователя в базе не обнаружено!";
const usernameInput = document.querySelector("#inputUsername");
const passwordInput = document.querySelector("#inputPassword");
const passwordErrorMessageSpan = document.querySelector("#passwordErrorMessage");
const userErrorMessageSpan = document.querySelector("#userErrorMessage");

passwordInput.addEventListener("input", (env) => {
	if (Object(_passwordCheck_es6__WEBPACK_IMPORTED_MODULE_0__["passwordCheck"])(passwordInput.value) === true) {
		passwordInput.style.color = "green";
		passwordInput.removeAttribute("title");
		passwordErrorMessageSpan.style.display = "none";
	} else {
		passwordInput.style.color = "red";
		passwordInput.setAttribute("title", PASSWORD_ERROR_MESSAGE);
		passwordErrorMessageSpan.style.display = "block";
		passwordErrorMessageSpan.innerHTML = PASSWORD_ERROR_MESSAGE;
	}
});

usernameInput.addEventListener("input", (evt) => {
	if (!Object(_emailCheck_es6__WEBPACK_IMPORTED_MODULE_1__["emailCheck"])(usernameInput.value)) {
		usernameInput.setAttribute("title", USER_EMAIL_ERROR_MESSAGE);
		usernameInput.style.color = "red";
	} else if (Object(_emailCheck_es6__WEBPACK_IMPORTED_MODULE_1__["emailCheck"])(usernameInput.value) && !Object(_emailCheck_es6__WEBPACK_IMPORTED_MODULE_1__["userEmailExist"])(usernameInput.value)) {
		usernameInput.removeAttribute("title");
		usernameInput.style.color = "green";
		passwordErrorMessageSpan.style.display = "block";
		passwordErrorMessageSpan.innerHTML = USER_NOT_FOUND;
	} else {
		usernameInput.removeAttribute("title");
		usernameInput.style.color = "green";
		passwordErrorMessageSpan.style.display = "none";
	}
});

/***/ }),

/***/ "./src/js/passwordCheck.es6":
/*!**********************************!*\
  !*** ./src/js/passwordCheck.es6 ***!
  \**********************************/
/*! exports provided: passwordCheck */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "passwordCheck", function() { return passwordCheck; });
function passwordCheck(password) {
	if ( (typeof password === "string" || typeof password === "number") && password.length < 5) {
		return false;
	} else {
		return true;
	}
}


/***/ })

/******/ });