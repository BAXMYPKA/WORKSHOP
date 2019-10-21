package workshop.controllers.external;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import workshop.controllers.WorkshopControllerAbstract;

@Controller
public class UpperMenuDemoController extends WorkshopControllerAbstract {
	
	@GetMapping(path = "/purposes-demo")
	public String getPurposes() {
		return "purposesDemo";
	}
	
	@GetMapping(path = "/how-it-works-demo")
	public String getHowItWorks() {
		return "howItWorksDemo";
	}
	
	@GetMapping(path = "/authentication-demo")
	public String getAuthentication() {
		return "authenticationDemo";
	}
	
	@GetMapping(path = "/authorization-demo")
	public String getAuthorization() {
		return "authorizationDemo";
	}
	
	@GetMapping(path = "/under-construction-demo")
	public String getUnderConstruction() {
		return "underConstructionDemo";
	}
	
	@GetMapping(path = "/technologies-demo")
	public String getTechnologies() {
		return "technologiesDemo";
	}
	
	@GetMapping(path = "/about-demo")
	public String getAbout() {
		return "aboutDemo";
	}
}
