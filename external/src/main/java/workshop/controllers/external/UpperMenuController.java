package workshop.controllers.external;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import workshop.controllers.WorkshopControllerAbstract;

@Controller
public class UpperMenuController extends WorkshopControllerAbstract {
	
	@GetMapping(path = "/purposes")
	public String getPurposes() {
		return "purposes";
	}
	
	@GetMapping(path = "/how-it-works")
	public String getHowItWorks() {
		return "howItWorks";
	}
	
	@GetMapping(path = "/authentication")
	public String getAuthentication() {
		return "authentication";
	}
	
	@GetMapping(path = "/authorization")
	public String getAuthorization() {
		return "authorization";
	}
	
	@GetMapping(path = "/under-construction")
	public String getUnderConstruction() {
		return "underConstruction";
	}
	
	@GetMapping(path = "/technologies")
	public String getTechnologies() {
		return "technologies";
	}
	
	@GetMapping(path = "/about")
	public String getAbout() {
		return "about";
	}
}
