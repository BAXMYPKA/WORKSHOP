export function deleteUserMessage() {
	const userMessageDiv = document.querySelector("#userMessage");
	userMessageDiv.innerHTML = "";
	userMessageDiv.style.display = "none";
}

export function setUserMessage(userMessage = "") {
	const userMessageDiv = document.querySelector("#userMessage");
	userMessageDiv.style.display = "block";
	userMessageDiv.innerHTML = userMessage;
}

export function addUserMessage(additionalUserMessage = "") {
	const userMessageDiv = document.querySelector("#userMessage");
	userMessageDiv.style.display = "block";
	userMessageDiv.innerHTML.concat("<br>").concat(additionalUserMessage);
}