package workshop.controllers.external;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import workshop.controllers.WorkshopControllerAbstract;

@Controller
public class UpperMenuDemoController extends WorkshopControllerAbstract {
	
	@GetMapping(path = "/purposes-demo")
	public String getPurposes() {
		return "purposes-demo";
	}
	
	@GetMapping(path = "/how-it-works-demo")
	public String getHowItWorks() {
		return "howItWorks-demo";
	}
	
	@GetMapping(path = "/authentication-demo")
	public String getAuthentication() {
		return "authentication-demo";
	}
	
	@GetMapping(path = "/authorization-demo")
	public String getAuthorization() {
		return "authorization-demo";
	}
	
	@GetMapping(path = "/under-construction-demo")
	public String getUnderConstruction() {
		return "underConstruction-demo";
	}
	
	@GetMapping(path = "/technologies-demo")
	public String getTechnologies() {
		return "technologies-demo";
	}
	
	@GetMapping(path = "/about-demo")
	public String getAbout() {
		return "about-demo";
	}
}
