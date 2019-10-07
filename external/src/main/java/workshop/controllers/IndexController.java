package workshop.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(path = "/")
public class IndexController extends WorkshopControllerAbstract {
	
	@GetMapping
	public String getIndex() {
		return "index";
	}
	
}
