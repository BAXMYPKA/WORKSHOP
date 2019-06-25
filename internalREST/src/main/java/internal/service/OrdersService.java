package internal.service;

import internal.dao.OrdersDao;
import internal.entities.Order;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
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
	 * @param size    min = 1, max = 50. In case of incorrect values the size will be set in between min and max
	 * @param page    min = 1, max = 100. In case of incorrect values the page will be set in between min and max
	 * @param sortBy  If "default" or empty - a List will be ordered by CreationDate
	 * @param ascDesc May by empty of "default"
	 * @return
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<Order> findAllOrders(int size, int page, String sortBy, String ascDesc)
		throws IllegalArgumentException {
		
		int correctSize = size <= 0 ? 1 : size > 50 ? 50 : 1;
		int correctPage = page <= 0 ? 1 : page > 100 ? 50 : page;
		
		try {
			List<Order> orders = ordersDao.findAll(
				correctSize,
				correctPage,
				sortBy == null ? "" : sortBy,
				ascDesc == null ? "desc" : "asc".equalsIgnoreCase(ascDesc) ? "asc" : "desc");
			return orders;
		} catch (PersistenceException e) {
			log.trace("No Orders found.");
			return Collections.<Order>emptyList();
		}
	}
}
