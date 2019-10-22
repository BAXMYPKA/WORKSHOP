package workshop.controllers.external.ajax;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import workshop.internal.entities.User;
import workshop.internal.entities.WorkshopEntity;
import workshop.internal.services.UsersService;

import java.util.List;

@Controller
@RequestMapping(path = "/ajax")
public class IsExistAjaxController {
	
	@Autowired
	private UsersService usersService;
	
	/**
	 * @param email {@link User#getEmail()}
	 * @return "true" is an {@link User} with such an email exist or {@link org.springframework.http.HttpStatus#NOT_FOUND}
	 */
	@PostMapping(path = "/user-email-exist", consumes = MediaType.TEXT_HTML_VALUE)
	public Boolean isWorkshopEntityExist(@RequestAttribute(name = "email") String email) {
		List<User> users = usersService.findByProperty("email", email);
		return true;
	}
}
