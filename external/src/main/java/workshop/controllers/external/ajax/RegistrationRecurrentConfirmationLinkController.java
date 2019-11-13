package workshop.controllers.external.ajax;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import workshop.controllers.utils.UserMessagesCreator;
import workshop.exceptions.EntityNotFoundException;
import workshop.internal.entities.User;
import workshop.internal.entities.Uuid;
import workshop.internal.services.UsersService;

import java.util.Locale;

@Controller
@RequestMapping(path = "/ajax/registration")
public class RegistrationRecurrentConfirmationLinkController {
	
	@Autowired
	private UserMessagesCreator userMessagesCreator;
	
	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private UsersService usersService;
	
	@Value("${url}")
	private String workshopUrl;
	
	@PostMapping(path = "/recurrent-confirmation-link")
	public ResponseEntity<String> postConfirmationLinkCredentials(@RequestParam(name = "email") String email,
		Locale locale) {
		try {
			User user = usersService.findByLogin(email != null ? email : "");
			if (user.getIsEnabled()) { //We have to create links only for non-enabled Users with Uuid
				String userMessageUserExists =
					messageSource.getMessage("message.emailEnabledExist(1)", new Object[]{email}, locale);
				return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).
					body(userMessagesCreator.getJsonMessageForUser(userMessageUserExists));
			} else { //Non-enabled Users MUST contain Uuid. Otherwise it is the fatal error
				Uuid uuid = user.getUuid();
				String registrationConfirmationUrl = workshopUrl + "login?uuid=" + uuid.getUuid();
				String confirmRegistrationLink = "<a href=\"" + registrationConfirmationUrl + "\">Подтвердить регистрацию</a>.";
				String userMessageRegistrationConfirmation = messageSource.getMessage(
					"message.confirmRegistrationRequiredDemo(3)",
					new Object[]{uuid.getUser().getEmail(), uuid.getUuid(), confirmRegistrationLink},
					locale);
				return ResponseEntity.ok(userMessagesCreator.getJsonMessageForUser(userMessageRegistrationConfirmation));
			}
		} catch (EntityNotFoundException e) {
			String userMessage = messageSource.getMessage("message.notFound(1)", new Object[]{email}, locale);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(userMessagesCreator.getJsonMessageForUser(userMessage));
		}
	}
}
