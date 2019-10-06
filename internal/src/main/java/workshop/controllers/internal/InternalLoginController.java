package workshop.controllers.internal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.exceptions.TemplateProcessingException;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping(path = "/internal/login")
public class InternalLoginController {
	
	@Value("${supportedLanguages}")
	private String headerContentLanguageValue;
	
	@Value("${placeholder.adminPassword}")
	private String placeholderAdminPassword;
	
	@Value("${placeholder.adminLogin}")
	private String placeholderAdminLogin;
	
	@Autowired
	private MessageSource messageSource;
	
	@GetMapping
	public String getLogin(Model model, Locale locale) {
		log.trace("Internal login entered with locale={}", locale.getDisplayLanguage());
		Map<String, String> placeholders = new HashMap<>();
		placeholders.put("adminLogin", placeholderAdminLogin);
		placeholders.put("adminPassword", placeholderAdminPassword);

		model.addAttribute("supportedLanguages", headerContentLanguageValue.split(","));
		model.addAttribute("placeholders", placeholders);
		
		return "login";
	}
	
	@PostMapping
	public String postLogin(Model model) {
		return "login";
	}
	
}
