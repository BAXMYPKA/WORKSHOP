package workshop.internal.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.exceptions.TemplateProcessingException;

import java.util.Locale;

@Slf4j
@Controller
@RequestMapping(path = "/internal/login")
public class LoginController {
	
	@Value("${supportedLanguages}")
	private String headerContentLanguageValue;
	
	@Autowired
	private MessageSource messageSource;
	
	@GetMapping
	public String getLogin(Model model, Locale locale) {
		log.trace("Internal login entered with locale={}", locale.getDisplayLanguage());
		model.addAttribute("supportedLanguages", headerContentLanguageValue.split(","));
		return "login";
	}
	
/*
	@ExceptionHandler({TemplateProcessingException.class})
	@ResponseBody
	public ResponseEntity<String> thymeleafExceptions(TemplateProcessingException tee) {
		log.error(tee.getMessage(), tee);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
			messageSource.getMessage(
				"httpStatus.internalServerError.common", null, LocaleContextHolder.getLocale()));
	}
*/

}
