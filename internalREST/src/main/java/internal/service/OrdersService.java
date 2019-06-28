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
import java.util.Optional;

@Slf4j
@Service
public class OrdersService {
	
	@Getter
	@Setter
	@Autowired
	public OrdersDao ordersDao;
	
	public static int PAGE_SIZE = 150;
	
	/**
	 * @param pageSize    min = 1, max = this.PAGE_SIZE In case of incorrect values the size will be set in between min and max
	 * @param pageNum    min = 1, max = 100. In case of incorrect values the page will be set in between min and max
	 * @param sortBy  If "default" or empty - a List will be ordered by CreationDate
	 * @param ascDesc May by empty of "default"
	 * @return
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public Optional<List<Order>> findAllOrders(int pageSize, int pageNum, String sortBy, String ascDesc)
		throws IllegalArgumentException {
		
		pageSize = pageSize <= 0 || pageSize > PAGE_SIZE ? PAGE_SIZE : pageSize;
		pageNum = pageNum <= 0 || pageNum > 5000 ? 1 : pageNum ;
		
		try {
			Optional<List<Order>> orders = ordersDao.findAll(
				pageSize,
				pageNum,
				sortBy == null ? "" : sortBy,
				ascDesc == null ? "desc" : "asc".equalsIgnoreCase(ascDesc) ? "asc" : "desc");
			return orders;
		} catch (PersistenceException e) {
			log.trace("No Orders found.");
			return Optional.of(Collections.<Order>emptyList());
		}
	}
	
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public Optional<Order> findById(long id) {
		if (id <= 0){
			throw new IllegalArgumentException("id cannot by 0 or below!");
		}
//		Order order = ordersDao.findById(id);
		Optional<Order> order = ordersDao.findById(id);
		return order;
	}
}
