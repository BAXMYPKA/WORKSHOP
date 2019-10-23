export default class AjaxSearchObject {
	constructor(
		urlEntityExist = "http://localhost:18080/workshop.pro/ajax/entity-exist",
		methodEntityExist = "POST",
		bodyFormDataEntityExist = function () {
			return new FormData();
		},
		headersEntityExist = {
			"Content-Type": "application/x-www-form-urlencoded"
		}) {
		
		this.urlEntityExist = urlEntityExist;
		this.methodEntityExist = methodEntityExist;
		this.bodyFormDataEntityExist = bodyFormDataEntityExist();
		this.headersEntityExist = headersEntityExist;
	}
	
	set entityExistEntityType(workshopEntityType) {
		this.bodyFormDataEntityExist.append("workshopEntityType", workshopEntityType);
	}
	
	set entityExistEntityPropertyName(propertyName) {
		this.bodyFormDataEntityExist.append("propertyName", propertyName);
	}
	
	set entityExistEntityPropertyValue(propertyValue) {
		this.bodyFormDataEntityExist.append("propertyValue", propertyValue);
	}
}