/**
 *
 * @param workshopEntityType String representations for WorkshopEntityType to be found
 * @param propertyName The name of any existing property of that WorkshopEntity
 * @param propertyValue A value of that property
 * @returns {Promise<Response>} with status.ok === true or status.ok === false
 */
export default function workshopEntityExist(workshopEntityType = "default", propertyName = "default", propertyValue = "default") {
	
	const formData = new FormData();
	formData.append("workshopEntityType", workshopEntityType);
	formData.append("propertyName", propertyName);
	formData.append("propertyValue", propertyValue);
	
	return  fetch("http://localhost:18080/workshop.pro/ajax/entity-exist", {
		method: "POST",
		body: formData,
		// headers: new Headers({
		// 	"Content-Type": "application/x-www-form-urlencoded"
		// })
	}).then(function (promise) {
		return promise;
	});
}