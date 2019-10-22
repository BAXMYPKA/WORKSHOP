export function emailCheck(email) {
	
	let regexp = /^user@email.pro$/i;
	
	if (typeof email === "string" && email.match(regexp)) {
		return true;
	} else {
		return false;
	}
};

export function userEmailExist(userEmail) {
	return true;
}