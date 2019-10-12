package workshop.internal.entities.utils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import workshop.internal.entities.Order;

/**
 * As {@link workshop.internal.entities.WorkshopEntity} cannot be Beans and hold {@link org.springframework.beans.factory.annotation.Autowired}
 * properties, this class lets Entities to send ApplicationEvents by static {@link #publishOrderFinishedEvent(Order)}
 * method.
 */
@Getter(AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
@Component
public class WorkshopEntitiesEventPublisher implements ApplicationContextAware {
	
	private static ApplicationContext applicationContext;
	
	public static void publishOrderFinishedEvent(Order finishedOrder) {
		ApplicationEventPublisher eventPublisher =
			(ApplicationEventPublisher) applicationContext.getBean("eventPublisher");
		OrderFinishedEvent event = new OrderFinishedEvent(finishedOrder);
		eventPublisher.publishEvent(event);
		
	}
	
	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		applicationContext = context;
	}
}
