package internal.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping(path = "/internal/login")
public class LoginController {
	
	@GetMapping
	public String getLogin(){
		log.trace("Internal login entered");
		return "login";
	}
}
