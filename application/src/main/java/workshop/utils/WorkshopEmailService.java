package workshop.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class WorkshopEmailService {
	
	@Autowired
	private JavaMailSender javaMailSender; //Got its mail properties from application.properties 'spring.mail. ...'
	
	public void sendSimpleMessage(SimpleMailMessage mailMessage) {
		javaMailSender.send(mailMessage);
	}
}
