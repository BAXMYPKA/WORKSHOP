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