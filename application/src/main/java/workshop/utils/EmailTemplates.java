package workshop.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import workshop.applicationEvents.UserRegisteredEvent;
import workshop.internal.entities.Order;

import java.net.URL;
import java.util.Locale;

/**
 * Emails templates container for any kinds of Workshop emails with any kinds of {@link org.springframework.mail.MailMessage}
 */
@Slf4j
@Component
public class EmailTemplates {
	
	@Autowired
	private MessageSource messageSource;
	
	@Value("${default.languageTag}")
	private String defaultLanguageTag;
	
	@Value("${email.orderFinished.from}")
	private String orderFinishedFrom;
	
	@Value("${url}")
	private String workshopUrl;
	
	public SimpleMailMessage getOrderFinishedEmailTemplate(Order finishedOrder) {
		
		Locale locale =
			Locale.forLanguageTag(finishedOrder.getCreatedFor().getLanguageTag()).toLanguageTag() != null ?
				Locale.forLanguageTag(finishedOrder.getCreatedFor().getLanguageTag()) :
				Locale.forLanguageTag(defaultLanguageTag);
		
		String subject = messageSource.getMessage(
			"email.simpleMessage.subject.orderFinished(1)",
			new Object[]{finishedOrder.getIdentifier()},
			locale);
		
		String messageToUser = finishedOrder.getMessageToUser() != null ? finishedOrder.getMessageToUser() : "";
		
		String text = messageSource.getMessage(
			"email.simpleMessage.text.orderFinished(2)",
			new Object[]{finishedOrder.getIdentifier(), messageToUser},
			locale);
		
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setSubject(subject);
		mailMessage.setTo(finishedOrder.getCreatedFor().getEmail());
		mailMessage.setFrom(orderFinishedFrom);
		mailMessage.setText(text);
		
		log.debug("SimpleMailMessage for the finished Order.ID={} is created.", finishedOrder.getIdentifier());
		
		return mailMessage;
	}
	
	//TODO: to complete
	public MimeMailMessage getUserRegistrationConfirmationEmailTemplate(UserRegisteredEvent userRegisteredEvent) {
		
		//Language tag may not be supported by the WorkshopApplication
		Locale locale =
			Locale.forLanguageTag(userRegisteredEvent.getUuid().getUser().getLanguageTag()).toLanguageTag() != null ?
				Locale.forLanguageTag(userRegisteredEvent.getUuid().getUser().getLanguageTag()) :
				Locale.forLanguageTag(defaultLanguageTag);
		
		String subject = messageSource.getMessage(
			"email.mimeMessage.subject.registrationConfirmation", null, locale);
		
		//The below is the real URL to be sent
//		URL link = new URL("https", "workshop", "443", "/registration?uuid=");
		
		String url = workshopUrl + "registration?uuid=" + userRegisteredEvent.getUuid().getUuid();
		String href = "<a href=\"" + url + "\">Workshop.pro</a>";
		
		String htmlText = messageSource.getMessage("email.mimeMessage.text.registrationConfirmation(2)",
			new Object[]{workshopUrl, href}, locale);
		
/*
		MimeMessageHelper helper = new MimeMessageHelper()
		
		MimeMailMessage mailMessage = new MimeMailMessage();
		mailMessage.setSubject(subject);
		mailMessage.setTo(userRegisteredEvent.getCreatedFor().getEmail());
		mailMessage.setFrom(orderFinishedFrom);
		mailMessage.setText(htmlText);
		
		log.debug("SimpleMailMessage for the finished Order.ID={} is created.", userRegisteredEvent.getIdentifier());
		
		return mailMessage;
*/
		
		return null;
	}
	
}
