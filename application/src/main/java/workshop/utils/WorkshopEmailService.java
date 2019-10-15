package workshop.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Just an email sender class.
 */
@Slf4j
@Component
public class WorkshopEmailService {
	
	@Autowired
	private JavaMailSender javaMailSender; //Got its mail properties from application.properties 'spring.mail. ...'
	
	/**
	 * @param mailMessage Ready to be sent SimpleMailMessage
	 * @throws MailException MailParseException - in case of failure when parsing the message
	 *                       MailAuthenticationException - in case of authentication failure
	 *                       MailSendException - in case of failure when sending the message
	 */
	public void sendSimpleMessage(SimpleMailMessage mailMessage) throws MailException {
		log.debug("SimpleMailMessage to={} will be sent",
			Objects.requireNonNull(mailMessage.getTo(), "'to' property cannot be null!")[0]);
		try {
			javaMailSender.send(mailMessage);
		} catch (MailParseException mpe) {
			log.error("Mail template is unprocessable! It is impossible to send mails!\n\t" + mpe.getMessage(), mpe);
		} catch (MailAuthenticationException mae) {
			log.error("Impossible to send mail due to error authorization! Check email credentials!\n\t" + mae.getMessage(), mae);
		} catch (MailSendException mse) {
			log.info("It is look like recipient's email address is wrong! Check email credentials for={}", mailMessage.getTo()[0], mse);
		}
	}
}
