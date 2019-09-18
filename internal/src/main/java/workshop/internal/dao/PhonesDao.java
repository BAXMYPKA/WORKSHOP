package workshop.internal.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import workshop.internal.entities.Employee;
import workshop.internal.entities.Phone;
import workshop.internal.entities.User;
import workshop.internal.exceptions.InternalServerErrorException;

import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class PhonesDao extends WorkshopEntitiesDaoAbstract<Phone, Long> {
	
	public PhonesDao() {
		super.setEntityClass(Phone.class);
		super.setKeyClass(Long.class);
	}
	
	/**
	 * @param pageSize Amount of Tasks to be returned at once. Min = 1
	 * @param pageNum  Zero based index.
	 * @param orderBy  Property name to order by.
	 * @param order    Ascending or Descending {@link Sort.Direction}
	 * @return 'Optional.of(List<Phone>)' or Optional.empty() if nothing found.
	 * @throws IllegalArgumentException     1) If pageSize or pageNum are greater or less than their Min and Max values or < 0.
	 *                                      2) If 'orderBy' is null or empty 3) If 'order' is null
	 * @throws InternalServerErrorException IllegalStateException  - if called for a Java Persistence query language UPDATE or DELETE statement
	 *                                      QueryTimeoutException - if the query execution exceeds the query timeout value set and only the statement is rolled back
	 *                                      TransactionRequiredException - if a lock mode other than NONE has been set and there is no transaction or the persistence context has not been joined to the transaction
	 *                                      PessimisticLockException - if pessimistic locking fails and the transaction is rolled back
	 *                                      LockTimeoutException - if pessimistic locking fails and only the statement is rolled back
	 *                                      PersistenceException - if the query execution exceeds the query timeout value set and the transaction is rolled back
	 */
	public Optional<List<Phone>> findAllPhonesByUser(Integer pageSize,
		Integer pageNum,
		String orderBy,
		Sort.Direction order,
		Long userId) throws PersistenceException {
		verifyPageableValues(pageSize, pageNum, orderBy, order);
		pageSize = pageSize == 0 ? getPAGE_SIZE_DEFAULT() : pageSize;
		
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Phone> cq = cb.createQuery(Phone.class);
		
		Root<Phone> phoneRoot = cq.from(Phone.class);
		
		Join<Phone, User> joinPhoneUser = phoneRoot.join("user", JoinType.INNER);
		Predicate userIdEqual = cb.equal(joinPhoneUser.get("identifier"), userId);
		joinPhoneUser.on(userIdEqual);
		
		if (order.isAscending()) {
			cq.orderBy(cb.asc(phoneRoot.get(orderBy)));
		} else {
			cq.orderBy(cb.desc(phoneRoot.get(orderBy)));
		}
		TypedQuery<Phone> typedQuery = entityManager.createQuery(cq);
		typedQuery.setMaxResults(pageSize);
		typedQuery.setFirstResult(pageSize * pageNum);
		
		List<Phone> phonesByEmployee = typedQuery.getResultList();
		
		if (phonesByEmployee != null && !phonesByEmployee.isEmpty()) {
			return Optional.of(phonesByEmployee);
		} else {
			return Optional.empty();
		}
	}
	
	/**
	 * @param pageSize   Amount of Employees at once
	 * @param pageNum    Zero-based pagination.
	 * @param orderBy    Non-null and non-empty property name the Employees to be ordered by
	 * @param order      Ascending or Descending
	 * @param employeeId Id of the Position
	 * @return Optional.of(List Classifiers) or Optional.empty() if nothing found.
	 * @throws PersistenceException If some DateBase problems occurred (Locks, timeouts etc).
	 *                              IllegalStateException  - if called for a Java Persistence query language UPDATE or DELETE statement
	 *                              QueryTimeoutException - if the query execution exceeds the query timeout value set and only the statement is rolled back
	 *                              TransactionRequiredException - if a lock mode other than NONE has been set and there is no transaction or the persistence context has not been joined to the transaction
	 *                              PessimisticLockException - if pessimistic locking fails and the transaction is rolled back
	 *                              LockTimeoutException - if pessimistic locking fails and only the statement is rolled back
	 *                              PersistenceException - if the query execution exceeds the query timeout value set and the transaction is rolled back
	 */
	public Optional<List<Phone>> findAllPhonesByEmployee(Integer pageSize,
		Integer pageNum,
		String orderBy,
		Sort.Direction order,
		Long employeeId) throws PersistenceException {
		
		verifyPageableValues(pageSize, pageNum, orderBy, order);
		pageSize = pageSize == 0 ? getPAGE_SIZE_DEFAULT() : pageSize;
		
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Phone> cq = cb.createQuery(Phone.class);
		
		Root<Phone> phoneRoot = cq.from(Phone.class);
		
		Join<Phone, Employee> joinPhoneEmployee = phoneRoot.join("employee", JoinType.INNER);
		Predicate employeeIdEqual = cb.equal(joinPhoneEmployee.get("identifier"), employeeId);
		joinPhoneEmployee.on(employeeIdEqual);
		
		if (order.isAscending()) {
			cq.orderBy(cb.asc(phoneRoot.get(orderBy)));
		} else {
			cq.orderBy(cb.desc(phoneRoot.get(orderBy)));
		}
		TypedQuery<Phone> typedQuery = entityManager.createQuery(cq);
		typedQuery.setMaxResults(pageSize);
		typedQuery.setFirstResult(pageSize * pageNum);
		
		List<Phone> phonesByEmployee = typedQuery.getResultList();
		
		if (phonesByEmployee != null && !phonesByEmployee.isEmpty()) {
			return Optional.of(phonesByEmployee);
		} else {
			return Optional.empty();
		}
	}
	
	/**
	 * @param employeeId Existed Employee.ID to set Phone to.
	 * @param phoneId    Existed Phone.ID to be set a new owner
	 * @return Optional.(Employee) with a new Phone set or Optional.empty() if some of the given IDs is wrong.
	 * @throws PersistenceException If some of IDs is wrong.
	 */
	public Optional<Employee> addPhoneToEmployee(Long employeeId, Long phoneId) throws PersistenceException {
		Query query = entityManager.createQuery("UPDATE Phone p SET p.employee.id = :employeeId WHERE p.id = :phoneId");
		query.setParameter("phoneId", phoneId);
		query.setParameter("employeeId", employeeId);
		try {
			query.executeUpdate();
		} catch (PersistenceException e) {
			log.info(e.getMessage(), e);
			return Optional.empty();
		}
		return Optional.of(entityManager.find(Employee.class, employeeId));
	}
}
