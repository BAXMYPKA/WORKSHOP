package workshop.controllers.external;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import workshop.controllers.WorkshopControllerAbstract;
import workshop.internal.entities.User;
import workshop.internal.entities.hibernateValidation.PersistenceValidation;
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
	
	/**
	 * By every request adds the "user" attribute as the {@link User} obtained from the given {@link Authentication}
	 * if that User is present and not 'Anonymous'.
	 *
	 * @param authentication A {@link User#getEmail()} to be obtained from (if present).
	 * @return "profile.html"
	 * @throws EntityNotFoundException If a {@link User} cannot be obtained from the given {@link Authentication}
	 */
	@GetMapping
	public String getProfile(Model model, Authentication authentication, Locale locale) throws EntityNotFoundException {
		if (authentication == null || authentication.getPrincipal().toString().equals("Anonymous")) {
			return "profile";
		} else {
			User userByLogin = usersService.findByLogin(authentication.getName())
				.orElseThrow(() -> new EntityNotFoundException(
					authentication.getName() + " not found in the DataBase!",
					HttpStatus.NOT_FOUND,
					messageSource.getMessage("message.notFound(1)", new Object[]{authentication.getName()}, locale)));
			model.addAttribute("user", userByLogin);
		}
		return "profile";
	}
	
	@PostMapping
	public String postProfile(@Validated(PersistenceValidation.class) @ModelAttribute(name = "user") User user,
							  BindingResult bindingResult, Model model, Locale locale) {
		if (bindingResult.hasErrors()) {
			System.out.println(bindingResult.getAllErrors());
			model.addAttribute("errors", bindingResult);
			return "profile";
		}
		return "profile";
	}
	
/*
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
*/
}

