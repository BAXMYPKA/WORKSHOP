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
	
	/**
	 * @param size     min = 1, max = 50
	 * @param page     min = 1
	 * @param sortBy   If "default" of empty a List will be ordered by CreationDate
	 * @param ordering May by empty of "default"
	 * @return
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<Order> findAllOrders(int size, int page, String sortBy, String ordering) throws IllegalArgumentException {
		if (size == 0 || size > 50 || page == 0 || sortBy == null || ordering == null) {
			throw new IllegalArgumentException("Size, Page or SortBy contain wrong values!");
		}
		if ((!ordering.isEmpty() && ("asc".equalsIgnoreCase(ordering) || "desc".equalsIgnoreCase(ordering)))) {
			
			try {
				List<Order> orders = ordersDao.findAll(size, page, sortBy, ordering);
				return orders;
			} catch (PersistenceException e) {
				log.trace("No Orders found.");
				return Collections.<Order>emptyList();
			}
		} else {
			throw new IllegalArgumentException("Ordering must contain 'ASC' or 'DESC' values!");
		}
	}
}
