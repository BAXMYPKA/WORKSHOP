package internal.hateoasResources;

import internal.controllers.OrdersController;
import internal.entities.Order;
import org.springframework.stereotype.Component;

@Component
public class OrdersResourceAssembler extends WorkshopEntitiesResourceAssemblerAbstract<Order> {
	
	public OrdersResourceAssembler() {
		super(OrdersController.class, Order.class);
	}
}
