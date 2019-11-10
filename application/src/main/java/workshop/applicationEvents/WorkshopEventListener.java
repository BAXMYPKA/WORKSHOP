package workshop.applicationEvents;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import workshop.internal.entities.Order;
import workshop.applicationEvents.OrderFinishedEvent;
import workshop.internal.entities.User;
import workshop.utils.EmailTemplates;
import workshop.utils.WorkshopEmailService;

/**
 * Depending on kind of caught {@link ApplicationEvent} this class reacts accordingly:
 * 	creates and sends emails, do phone SMS-servicing etc.
 */
@Slf4j
@Component
public class WorkshopEventListener {
	
	@Autowired
	private EmailTemplates emailTemplates;
	
	@Autowired
	private WorkshopEmailService emailService;
	
	/**
	 *
	 * @param event Special king of Workshop {@link ApplicationEvent} which shows that a particular {@link Order}
	 *              is ready (its {@link Order#getFinished()} is set or all its {@link Order#getTasks()} same
	 *              properties are set).
	 */
	@Async
	@EventListener
	public void orderFinishedEventListener(OrderFinishedEvent event) {
		log.debug("Order finished event for Order.ID={} to send an Email to User.ID={}",
			event.getOrder().getIdentifier(), event.getOrder().getCreatedFor().getIdentifier());
		//if User has an email or a Phone
		Order finishedOrder = event.getOrder();
		if (finishedOrder.getCreatedFor().getEmail() != null) {
			SimpleMailMessage orderFinishedEmail = emailTemplates.getOrderFinishedEmailTemplate(finishedOrder);
			emailService.sendSimpleMessage(orderFinishedEmail);
		}
		if (finishedOrder.getCreatedFor().getPhones() != null) {
			//TODO: Phone service is not ready yet
		}
	}
	
	@Async
	@EventListener()
	public void userRegisteredEventListener(UserRegisteredEvent event) {
		log.debug("New User registered event for User.ID={} to send an Email to", event.getUser().getIdentifier());
		User registeredUser = event.getUser();
		if (registeredUser.getEmail() != null) {
//			SimpleMailMessage registrationConfirmationEmail =
//				emailTemplates.getOrderFinishedEmailTemplate(null);
//			emailService.sendSimpleMessage(registrationConfirmationEmail);
		}
	}
}
