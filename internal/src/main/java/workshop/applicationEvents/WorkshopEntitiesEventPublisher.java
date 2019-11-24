package workshop.applicationEvents;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;
import workshop.internal.entities.Order;
import workshop.internal.entities.User;
import workshop.internal.entities.Uuid;
import workshop.internal.entities.WorkshopEntity;

import javax.persistence.Entity;

/**
 * Helper class.
 * As all the {@link WorkshopEntity} classes are only {@link Entity}
 * but not Beans and cannot hold any {@link Autowired} instances, this class lets those WorkshopEntities to send
 * {@link ApplicationEvent}s (such as {@link OrderFinishedEvent} etc) via static methods
 * (e.g. {@link #publishOrderFinishedEvent(Order)}) .
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
		log.debug("OrderFinishedEvent for the finished Order.ID={} has been published.", finishedOrder.getIdentifier());
	}
	
	public static void publishUserRegisteredEvent(Uuid uuid) {
		UserRegisteredEvent event = new UserRegisteredEvent(uuid);
		applicationEventPublisher.publishEvent(event);
		log.debug("UserRegisteredEvent for the User.ID={} has been published.", uuid.getUser().getIdentifier());
	}
	
	public static void publishUserPasswordResetEvent(Uuid uuid) {
		UserPasswordResetEvent event = new UserPasswordResetEvent(uuid);
		applicationEventPublisher.publishEvent(event);
		log.debug("UserPasswordResetEvent for the User.ID={} has been published.", uuid.getPasswordResetUser().getIdentifier());
	}
	
	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		WorkshopEntitiesEventPublisher.applicationEventPublisher = applicationEventPublisher;
	}
}
