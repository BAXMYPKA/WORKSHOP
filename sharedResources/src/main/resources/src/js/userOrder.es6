const unrollHref = document.querySelector(".unrollHref");

unrollHref.addEventListener("click", (clickEvent) => {
	const orderTasksTable = document.querySelector(".orderTasksTable");
	const unrollHref = document.querySelector(".unrollHref");
	clickEvent.preventDefault();
	orderTasksTable.hidden = orderTasksTable.hidden ? false : true;
	if (orderTasksTable.hidden) {
		unrollHref.textContent = "РАЗВЕНУТЬ";
	} else {
		unrollHref.textContent = "СВЕРНУТЬ";
	}
});