package workshop.controllers.external.ajax;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import workshop.internal.entities.Phone;
import workshop.internal.entities.hibernateValidation.Persist;
import workshop.internal.services.PhonesService;

@Controller
@RequestMapping(path = "/ajax")
public class PhonesAjaxController {
	
	@Autowired
	private PhonesService phonesService;
	
	@PostMapping(path = "/phones", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public ResponseEntity<String> postPhone(@Validated({Persist.class}) @ModelAttribute(name = "phone") Phone phone,
		BindingResult bindingResult) {
		
		if (bindingResult.hasErrors()) {
			StringBuilder errors = new StringBuilder();
			for (ObjectError objectError : bindingResult.getAllErrors()) {
				errors.append("\"errorFieldName\":\"")
					.append(objectError.getObjectName())
					.append("\",")
					.append("\"errorFieldMessage\":\"")
					.append(objectError.getDefaultMessage().replace("'", "\"").replace("+", "\\+"))
					.append("\"");
			}
			errors.insert(0, "{").insert(errors.length(), "}");
			
			bindingResult.getAllErrors().stream()
				.forEach(objectError -> {
						System.out.println(objectError.getObjectName() + " + " + objectError.getDefaultMessage());
					}
				);
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(errors.toString());
		}
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
