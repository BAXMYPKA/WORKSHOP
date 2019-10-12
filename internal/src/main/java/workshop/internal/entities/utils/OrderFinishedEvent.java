package workshop.internal.entities.utils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import workshop.internal.entities.Order;

@Getter
@Slf4j
public class OrderFinishedEvent extends ApplicationEvent {
	
	private Order order;
	
	public OrderFinishedEvent(Order order) {
		super(order);
		this.order = order;
	}
}
