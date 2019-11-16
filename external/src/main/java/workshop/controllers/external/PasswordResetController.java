package workshop.controllers.external;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import workshop.controllers.WorkshopControllerAbstract;
import workshop.exceptions.EntityNotFoundException;
import workshop.internal.entities.Uuid;
import workshop.internal.services.UuidsService;

import java.util.Locale;

@Controller
@RequestMapping(path = "/password-reset")
public class PasswordResetController extends WorkshopControllerAbstract {
	
	@Autowired
	private UuidsService uuidsService;
	
	
	@GetMapping
	public String getPasswordReset() {
		return "passwordReset";
	}
	
	@GetMapping(params = "uuid")
	public String getPasswordResetUuid(@RequestParam(name = "uuid") String uuid, Model model, Locale locale) {
		try {
			Uuid resetPassUuid = uuidsService.findByProperty("uuid", uuid).get(0);
			
			return "passwordReset";
		} catch (EntityNotFoundException e) {
			String userMessageUuidNotValid = getMessageSource().getMessage(
				"message.uuidPassResetNotValid", null, locale);
			getUserMessagesCreator().setMessageForUser(model, userMessageUuidNotValid);
			return "passwordReset";
		}
	}
	
}
