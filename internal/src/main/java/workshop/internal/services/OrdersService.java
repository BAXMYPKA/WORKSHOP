package workshop.internal.services;

import workshop.internal.dao.OrdersDao;
import workshop.internal.entities.Order;
import workshop.internal.entities.User;
import workshop.internal.exceptions.EntityNotFoundException;
import workshop.internal.exceptions.IllegalArgumentsException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class OrdersService extends WorkshopEntitiesServiceAbstract<Order> {
	
	@Getter
	@Setter
	@Autowired
	private OrdersDao ordersDao;
	
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
	
	/**
	 * @param employeeId Self-description.
	 * @return 'Page<Task>' with included List of Tasks and pageable info for constructing pages.
	 * @throws IllegalArgumentsException If the given 'orderID' is null, zero or below.
	 * @throws EntityNotFoundException   If no Order or Tasks from if were found.
	 */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE, readOnly = true)
	public Page<Order> findAllOrdersModifiedByEmployee(Pageable pageable, Long employeeId)
		  throws IllegalArgumentsException, EntityNotFoundException {
		
		super.verifyIdForNullZeroBelowZero(employeeId);
		Pageable verifiedPageable = super.getVerifiedAndCorrectedPageable(pageable);
		
		String orderBy = verifiedPageable.getSort().iterator().next().getProperty();
		Sort.Direction order = verifiedPageable.getSort().getOrderFor(orderBy).getDirection();
		
		Optional<List<Order>> allOrdersModifiedByEmployee = ordersDao.findAllOrdersModifiedByEmployee(
			  verifiedPageable.getPageSize(),
			  verifiedPageable.getPageNumber(),
			  orderBy,
			  order,
			  employeeId);
		
		Page<Order> verifiedEntitiesPageFromDao =
			  super.getVerifiedEntitiesPage(verifiedPageable, allOrdersModifiedByEmployee);
		
		return verifiedEntitiesPageFromDao;
	}
	
	/**
	 * @param employeeId Self-description.
	 * @return 'Page<Task>' with included List of Tasks and pageable info for constructing pages.
	 * @throws IllegalArgumentsException If the given 'orderID' is null, zero or below.
	 * @throws EntityNotFoundException   If no Order or Tasks from if were found.
	 */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE, readOnly = true)
	public Page<Order> findAllOrdersCreatedByEmployee(Pageable pageable, Long employeeId)
		throws IllegalArgumentsException, EntityNotFoundException {
		
		super.verifyIdForNullZeroBelowZero(employeeId);
		Pageable verifiedPageable = super.getVerifiedAndCorrectedPageable(pageable);
		
		String orderBy = verifiedPageable.getSort().iterator().next().getProperty();
		Sort.Direction order = verifiedPageable.getSort().getOrderFor(orderBy).getDirection();
		
		Optional<List<Order>> allOrdersCreatedByEmployee = ordersDao.findAllOrdersCreatedByEmployee(
			verifiedPageable.getPageSize(),
			verifiedPageable.getPageNumber(),
			orderBy,
			order,
			employeeId);
		
		Page<Order> verifiedEntitiesPageFromDao =
			super.getVerifiedEntitiesPage(verifiedPageable, allOrdersCreatedByEmployee);
		
		return verifiedEntitiesPageFromDao;
	}
	
	/**
	 * @param userId Self-description.
	 * @return 'Page<Task>' with included List of Tasks and pageable info for constructing pages.
	 * @throws IllegalArgumentsException If the given 'orderID' is null, zero or below.
	 * @throws EntityNotFoundException   If no Order or Tasks from if were found.
	 */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE, readOnly = true)
	public Page<Order> findAllOrdersCreatedForUser(Pageable pageable, Long userId)
		throws IllegalArgumentsException, EntityNotFoundException {
		
		super.verifyIdForNullZeroBelowZero(userId);
		Pageable verifiedPageable = super.getVerifiedAndCorrectedPageable(pageable);
		
		String orderBy = verifiedPageable.getSort().iterator().next().getProperty();
		Sort.Direction order = verifiedPageable.getSort().getOrderFor(orderBy).getDirection();
		
		Optional<List<Order>> allOrdersCreatedForUser = ordersDao.findAllOrdersCreatedForUser(
			verifiedPageable.getPageSize(),
			verifiedPageable.getPageNumber(),
			orderBy,
			order,
			userId);
		
		Page<Order> verifiedEntitiesPageFromDao =
			super.getVerifiedEntitiesPage(verifiedPageable, allOrdersCreatedForUser);
		
		return verifiedEntitiesPageFromDao;
	}

}
