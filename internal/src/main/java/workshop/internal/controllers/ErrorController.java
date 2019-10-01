package workshop.internal.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class ErrorController {
	
	@GetMapping(path = "/error")
	@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
	public String getError() {
		return "error";
	}
}
