package workshop.controllers;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

@NoArgsConstructor
@Slf4j
@Controller
@SessionAttributes(names = {"headerContentLanguageValue", "language"})
public class WorkshopControllerAbstract implements WorkshopController {
	
	/**
	 * The value of http header 'Content-Language' for currently supported languages to be passed into views
	 * for the language switcher.
	 */
	@Value("${supportedLanguages}")
	private String headerContentLanguageValue;
	
	@Value("${langCookieName}")
	private String langCookieName;
	
	/**
	 * Adds a List of currently supported languages to the current session for html language selector to pick up from.
	 * The languages list is obtained from 'workshop.properties' "headerContentLanguageValue=[lang1,lang2,...etc]
	 */
	@ModelAttribute(name = "headerContentLanguageValue")
	public void addSupportedLanguages(Model model) {
		log.trace("SupportedLanguages={} are added into the Session.", headerContentLanguageValue);
		model.addAttribute("supportedLanguages", headerContentLanguageValue.split(","));
	}
	
	/**
	 * Adds a currently selected language from user's cookie "lang' as a session attribute to be displayed instead of
	 * default.
	 * If no cookie presented, 'RU" will be displayed as a default.
	 */
	@ModelAttribute(name = "language")
	public void addCurrentLanguage(Model model, HttpServletRequest request) {
		if (Arrays.stream(request.getCookies()).anyMatch(cookie -> cookie.getName().equals(langCookieName))) {
			model.addAttribute("language",
				Arrays.stream(request.getCookies())
					.filter(cookie -> cookie.getName().equals(langCookieName))
					.findFirst()
					.orElse(new Cookie(langCookieName, "RU"))
					.getValue().toUpperCase());
		}
	}
}
