package workshop.controllers.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.context.request.WebRequest;
import workshop.controllers.WorkshopControllerAbstract;

@Slf4j
@Controller
public class ExternalLoginController extends WorkshopControllerAbstract {
	
	@GetMapping(path = "/login")
	public String getLogin(WebRequest webRequest) {
		return "redirect:/#loginModalWindow";
	}
	
	
	@PostMapping(path = "/login")
	public String postLogin(WebRequest webRequest) {
		return "login";
	}
}
