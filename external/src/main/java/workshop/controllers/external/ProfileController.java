package workshop.controllers.external;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import workshop.controllers.WorkshopControllerAbstract;
import workshop.internal.services.UsersService;

@Controller
@RequestMapping(path = "/profile")
public class ProfileController extends WorkshopControllerAbstract {
	
	@Autowired
	private UsersService usersService;
	
	@GetMapping
	public String getProfile(Model model, Authentication authentication) {
		return "profile";
	}
	
}

