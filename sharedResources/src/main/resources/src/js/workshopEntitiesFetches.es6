/**
 *
 * @param workshopEntityType String representations for WorkshopEntityType to be found
 * @param propertyName The name of any existing property of that WorkshopEntity
 * @param propertyValue A value of that property
 * @returns {Promise<Response>} with status.ok === true or status.ok === false
 */
export function checkWorkshopEntityExist(workshopEntityType = "default", propertyName = "default", propertyValue = "default") {
	
	const formData = new FormData();
	formData.append("workshopEntityType", workshopEntityType);
	formData.append("propertyName", propertyName);
	formData.append("propertyValue", propertyValue);
	
	return  fetch("http://localhost:18080/workshop.pro/ajax/entity-exist", {
		method: "POST",
		body: formData
		// credentials: "same-origin"
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
export function checkNonEnabledUserExist(nonEnabledUserEmail = "default") {
	
	const formData = new FormData();
	formData.append("email", nonEnabledUserEmail);
	
	return fetch("http://localhost:18080/workshop.pro/ajax/registration/repeated-activation-link", {
		method: "POST",
		body: formData
		// credentials: "same-origin"
	}).then((promise) => {
		return promise;
	})
}