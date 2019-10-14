package workshop.internal.entities.utils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;
import workshop.internal.entities.Order;

/**
 * Helper class.
 * As all the {@link workshop.internal.entities.WorkshopEntity} classes are only {@link javax.persistence.Entity}
 * but not Beans and cannot hold any {@link org.springframework.beans.factory.annotation.Autowired}
 * properties, this class lets those WorkshopEntities to send ApplicationEvents (such as {@link OrderFinishedEvent})
 * via static {@link #publishOrderFinishedEvent(Order)} method.
 */
@Getter(AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
@Slf4j
@Component
public class WorkshopEntitiesEventPublisher implements ApplicationEventPublisherAware {
	
	private static ApplicationEventPublisher applicationEventPublisher;
	
	public static void publishOrderFinishedEvent(Order finishedOrder) {
		OrderFinishedEvent event = new OrderFinishedEvent(finishedOrder);
		applicationEventPublisher.publishEvent(event);
		log.debug("OrderFinishedEvent for the finished Order.ID={} has been sent.", finishedOrder.getIdentifier());
	}
	
	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		WorkshopEntitiesEventPublisher.applicationEventPublisher = applicationEventPublisher;
	}
}
