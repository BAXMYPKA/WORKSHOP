package internal.services;

import internal.dao.OrdersDao;
import internal.entities.Order;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrdersService extends WorkshopEntitiesServiceAbstract<Order> {
	
	@Getter
	@Setter
	@Autowired
	public OrdersDao ordersDao;
	
	public OrdersService(OrdersDao ordersDao) {
		super(ordersDao);
	}
}
