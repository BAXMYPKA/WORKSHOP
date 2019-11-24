export function deleteUserPhoto(src) {
	return fetch(src,
		{
			method: "delete",
			credentials: "same-origin"
		});
}