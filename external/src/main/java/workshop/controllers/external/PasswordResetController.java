package workshop.controllers.external;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import workshop.controllers.WorkshopControllerAbstract;
import workshop.exceptions.EntityNotFoundException;
import workshop.internal.entities.Uuid;
import workshop.internal.services.UuidsService;

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
	public String getPasswordResetUuid(@RequestParam(name = "uuid") String uuid) {
		try {
			Uuid resetPassUuid = uuidsService.findByProperty("uuid", uuid).get(0);
		} catch (EntityNotFoundException e) {
			//
		}
		return "passwordReset";
	}
	
}
