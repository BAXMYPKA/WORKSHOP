package internal.dao;

import internal.entities.Order;
import internal.entities.Task;
import internal.exceptions.InternalServerErrorException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;

@Repository
public class OrdersDao extends WorkshopEntitiesDaoAbstract<Order, Long> {
	
	public OrdersDao() {
		super.setEntityClass(Order.class);
		super.setKeyClass(Long.class);
	}
	
	/**
	 * @param pageSize Amount of Tasks to be returned at once. Min = 1
	 * @param pageNum  Zero based index.
	 * @param orderBy  Property name to order by.
	 * @param order    Ascending or Descending {@link Sort.Direction}
	 * @return 'Optional.of(List<Task>)' or Optional.empty() if nothing found.
	 * @throws IllegalArgumentException     1) If pageSize or pageNum are greater or less than their Min and Max values or < 0.
	 *                                      2) If 'orderBy' is null or empty 3) If 'order' is null
	 * @throws InternalServerErrorException IllegalStateException  - if called for a Java Persistence query language UPDATE or DELETE statement
	 *                                      QueryTimeoutException - if the query execution exceeds the query timeout value set and only the statement is rolled back
	 *                                      TransactionRequiredException - if a lock mode other than NONE has been set and there is no transaction or the persistence context has not been joined to the transaction
	 *                                      PessimisticLockException - if pessimistic locking fails and the transaction is rolled back
	 *                                      LockTimeoutException - if pessimistic locking fails and only the statement is rolled back
	 *                                      PersistenceException - if the query execution exceeds the query timeout value set and the transaction is rolled back
	 */
	public Optional<List<Order>> findAllOrdersModifiedByEmployee(Integer pageSize,
																Integer pageNum,
																String orderBy,
																Sort.Direction order,
																Long employeeId) {
		verifyPageableValues(pageSize, pageNum, orderBy, order);
		
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Order> cq = cb.createQuery(Order.class);
		Root<Order> orderRoot = cq.from(Order.class);
		
		Predicate modifiedByPredicate = cb.equal(orderRoot.get("modifiedBy").get("identifier"), employeeId);
		cq.where(modifiedByPredicate);
		
		TypedQuery<Order> ordersTypedQuery = entityManager.createQuery(cq);
		ordersTypedQuery.setMaxResults(pageSize);
		ordersTypedQuery.setFirstResult(pageNum * pageSize);
		
		try {
			List<Order> modifiedByOrders = ordersTypedQuery.getResultList();
			if (modifiedByOrders != null && !modifiedByOrders.isEmpty()) {
				return Optional.of(modifiedByOrders);
			} else {
				return Optional.empty();
			}
		} catch (PersistenceException e) {
			throw new InternalServerErrorException(
				  e.getMessage(), "httpStatus.internalServerError", HttpStatus.INTERNAL_SERVER_ERROR, e);
		}
	}
	
	/**
	 * @param pageSize Amount of Tasks to be returned at once. Min = 1
	 * @param pageNum  Zero based index.
	 * @param orderBy  Property name to order by.
	 * @param order    Ascending or Descending {@link Sort.Direction}
	 * @return 'Optional.of(List<Task>)' or Optional.empty() if nothing found.
	 * @throws IllegalArgumentException     1) If pageSize or pageNum are greater or less than their Min and Max values or < 0.
	 *                                      2) If 'orderBy' is null or empty 3) If 'order' is null
	 * @throws InternalServerErrorException IllegalStateException  - if called for a Java Persistence query language UPDATE or DELETE statement
	 *                                      QueryTimeoutException - if the query execution exceeds the query timeout value set and only the statement is rolled back
	 *                                      TransactionRequiredException - if a lock mode other than NONE has been set and there is no transaction or the persistence context has not been joined to the transaction
	 *                                      PessimisticLockException - if pessimistic locking fails and the transaction is rolled back
	 *                                      LockTimeoutException - if pessimistic locking fails and only the statement is rolled back
	 *                                      PersistenceException - if the query execution exceeds the query timeout value set and the transaction is rolled back
	 */
	public Optional<List<Order>> findAllOrdersCreatedByEmployee(Integer pageSize,
																 Integer pageNum,
																 String orderBy,
																 Sort.Direction order,
																 Long employeeId) {
		verifyPageableValues(pageSize, pageNum, orderBy, order);
		
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Order> cq = cb.createQuery(Order.class);
		Root<Order> orderRoot = cq.from(Order.class);
		
		Predicate createdByPredicate = cb.equal(orderRoot.get("createdBy").get("identifier"), employeeId);
		cq.where(createdByPredicate);
		
		TypedQuery<Order> ordersTypedQuery = entityManager.createQuery(cq);
		ordersTypedQuery.setMaxResults(pageSize);
		ordersTypedQuery.setFirstResult(pageNum * pageSize);
		
		try {
			List<Order> createdByOrders = ordersTypedQuery.getResultList();
			if (createdByOrders != null && !createdByOrders.isEmpty()) {
				return Optional.of(createdByOrders);
			} else {
				return Optional.empty();
			}
		} catch (PersistenceException e) {
			throw new InternalServerErrorException(
				e.getMessage(), "httpStatus.internalServerError", HttpStatus.INTERNAL_SERVER_ERROR, e);
		}
	}
	
	/**
	 * @param pageSize Amount of Tasks to be returned at once. Min = 1
	 * @param pageNum  Zero based index.
	 * @param orderBy  Property name to order by.
	 * @param order    Ascending or Descending {@link Sort.Direction}
	 * @return 'Optional.of(List<Order>)' or Optional.empty() if nothing found.
	 * @throws IllegalArgumentException     1) If pageSize or pageNum are greater or less than their Min and Max values or < 0.
	 *                                      2) If 'orderBy' is null or empty 3) If 'order' is null
	 * @throws InternalServerErrorException IllegalStateException  - if called for a Java Persistence query language UPDATE or DELETE statement
	 *                                      QueryTimeoutException - if the query execution exceeds the query timeout value set and only the statement is rolled back
	 *                                      TransactionRequiredException - if a lock mode other than NONE has been set and there is no transaction or the persistence context has not been joined to the transaction
	 *                                      PessimisticLockException - if pessimistic locking fails and the transaction is rolled back
	 *                                      LockTimeoutException - if pessimistic locking fails and only the statement is rolled back
	 *                                      PersistenceException - if the query execution exceeds the query timeout value set and the transaction is rolled back
	 */
	public Optional<List<Order>> findAllOrdersCreatedForUser(Integer pageSize,
																Integer pageNum,
																String orderBy,
																Sort.Direction order,
																Long userId) {
		verifyPageableValues(pageSize, pageNum, orderBy, order);
		
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Order> cq = cb.createQuery(Order.class);
		Root<Order> orderRoot = cq.from(Order.class);
		
		Predicate createdForPredicate = cb.equal(orderRoot.get("createdFor").get("identifier"), userId);
		cq.where(createdForPredicate);
		
		TypedQuery<Order> ordersTypedQuery = entityManager.createQuery(cq);
		ordersTypedQuery.setMaxResults(pageSize);
		ordersTypedQuery.setFirstResult(pageNum * pageSize);
		
		try {
			List<Order> createdForOrders = ordersTypedQuery.getResultList();
			if (createdForOrders != null && !createdForOrders.isEmpty()) {
				return Optional.of(createdForOrders);
			} else {
				return Optional.empty();
			}
		} catch (PersistenceException e) {
			throw new InternalServerErrorException(
				e.getMessage(), "httpStatus.internalServerError", HttpStatus.INTERNAL_SERVER_ERROR, e);
		}
	}
	
}
