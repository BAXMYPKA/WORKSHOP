export function passwordCheck(password) {
	if ( (typeof password === "string" || typeof password === "number") && password.length < 5) {
		return false;
	} else {
		return true;
	}
}
