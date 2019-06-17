package internal.service;

import internal.dao.OrdersDao;
import internal.entities.Order;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.PersistenceException;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class OrdersService {
	
	@Getter
	@Setter
	@Autowired
	public OrdersDao ordersDao;
	
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<Order> findAllOrders(){
		try {
			List<Order> orders = ordersDao.findAll();
			return orders;
		} catch (PersistenceException e) {
			log.trace("No Orders found.");
			return Collections.<Order>emptyList();
		}
	}
}
