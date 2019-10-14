package workshop.internal.entities.utils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import workshop.internal.entities.Order;
import workshop.internal.entities.Task;

/**
 * Special class only for particular type of {@link ApplicationEvent} which invokes in {@link Order#preUpdate()}
 * when all {@link Order}'s {@link workshop.internal.entities.Task}'s are set to {@link Task#getFinished()} or the
 * Order itself is set to 'finished' state.
 */
@Getter
@Slf4j
public class OrderFinishedEvent extends ApplicationEvent {
	
	private Order order;
	
	public OrderFinishedEvent(Order order) {
		super(order);
		this.order = order;
	}
}
