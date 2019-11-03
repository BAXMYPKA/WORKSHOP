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
		})
}

function addPhone(phoneNum, phoneName) {

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


const PHONE_DELETION_ERROR_MESSAGE = "Не удалось удалить телефон!";
const deleteButtons = document.querySelectorAll(".deleteButton");
let phoneIdToOperateOn;

deleteButtons.forEach(function (button, key, parent) {
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


/***/ })

/******/ });