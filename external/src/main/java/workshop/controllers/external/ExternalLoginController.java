package workshop.controllers.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import workshop.controllers.WorkshopControllerAbstract;
import workshop.internal.entities.User;
import workshop.internal.entities.Uuid;
import workshop.exceptions.EntityNotFoundException;
import workshop.internal.services.UuidsService;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

@Slf4j
@Controller
@RequestMapping(path = "/login")
public class ExternalLoginController extends WorkshopControllerAbstract {
	
	@Autowired
	private UuidsService uuidsService;
	
	/**
	 * !!! If {@link Authentication} != null and 'Referer' contains '/login', all these mean User has come here after
	 * successful logging in on '/login' page as a 'Referer' and needs to be redirected to '/profile' page !!!
	 *
	 * @param request
	 * @param authentication
	 * @return
	 */
	@GetMapping
	public String getLogin(HttpServletRequest request, Authentication authentication) {
		if (request.getHeader("Referer") != null && request.getHeader("Referer").contains("/login") &&
			authentication != null && authentication.isAuthenticated() && !authentication.getName().contains("Anonymous")) {
			return "redirect:/profile";
		}
		return "login";
	}
	
	/**
	 * For newly-created {@link User}s with the confirmation registration link who have to enter the site exactly here for the first
	 * time to confirm their registration.
	 * This method adds the 'uuid' hidden parameter to 'login.html' page. The LoginAuthenticationFilter intercepts the
	 * 'uuid' parameter and issues the UsernamePasswordUuidAuthenticationToken which is validated by
	 * UsersAuthenticationProvider.
	 *
	 * @param uuid  The {@link Uuid} object to be evaluated.
	 * @param model If the given {@link Uuid} is valid it will be added into this {@link Model}
	 *              to be the hidden 'uuid' parameter that will be passed along with 'username' and 'password'
	 *              and further will be evaluated by the LoginAuthenticationFilter.
	 */
	@GetMapping(params = "uuid")
	public String getLoginRegistrationConfirmation(
		@RequestParam(name = "uuid") String uuid,
		Model model,
		Locale locale,
		RedirectAttributes redirectAttributes,
		Authentication authentication) {
		if (authentication != null && authentication.isAuthenticated()){
			//Here we have confirmed and authenticated User
			return "redirect:/profile";
		}
		if (uuid.isEmpty()) {
			String userMessageNullUuid = getMessageSource().getMessage("message.uuidNull", null, locale);
			getUserMessagesCreator().setMessageForUser(redirectAttributes, userMessageNullUuid);
			return "redirect:/login";
		}
		try { //Check if the UUID is presented in the DataBase
			uuidsService.findByProperty("uuid", uuid).get(0);
			model.addAttribute("uuid", uuid);
			String userMessageConfirmationFirstTime = getMessageSource().getMessage(
				"message.confirmRegistrationFirstTime", null, locale);
			getUserMessagesCreator().setMessageForUser(model, userMessageConfirmationFirstTime);
			return "login";
		} catch (EntityNotFoundException e) { //UUID is not valid or outdated
			log.debug("UUID={} not found in the DataBase!", uuid);
			String userMessageUuidNotValid = getMessageSource().getMessage("message.uuidRegConfirmNotValid", null, locale);
			getUserMessagesCreator().setMessageForUser(redirectAttributes, userMessageUuidNotValid);
			return "redirect:/login";
		}
	}
}
