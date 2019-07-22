package internal.dao;

import internal.entities.Order;
import org.springframework.stereotype.Repository;

@Repository
public class OrdersDao extends EntitiesDaoAbstract<Order, Long> {
	
	public OrdersDao() {
		super.setEntityClass(Order.class);
		super.setKeyClass(Long.class);
	}
}
