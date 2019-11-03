package workshop.controllers.external.ajax;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import workshop.internal.entities.Phone;
import workshop.internal.services.PhonesService;
import workshop.internal.services.UsersService;

@Controller
@RequestMapping(path = "/ajax")
public class PhonesAjaxController {
	
	@Autowired
	private PhonesService phonesService;
	
	@DeleteMapping(path = "/phones/{phoneId}")
	public ResponseEntity<String> deletePhone(@PathVariable(name = "phoneId") Long phoneId,
		Authentication authentication) {
//		System.out.println(authentication.getName());
//		System.out.println(phoneId);
//		if (phoneId == 45) {
//			return ResponseEntity.ok().build();
//		} else {
//			return ResponseEntity.notFound().build();
//		}
		
		Phone phone = phonesService.findById(phoneId);
		if (phone.getUser() != null &&
			phone.getUser().getEmail() != null &&
			phone.getUser().getEmail().equals(authentication.getName())) {
			phonesService.removeEntity(phoneId);
//			phonesService.removeEntity(phone);
//			Phone byId = phonesService.findById(phoneId);
			
			return ResponseEntity.status(HttpStatus.OK).build();
		} else {
			return ResponseEntity.notFound().build();
		}
	}
}
