package workshop.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ShutdownController {
	
	@Autowired
	private ApplicationContext applicationContext;
	
	@GetMapping(path = "/shutdown")
	public void getShutdown() {
		int exitCode = SpringApplication.exit(applicationContext, () -> 10);
		System.exit(exitCode);
	}
}
