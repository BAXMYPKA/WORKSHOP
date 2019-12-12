package workshop.controllers.internal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Controller
@RequestMapping(path = "/internal/application")
public class InternalApplicationController {
	
	@GetMapping
	public String getInternal() {
		log.trace("application.html will be returned");
		return "internal/application";
	}
}
