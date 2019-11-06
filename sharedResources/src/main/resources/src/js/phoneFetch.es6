export function deletePhone(phoneId) {
	
	return fetch(`http://localhost:18080/workshop.pro/ajax/phones/${phoneId}`,
		{
			method: "DELETE",
			credentials: "same-origin"
		})
		.then((resolve) => {
			return resolve;
		})
		.catch((reject) => {
			return reject;
		});
}

export function addPhone(phoneNum, phoneName) {
	let phoneForm = new FormData;
	phoneForm.append("name", phoneName);
	phoneForm.append("phone", phoneNum);
	
	return fetch("http://localhost:18080/workshop.pro/ajax/phones",
		{
			method: "POST",
			credentials: "same-origin",
			body: phoneForm
		});
};

