import workshopEntityExist from "./workshopEntityExist.es6";

export function emailRegexpCheck(email) {
	
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
export function userEmailExist(userEmail) {
	
	return workshopEntityExist("User", "email", userEmail)
		.then((promise) => {
			promise.exist = promise.ok;
			return promise;
		});
}

export function passwordCheck(password) {
	if ((typeof password === "string" || typeof password === "number") && password.length < 5) {
		return false;
	} else {
		return true;
	}
}

export function phoneNumberCheck(phoneNumber) {
	
	let phoneNumberRegexp = /^[+(]?\s?[\d()\-^\s]{10,20}$/;
	
	if (typeof phoneNumber !== "string") {
		return false;
	} else {
		let stringNumber = phoneNumber.toString();
		return stringNumber.match(phoneNumberRegexp);
	}
}

export function phoneNameCheck(phoneName) {
	
	let phoneNameRegexp = /^[\w\sа-яЁёА-Я]{2,15}$/;
	
	if (typeof phoneName !== "string") {
		return false;
	} else {
		return phoneName.toString().match(/^$/) || phoneName.match(phoneNameRegexp);
	}
}