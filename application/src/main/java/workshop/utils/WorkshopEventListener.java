package workshop.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import workshop.internal.entities.Order;
import workshop.internal.entities.utils.OrderFinishedEvent;

@Slf4j
@Component
public class WorkshopEventListener {
	
	@Autowired
	private EmailTemplates emailTemplates;
	
	@EventListener
	public void orderFinishedEventListener(OrderFinishedEvent event) {
		log.debug("Order finished event for Order.ID={} to send an Email to User.ID={}",
			event.getOrder().getIdentifier(), event.getOrder().getCreatedFor().getIdentifier());
		
		//if User has email or if user has Phone
		Order finishedOrder = event.getOrder();
		if (finishedOrder.getCreatedFor().getEmail() != null) {
			emailTemplates
		}
		if (finishedOrder.getCreatedFor().getPhones() != null) {
			//TODO: Phone service is not completed
		}
		System.out.println(event.getOrder().getFinished());
	}
}
