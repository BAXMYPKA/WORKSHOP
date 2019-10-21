export function passwordCheck(password) {
	if (password.length < 5) {
		return false;
	} else {
		return true;
	}
}
