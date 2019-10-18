package workshop.controllers.external;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import workshop.controllers.WorkshopControllerAbstract;

@Controller
public class ServicesController extends WorkshopControllerAbstract {
	
	@GetMapping(path = "/services")
	public String getServices(Model model) {
		return "services";
	}
	
	@ModelAttribute(name = "classifiersGroup")
	public void setClassifiersGroups(Model model) {
		//TODO: 
	}
}
