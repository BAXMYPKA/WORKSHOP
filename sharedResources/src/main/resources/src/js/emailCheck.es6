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