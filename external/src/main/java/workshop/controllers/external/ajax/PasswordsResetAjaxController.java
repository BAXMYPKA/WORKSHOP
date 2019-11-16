package workshop.controllers.external.ajax;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import workshop.internal.services.UuidsService;

import java.util.Locale;

@Controller
@RequestMapping(path = "/ajax/password-reset")
public class PasswordsResetAjaxController {
	
	@Autowired
	private UsersService usersService;
	
	@Autowired
	private UuidsService uuidsService;
	
	@Autowired
	private UserMessagesCreator userMessagesCreator;
	
	@Autowired
	private MessageSource messageSource;
	
	@Value("${url}")
	private String workshopUrl;
	
	@PostMapping(path = "/email", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<String> postEmailPasswordResetForUser(@RequestParam(name = "email") String email, Locale locale) {
		try {
			User user = usersService.findByLogin(email);
			Uuid resetUserPassUuid = new Uuid();
			resetUserPassUuid.setPasswordResetUser(user);
			
			uuidsService.persistEntity(resetUserPassUuid);
			
			user.setPasswordResetUuid(resetUserPassUuid);
			
			usersService.mergeEntity(user);
			
			String resetPasswordLink = "<a href=\"" + workshopUrl + "/password-reset?uuid=" + resetUserPassUuid.getUuid() +
				"\">Сбросить пароль</a>";
			String userMessage = messageSource.getMessage(
				"message.passwordResetByEmailDemo(3)",
				new Object[]{user.getEmail(), resetUserPassUuid.getUuid(), resetPasswordLink}, locale);
			String jsonMessageForUser = userMessagesCreator.getJsonMessageForUser(userMessage);
			
			return ResponseEntity.ok(jsonMessageForUser);
			
		} catch (EntityNotFoundException e) {
			String userMessageNotFound = messageSource.getMessage(
				"message.notFound(1)", new Object[]{email}, locale);
			String jsonUserMessageNotFound = userMessagesCreator.getJsonMessageForUser(userMessageNotFound);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jsonUserMessageNotFound);
		}
	}
}
