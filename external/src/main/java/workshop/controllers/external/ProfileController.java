package workshop.controllers.external;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import workshop.controllers.WorkshopControllerAbstract;
import workshop.internal.entities.User;
import workshop.internal.exceptions.EntityNotFoundException;
import workshop.internal.services.UsersService;

import java.util.Locale;

@Controller
@RequestMapping(path = "/profile")
public class ProfileController extends WorkshopControllerAbstract {
	
	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private UsersService usersService;
	
	@GetMapping
	public String getProfile(Model model, Authentication authentication) {
		return "profile";
	}
	
	@ModelAttribute(name = "user")
	public void setUser(Model model, Authentication authentication, Locale locale) {
		if (authentication == null || authentication.getPrincipal().toString().equals("Anonymous")) {
			return;
		} else {
			User userByLogin = usersService.findByLogin(authentication.getName())
				.orElseThrow(() -> new EntityNotFoundException(
					authentication.getName()+" not found in the DataBase!",
					HttpStatus.NOT_FOUND,
					messageSource.getMessage("message.notFound(1)", new Object[]{authentication.getName()}, locale)));
			model.addAttribute("user", userByLogin);
		}
	}
}

