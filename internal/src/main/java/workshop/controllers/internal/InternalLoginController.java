package workshop.controllers.internal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.exceptions.TemplateProcessingException;
import workshop.internal.entities.Employee;
import workshop.internal.services.EmployeesService;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping(path = "/internal/login")
@SessionAttributes(names = {"loggedUsername"})
public class InternalLoginController {
	
	@Value("${supportedLanguages}")
	private String headerContentLanguageValue;
	
	@Value("${placeholder.adminPassword}")
	private String placeholderAdminPassword; //Just for the presentation purposes
	
	@Value("${placeholder.adminLogin}")
	private String placeholderAdminLogin; //Just for the presentation purposes
	
	@Autowired
	private EmployeesService employeesService;
	
	@Autowired
	private MessageSource messageSource;
	
	@GetMapping
	public String getLogin(Model model, Locale locale) {
		log.trace("Internal login entered with locale={}", locale.getDisplayLanguage());
		
		Map<String, String> placeholders = new HashMap<>();
		placeholders.put("adminLogin", placeholderAdminLogin);
		placeholders.put("adminPassword", placeholderAdminPassword);
		
		List<Employee> employees = employeesService.findAllEntities(0, 0, "lastName", Sort.Direction.ASC);
		
		model.addAttribute("supportedLanguages", headerContentLanguageValue.split(","));
		model.addAttribute("placeholders", placeholders);
		model.addAttribute("employees", employees);
		model.addAttribute("loggedUsername", "");
		return "internal/login";
	}
	
//	@PostMapping
//	public String postLogin(Model model) {
//		return "internal/login";
//	}
	
}
