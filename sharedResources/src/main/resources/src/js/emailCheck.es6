export function emailRegexpCheck(email) {
	
	let emailRegexp = /^([^\s][\d]|[\w]){3,25}@([^\s][\d]|[\w]){2,15}\.([^\s][\w]|[\d]){2,10}$/i;
	
	if (typeof email === "string" && email.match(emailRegexp)) {
		return true;
	} else {
		return false;
	}
};

export function userEmailExist(userEmail) {
	
	let prom = new Promise(function (resolve, reject) {
	
	});
	
	return true;
}