package workshop.controllers.external;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import workshop.controllers.WorkshopControllerAbstract;
import workshop.internal.entities.ClassifiersGroup;
import workshop.internal.services.ClassifiersGroupsService;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class UpperMenuController extends WorkshopControllerAbstract {
	
	@Autowired
	private ClassifiersGroupsService classifiersGroupsService;
	
	@GetMapping(path = "/services")
	public String getPurposes(Model model) {
		return "services";
	}
	
/*
	@GetMapping(path = "/how-it-works-demo")
	public String getHowItWorks() {
		return "howItWorks-demo";
	}
*/
	
	/**
	 * Adds ClassifierGroupsNames as Strings for the Services List dropdown UpperMenu
	 */
	@ModelAttribute(name = "classifiersGroupsNames")
	public void setClassifiersGroups(Model model) {
		List<String> classifiersGroupsNames =
			classifiersGroupsService.findAllEntities(0, 0, "name", Sort.Direction.ASC)
			.stream()
			.map(ClassifiersGroup::getName)
			.collect(Collectors.toList());
		model.addAttribute("classifiersGroupsNames", classifiersGroupsNames);
	}
	
}
