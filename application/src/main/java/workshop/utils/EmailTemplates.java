package workshop.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;
import workshop.internal.entities.Order;

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
	
	SimpleMailMessage getOrderFinishedEmailTemplate(Order finishedOrder) {
		
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
}
