import AjaxSearchObject from "./AjaxSearchObject.es6";

export function emailRegexpCheck(email) {
	
	let emailRegexp = /^([^\s][\d]|[\w]){3,25}@([^\s][\d]|[\w]){2,15}\.([^\s][\w]|[\d]){2,10}$/i;
	
	if (typeof email === "string" && email.match(emailRegexp)) {
		return true;
	} else {
		return false;
	}
};

export function userEmailExist(userEmail) {
	
	let emailRegexp = /^([^\s][\d]|[\w]){3,25}@([^\s][\d]|[\w]){2,15}\.([^\s][\w]|[\d]){2,10}$/i;
	
	if (typeof userEmail === "string" && userEmail.match(emailRegexp)) {
		return false;
	}
	
	let searchObject = new AjaxSearchObject();
	
	searchObject.entityExistEntityType = "User";
	searchObject.entityExistEntityPropertyName = "email";
	searchObject.entityExistEntityPropertyValue = userEmail;
	
	let promise = fetch(searchObject.urlEntityExist, {
		method: searchObject.methodEntityExist,
		body: searchObject.bodyFormDataEntityExist,
		headers: searchObject.headersEntityExist
	}).then(function (response) {
			console.log(response.status);
			console.log(response.body)
		});
	
	return true;
}