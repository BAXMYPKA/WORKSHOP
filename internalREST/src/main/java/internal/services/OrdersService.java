package internal.services;

import internal.dao.OrdersDao;
import internal.dao.TasksDao;
import internal.entities.Order;
import internal.entities.Task;
import internal.entities.User;
import internal.exceptions.EntityNotFoundException;
import internal.exceptions.IllegalArgumentsException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class OrdersService extends WorkshopEntitiesServiceAbstract<Order> {
	
	@Getter
	@Setter
	@Autowired
	private OrdersDao ordersDao;
	@Autowired
	private TasksDao tasksDao;
	
	public OrdersService(OrdersDao ordersDao) {
		super(ordersDao);
	}
	
	/**
	 * @param orderId
	 * @return User from the given Order or throws EntityNotFoundException with HttpStatus and localized message
	 * @throws IllegalArgumentsException If 'orderId' == null, 0 or < 0.
	 * @throws EntityNotFoundException   If no Order's been found or no User's presented in that Order as 'createdFor' property.
	 *                                   With the fully localized message
	 */
	@Transactional(propagation = Propagation.REQUIRED, readOnly = true, isolation = Isolation.READ_COMMITTED)
	public User findUserByOrder(Long orderId) throws IllegalArgumentsException, EntityNotFoundException {
		
		super.verifyIdForNullZeroBelowZero(orderId);
		
		Optional<Order> order = ordersDao.findById(orderId);
		try {
			return order.orElseThrow(() -> new EntityNotFoundException("No Order with such an Id found!",
				HttpStatus.NOT_FOUND,
				getMessageSource().getMessage("httpStatus.notFound(1)", new Object[]{"OrderId=" + orderId},
					LocaleContextHolder.getLocale())))
				
				.getCreatedFor();
		} catch (NullPointerException npe) {
			throw new EntityNotFoundException("No User in 'createdFor' found!", "httpStatus.notFound", HttpStatus.NOT_FOUND);
		}
	}
	
/*
	*/
/**
	 * @param orderId Self-description.
	 * @return A Set of Tasks from the given Order or throws EntityNotFoundException with appropriate HttpStatus.
	 * @throws IllegalArgumentsException If the given 'orderID' is null, zero or below.
	 * @throws EntityNotFoundException   If no Order or Tasks from if were found.
	 *//*

	@Transactional(propagation = Propagation.REQUIRED, readOnly = true, isolation = Isolation.SERIALIZABLE)
	public Set<Task> findAllTasksByOrder(Long orderId) throws EntityNotFoundException, IllegalArgumentsException {
		super.verifyIdForNullZeroBelowZero(orderId);
		try {
			return ordersDao.findById(orderId).orElseThrow(() ->
				new EntityNotFoundException("No Order with such an ID!", HttpStatus.NOT_FOUND, getMessageSource().getMessage(
					"httpStatus.notFound(1)", new Object[]{"OrderID=" + orderId}, LocaleContextHolder.getLocale())))
				.getTasks();
		} catch (NullPointerException npe) {
			throw new EntityNotFoundException(
				"No Tasks for the Order found!",
				HttpStatus.NOT_FOUND, getMessageSource().getMessage("httpStatus.notFound(2)",
				new Object[]{"Tasks", "OrderID=" + orderId}, LocaleContextHolder.getLocale()));
		}
	}
*/

}
