package workshop.controllers.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import workshop.controllers.WorkshopControllerAbstract;
import workshop.exceptions.EntityNotFoundException;
import workshop.exceptions.IllegalArgumentsException;
import workshop.exceptions.WorkshopException;
import workshop.internal.entities.Uuid;
import workshop.internal.services.UuidsService;

import java.util.Locale;

@Slf4j
@Controller
@RequestMapping(path = "/password-reset")
public class PasswordResetController extends WorkshopControllerAbstract {
	
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
	
	@PostMapping
	public String postPasswordReset(
		@RequestParam(name = "passwordResetUuid") String passwordResetUuid,
		@RequestParam(name = "loggedUsername") String loggedUsername,
		@RequestParam(name = "password") String password,
		Model model,
		Locale locale) {
		try {
			Uuid uuid = uuidsService.findByProperty("uuid", passwordResetUuid).get(0);
			if (!uuid.getPasswordResetUser().getEmail().equals(loggedUsername)) {
				throw new EntityNotFoundException("Uuid passwordResetUser property mismatches loggedUsername!");
			}
			if (!isPasswordValid(password)) {
				throw new IllegalArgumentsException("Password incorrect!");
			}
			//TODO: to set the new password and delete UUID
			return "passwordReset";
		} catch (EntityNotFoundException e) {
			return returnError(e, model, locale);
		} catch (IllegalArgumentsException e) {
			String userMessagePasswordMismatch = getMessageSource().getMessage(
				"message.passwordIncorrect", null, locale);
			getUserMessagesCreator().setMessageForUser(model, userMessagePasswordMismatch);
			return "passwordReset";
		}
	}
	
	private String returnError(EntityNotFoundException e, Model model, Locale locale) {
		log.debug(e.getMessage(), e);
		String userMessageUuidNotValid = getMessageSource().getMessage(
			"message.uuidPassResetNotValid", null, locale);
		getUserMessagesCreator().setMessageForUser(model, userMessageUuidNotValid);
		return "passwordReset";
		
	}
	
	private boolean isPasswordValid(String password) {
		return password.matches("^[\\p{LD}\\-._+=()*&%$#@!<>\\[{\\]}'\"^;:?/~`]{5,254}$");
	}
}
