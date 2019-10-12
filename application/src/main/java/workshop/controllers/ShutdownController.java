package workshop.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * To catch a request to shutdown the Workshop Application
 */
@Controller
public class ShutdownController {
	
	@Autowired
	private ApplicationContext applicationContext;
	
	/**
	 * After catching a POST-request to this endpoint start a new Thread with a delay of 1 second to have a time to
	 * redirect a User to the Main page. Then shutdown.
	 * @return Redirects to "/" as the main endpoint.
	 */
	@PostMapping(path = "/shutdown")
	public String postShutdown() {
		
		Thread thread = new Thread(() -> {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				//
			}
			int exitCode = SpringApplication.exit(applicationContext, () -> 10);
			System.exit(exitCode);
		});
		thread.start();
		
		return "redirect:/";
	}
}
