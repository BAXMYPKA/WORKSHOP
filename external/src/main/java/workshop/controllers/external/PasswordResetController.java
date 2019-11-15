package workshop.controllers.external;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import workshop.controllers.WorkshopControllerAbstract;

@Controller
@RequestMapping(path = "/password-reset")
public class PasswordResetController extends WorkshopControllerAbstract {
	
	@GetMapping
	public String getPasswordReset() {
		return "passwordReset";
	}
}
