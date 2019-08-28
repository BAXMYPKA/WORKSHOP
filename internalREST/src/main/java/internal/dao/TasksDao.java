package internal.dao;

import internal.entities.Order;
import internal.entities.Task;
import internal.exceptions.EntityNotFoundException;
import internal.exceptions.IllegalArgumentsException;
import internal.exceptions.InternalServerErrorException;
import internal.exceptions.WorkshopException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.List;
import java.util.Optional;

@Repository
public class TasksDao extends WorkshopEntitiesDaoAbstract<Task, Long> {
	
	public TasksDao() {
		setEntityClass(Task.class);
		setKeyClass(Long.class);
	}
	
	
	/**
	 * @param pageSize Amount of Tasks to be returned at once. Min = 1
	 * @param pageNum  Zero based index.
	 * @param orderBy  Property name to order by.
	 * @param order    Ascending or Descending {@link Sort.Direction}
	 * @return "Optional.of(List<Task>)" or Optional.empty() if nothing found.
	 * @throws IllegalArgumentException     1) If pageSize or pageNum are greater or less than their Min and Max values or < 0.
	 *                                      2) If 'orderBy' is null or empty 3) If 'order' is null
	 * @throws InternalServerErrorException IllegalStateException  - if called for a Java Persistence query language UPDATE or DELETE statement
	 *                                      QueryTimeoutException - if the query execution exceeds the query timeout value set and only the statement is rolled back
	 *                                      TransactionRequiredException - if a lock mode other than NONE has been set and there is no transaction or the persistence context has not been joined to the transaction
	 *                                      PessimisticLockException - if pessimistic locking fails and the transaction is rolled back
	 *                                      LockTimeoutException - if pessimistic locking fails and only the statement is rolled back
	 *                                      PersistenceException - if the query execution exceeds the query timeout value set and the transaction is rolled back
	 */
	public Optional<List<Task>> findAllTasksByOrder(Integer pageSize,
													Integer pageNum,
													String orderBy,
													Sort.Direction order,
													Long orderId)
		throws IllegalArgumentException, InternalServerErrorException {
		
		verifyPageableValues(pageSize, pageNum, orderBy, order);
		
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Task> cq = cb.createQuery(Task.class);
		Root<Task> taskRoot = cq.from(Task.class);
		
		Join<Task, Order> taskOrderJoin = taskRoot.join("order");
		
		taskOrderJoin.on(cb.equal(taskRoot.get("order").get("identifier"), orderId));
		
		TypedQuery<Task> taskTypedQuery = entityManager.createQuery(cq);
		taskTypedQuery.setMaxResults(pageSize);
		taskTypedQuery.setFirstResult(pageSize * pageNum);
		
		try {
			List<Task> taskList = taskTypedQuery.getResultList();
			if (taskList != null && !taskList.isEmpty()) {
				return Optional.of(taskList);
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
	public Optional<List<Task>> findAllTasksAppointedToEmployee(Integer pageSize,
																Integer pageNum,
																String orderBy,
																Sort.Direction order,
																Long employeeId) {
		verifyPageableValues(pageSize, pageNum, orderBy, order);
		
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Task> cq = cb.createQuery(Task.class);
		Root<Task> taskRoot = cq.from(Task.class);
		Predicate appointedToPredicate = cb.equal(taskRoot.get("appointedTo").get("identifier"), employeeId);
		cq.where(appointedToPredicate);
		
		TypedQuery<Task> taskTypedQuery = entityManager.createQuery(cq);
		taskTypedQuery.setMaxResults(pageSize);
		taskTypedQuery.setFirstResult(pageSize * pageNum);
		
		try {
			List<Task> appointedTasks = taskTypedQuery.getResultList();
			if (appointedTasks != null && !appointedTasks.isEmpty()) {
				return Optional.of(appointedTasks);
			} else {
				return Optional.empty();
			}
		} catch (PersistenceException e) {
			throw new InternalServerErrorException(
				e.getMessage(), "httpStatus.internalServerError", HttpStatus.INTERNAL_SERVER_ERROR, e);
		}
	}
}
