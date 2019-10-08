package workshop.controllers.internal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(path = "/internal")
public class InternalController {
	
	@GetMapping(produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public String getInternal() {
		log.error("MAIN");
		return "Success";
	}
	
	@GetMapping(path = "/a", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public String getTest() {
		log.error("MAIN");
		return "Success";
	}
}