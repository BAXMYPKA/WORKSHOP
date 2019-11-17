package workshop.controllers.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
		try {
			Uuid resetPassUuid = uuidsService.findByProperty("uuid", uuid).get(0);
			if (resetPassUuid.getPasswordResetUser() != null) {
				model.addAttribute("loggedUsername", resetPassUuid.getPasswordResetUser().getEmail());
				model.addAttribute("passwordResetUuid", resetPassUuid.getUuid());
				return "passwordReset";
			} else {
				throw new EntityNotFoundException("Uuid.getPasswordResetUser() not found!");
			}
		} catch (EntityNotFoundException e) {
			return returnError(e, model, locale);
		}
	}
	
	/**
	 * Only for {@link User}s obtained their links from emails with secure {@link Uuid} for password resetting.
	 * @param uuid {@link Uuid} with {@link Uuid#getPasswordResetUser()}
	 * @param password A new one
	 */
	@PostMapping(params = "uuid")
	public String postPasswordResetWithUuid(
		@RequestParam(name = "uuid") String uuid,
		@RequestParam(name = "password") String password,
		Model model,
		RedirectAttributes redirectAttributes,
		Locale locale) {
		try {
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
			
		} catch (EntityNotFoundException e) {
			return returnError(e, model, locale);
		} catch (IllegalArgumentsException e) {
			String userMessagePasswordMismatch = getMessageSource().getMessage(
				"message.passwordIncorrect", null, locale);
			getUserMessagesCreator().setUserMessage(model, userMessagePasswordMismatch);
			return "passwordReset";
		}
	}
	
	@PostMapping
	public String postPasswordReset(
		@RequestParam(name = "password") String password,
		Authentication authentication,
		Model model,
		RedirectAttributes redirectAttributes,
		Locale locale) {
		try {
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
			
		} catch (EntityNotFoundException e) {
			return returnError(e, model, locale);
		} catch (IllegalArgumentsException e) {
			String userMessagePasswordMismatch = getMessageSource().getMessage(
				"message.passwordIncorrect", null, locale);
			getUserMessagesCreator().setUserMessage(model, userMessagePasswordMismatch);
			return "passwordReset";
		}
	}
	
	private String returnError(EntityNotFoundException e, Model model, Locale locale) {
		log.debug(e.getMessage(), e);
		String userMessageUuidNotValid = getMessageSource().getMessage(
			"message.uuidPassResetNotValid", null, locale);
		getUserMessagesCreator().setUserMessage(model, userMessageUuidNotValid);
		return "passwordReset";
		
	}
	
	private boolean isPasswordValid(String password) {
		return password.matches("^[\\p{LD}\\-._+=()*&%$#@!<>\\[{\\]}'\"^;:?/~`]{5,254}$");
	}
}
