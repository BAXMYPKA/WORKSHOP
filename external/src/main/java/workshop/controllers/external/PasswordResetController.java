package workshop.controllers.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import workshop.controllers.WorkshopControllerAbstract;
import workshop.exceptions.EntityNotFoundException;
import workshop.exceptions.IllegalArgumentsException;
import workshop.internal.entities.User;
import workshop.internal.entities.Uuid;
import workshop.internal.services.UsersService;
import workshop.internal.services.UuidsService;

import java.util.Locale;

@Slf4j
@Controller
@RequestMapping(path = "/password-reset")
public class PasswordResetController extends WorkshopControllerAbstract {
	
	@Autowired
	private UsersService usersService;
	
	@Autowired
	private UuidsService uuidsService;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@GetMapping
	public String getPasswordReset() {
		return "passwordReset";
	}
	
	@GetMapping(params = "uuid")
	public String getPasswordResetUuid(@RequestParam(name = "uuid") String uuid, Model model, Locale locale) {
		Uuid resetPassUuid = uuidsService.findByProperty("uuid", uuid).get(0);
		if (resetPassUuid.getPasswordResetUser() != null) {
			model.addAttribute("loggedUsername", resetPassUuid.getPasswordResetUser().getEmail());
			model.addAttribute("passwordResetUuid", resetPassUuid.getUuid());
			return "passwordReset";
		} else {
			throw new EntityNotFoundException("Uuid.getPasswordResetUser() not found!");
		}
	}
	
	/**
	 * Only for {@link User}s obtained their links from emails with secure {@link Uuid} for password resetting.
	 *
	 * @param uuid     {@link Uuid} with {@link Uuid#getPasswordResetUser()}
	 * @param password A new one
	 */
	@PostMapping(params = "uuid")
	public String postPasswordResetWithUuid(
		@RequestParam(name = "uuid") String uuid,
		@RequestParam(name = "password") String password,
		RedirectAttributes redirectAttributes,
		Locale locale) {
		Uuid uuidEntity = uuidsService.findByProperty("uuid", uuid).get(0);
		if (uuidEntity.getPasswordResetUser() == null) {
			throw new EntityNotFoundException("Uuid.getPasswordResetUser() not found!");
		}
		if (!isPasswordValid(password)) {
			throw new IllegalArgumentsException("Password incorrect!");
		}
		User user = uuidEntity.getPasswordResetUser();
		String newEncodedPass = passwordEncoder.encode(password);
		user.setPassword(newEncodedPass);
		
		usersService.mergeEntity(user);
		uuidsService.removeEntity(uuidEntity);
		
		String userMessageSuccess = getMessageSource().getMessage(
			"message.passwordChangedSuccessfully", null, locale);
		getUserMessagesCreator().setUserMessage(redirectAttributes, userMessageSuccess);
		return "redirect:/login";
	}
	
	@PostMapping
	public String postPasswordReset(
		@RequestParam(name = "password") String password,
		Authentication authentication,
		RedirectAttributes redirectAttributes,
		Locale locale) {
		User user = usersService.findByLogin(authentication.getName());
		if (!isPasswordValid(password)) {
			throw new IllegalArgumentsException("Password incorrect!");
		}
		String newEncodedPass = passwordEncoder.encode(password);
		user.setPassword(newEncodedPass);
		
		usersService.mergeEntity(user);
		
		String userMessageSuccess = getMessageSource().getMessage(
			"message.passwordChangedSuccessfully", null, locale);
		getUserMessagesCreator().setUserMessage(redirectAttributes, userMessageSuccess);
		return "redirect:/profile";
	}
	
	private boolean isPasswordValid(String password) {
		return password.matches("^[\\p{LD}\\-._+=()*&%$#@!<>\\[{\\]}'\"^;:?/~`]{5,254}$");
	}
	
	@ExceptionHandler({IllegalArgumentsException.class})
	public ModelAndView passwordIncorrectException(IllegalArgumentsException e) {
		log.trace(e.getMessage(), e);
		Locale locale = LocaleContextHolder.getLocale();
		ModelAndView modelAndView = new ModelAndView("passwordReset");
		String userMessagePasswordMismatch = getMessageSource().getMessage(
			"message.passwordIncorrect", null, locale);
		getUserMessagesCreator().setUserMessage(modelAndView, userMessagePasswordMismatch);
		log.debug("UserMessage for the incorrect password are being set to the ModelAndView with locale={}", locale.getCountry());
		return modelAndView;
	}
	
	@ExceptionHandler({EntityNotFoundException.class})
	public ModelAndView entityNotFoundException(EntityNotFoundException e) {
		log.trace(e.getMessage(), e);
		Locale locale = LocaleContextHolder.getLocale();
		ModelAndView modelAndView = new ModelAndView("passwordReset");
		String userMessageUuidNotValid = getMessageSource().getMessage(
			"message.uuidPassResetNotValid", null, locale);
		getUserMessagesCreator().setUserMessage(modelAndView, userMessageUuidNotValid);
		log.debug("UserMessage for not found User or Uuid are being set to the ModelAndView with locale={}", locale.getCountry());
		return modelAndView;
	}
}
