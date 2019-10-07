package workshop.controllers;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.SessionAttributes;

@NoArgsConstructor
@Slf4j
@Controller
@SessionAttributes(names = {"headerContentLanguageValue"})
public class WorkshopControllerAbstract implements WorkshopController {
	
	/**
	 * The value of http header 'Content-Language' for currently supported languages to be passed into views
	 * for the language switcher.
	 */
	@Value("${supportedLanguages}")
	private String headerContentLanguageValue;
	
}
