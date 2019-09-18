package workshop.internal.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Locale;

@Slf4j
@Controller
@RequestMapping(path = "/internal/login")
public class LoginController {
	
	@GetMapping
	public String getLogin(Locale locale){
		log.trace("Internal login entered with locale={}", locale.getDisplayLanguage());
		return "login";
	}
}
