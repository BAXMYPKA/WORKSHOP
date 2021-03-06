package workshop.controllers;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import workshop.controllers.utils.UserMessagesCreator;
import workshop.internal.entities.ClassifiersGroup;
import workshop.internal.services.ClassifiersGroupsService;
import workshop.internal.services.UsersService;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@Slf4j
@Controller
@SessionAttributes(names = {"headerContentLanguageValue", "language", "loggedUsername"})
public class WorkshopControllerAbstract implements WorkshopController {
	
	@Getter(AccessLevel.PROTECTED)
	@Autowired
	private MessageSource messageSource;
	
	/**
	 * The value of http header 'Content-Language' for currently supported languages to be passed into views
	 * for the language switcher.
	 */
	@Value("${supportedLanguages}")
	private String headerContentLanguageValue;
	
	@Value("${langCookieName}")
	private String langCookieName;
	
	@Autowired
	private ClassifiersGroupsService classifiersGroupsService;
	
	@Autowired
	private UsersService usersService;
	
	@Getter(AccessLevel.PROTECTED)
	@Autowired
	private UserMessagesCreator userMessagesCreator;
	
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
		if (request.getCookies() != null &&
			Arrays.stream(request.getCookies()).anyMatch(cookie -> cookie.getName().equals(langCookieName))) {
			model.addAttribute("language",
				Arrays.stream(request.getCookies())
					.filter(cookie -> cookie.getName().equals(langCookieName))
					.findFirst()
					.orElse(new Cookie(langCookieName, "RU"))
					.getValue().toUpperCase());
		}
	}
	
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
	
	@ModelAttribute(name = "username")
	public void setAuthenticationName(Model model, Authentication authentication) {
		if (authentication != null && !authentication.getName().contains("Anonymous")) {
			model.addAttribute("loggedUsername", authentication.getName());
		} else {
			model.addAttribute("loggedUsername", "");
		}
	}
	
	/**
	 * Adds all predefined Users for the table only for not logged in Users to observe.
	 * @param model
	 * @param authentication
	 */
	@ModelAttribute(name = "usersTable")
	public void setPredefinedUsersTable(Model model, Authentication authentication) {
		if (authentication != null && !authentication.getName().contains("Anonymous")) {
			model.addAttribute("usersTable", null);
		} else {
			model.addAttribute("usersTable",
				usersService.findAllEntities(10, 0, "lastName", Sort.Direction.ASC));
		}
	}
	
}
