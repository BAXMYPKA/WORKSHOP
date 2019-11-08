package workshop.controllers.external.ajax;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import workshop.controllers.utils.UserMessagesJsonCreator;
import workshop.internal.entities.Phone;
import workshop.internal.entities.User;
import workshop.internal.entities.hibernateValidation.Persist;
import workshop.internal.services.PhonesService;
import workshop.internal.services.UsersService;

@Controller
@RequestMapping(path = "/ajax")
public class PhonesAjaxController {
	
	@Autowired
	private UserMessagesJsonCreator userMessagesJsonCreator;
	
	@Autowired
	private PhonesService phonesService;
	
	@Autowired
	private UsersService usersService;
	
	@PostMapping(path = "/phones", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public ResponseEntity<String> postPhone(@Validated({Persist.class}) @ModelAttribute(name = "phone")
		Phone phone, BindingResult bindingResult, Authentication authentication) {
		
		if (bindingResult.hasErrors()) {
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
				.body(userMessagesJsonCreator.convertBindingResultToJson(bindingResult));
		}
		User user = usersService.findByLogin(authentication.getName());
		phone.setUser(user);
		phonesService.persistEntity(phone);
		user.getPhones().add(phone);
		usersService.mergeEntity(user);
		return ResponseEntity.ok().build();
	}
	
	@DeleteMapping(path = "/phones/{phoneId}")
	public ResponseEntity<String> deletePhone(@PathVariable(name = "phoneId") Long phoneId,
		Authentication authentication) {
		
		Phone phone = phonesService.findById(phoneId);
		if (phone.getUser() != null &&
			phone.getUser().getEmail() != null &&
			phone.getUser().getEmail().equals(authentication.getName())) {
			phonesService.removeEntity(phoneId);
			return ResponseEntity.status(HttpStatus.OK).build();
		} else {
			return ResponseEntity.notFound().build();
		}
	}
}
