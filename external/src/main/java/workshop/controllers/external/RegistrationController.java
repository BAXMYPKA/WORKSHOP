package workshop.controllers.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import workshop.controllers.WorkshopControllerAbstract;

@Slf4j
@Controller
public class RegistrationController extends WorkshopControllerAbstract {
	
	@GetMapping
	public String getRegistration() {
		return "registration";
	}
}
