package workshop.utils;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

@Component
public class EmailTemplates {
	
	SimpleMailMessage getOrderFinishedEmailTemplate() {
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		//TODO: to template
		return mailMessage;
	}
}
