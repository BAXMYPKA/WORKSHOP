package internal.service;

import internal.dao.OrdersDao;
import internal.entities.Order;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
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
	
	public static final int PAGE_SIZE = 150;
	public static final int MAX_PAGE_NUM = 5000;
	
	/**
	 * @param pageSize    min = 1, max = this.PAGE_SIZE In case of incorrect values the size will be set in between min and max
	 * @param pageNum    min = 1, max = 100. In case of incorrect values the page will be set in between min and max
	 * @param orderBy  If "default" or empty - a List will be ordered by CreationDate
	 * @param order ENUM from Sort.Direction with "ASC" or "DESC" values
	 * @return
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public Optional<List<Order>> findAllOrders(int pageSize, int pageNum, String orderBy, Sort.Direction order)
		throws IllegalArgumentException {
		pageSize = pageSize <= 0 || pageSize > PAGE_SIZE ? PAGE_SIZE : pageSize;
		pageNum = pageNum <= 0 || pageNum > 5000 ? 1 : pageNum ;
		try {
			Optional<List<Order>> orders = ordersDao.findAll(
				pageSize,
				pageNum,
				orderBy == null ? "" : orderBy,
				order);
			
			return orders;
		} catch (PersistenceException e) {
			log.trace("No Orders found.");
			return Optional.of(Collections.<Order>emptyList());
		}
		
	}
	
	/**
	 * @param pageable Must contain Sort.by(Sort.Direction, orderBy) or Sort.of(Sort.Direction, "created") property!
	 * @param orderBy
	 * @return
	 * @throws IllegalArgumentException
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public Page<Order> findAllOrders(Pageable pageable, String orderBy) throws IllegalArgumentException {
		int pageSize = pageable.getPageSize() < 0 || pageable.getPageSize() > PAGE_SIZE ? PAGE_SIZE : pageable.getPageSize();
		int pageNum = pageable.getPageNumber() <= 0 || pageable.getPageNumber() > MAX_PAGE_NUM ? 1 :
			pageable.getPageNumber();
//		orderBy = orderBy == null ? "" : orderBy;
		
		try {
			Optional<List<Order>> orders = ordersDao.findAll(
				pageSize,
				pageNum,
				orderBy == null ? "" : orderBy,
				pageable.getSort().getOrderFor(orderBy == null || orderBy.isEmpty() ? "created" : orderBy).getDirection());
			
			long total = ordersDao.countAllEntities();
			
			Page<Order> page = new PageImpl<Order>(orders.orElse(Collections.<Order>emptyList()), pageable, total);
			
			return page;
		} catch (PersistenceException e) {
			log.trace("No Orders found.");
			return new PageImpl<Order>(Collections.<Order>emptyList());
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
	
	@Transactional(propagation = Propagation.REQUIRED)
	public Optional<Order> persistOrder(Order order) throws IllegalArgumentException {
		if (order == null){
			throw new IllegalArgumentException("Order cannot by null!");
		}
		Optional<Order> persistedOrder = Optional.ofNullable(ordersDao.persistEntity(order));
		return persistedOrder;
	}
}
