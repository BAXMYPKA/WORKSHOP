package internal.dao;

import internal.entities.Employee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@Qualifier("employeesDao")
public class EmployeesDao extends WorkshopEntitiesDaoAbstract<Employee, Long> {
	
	public EmployeesDao() {
		setKeyClass(Long.class);
		setEntityClass(Employee.class);
		log.trace("EntityClass={}, KeyClass={}", this.getEntityClass().getName(), this.getKeyClass().getName());
	}
	
	/**
	 * @param email Employee's email
	 * @return Optional.of(Employee) or Optional.empty() of nothing has been found.
	 * @throws PersistenceException NonUniqueResultException - if more than one result
	 *                              IllegalStateException  - if called for a Java Persistence query language UPDATE or DELETE statement
	 *                              QueryTimeoutException - if the query execution exceeds the query timeout value set and only the statement is rolled back
	 *                              TransactionRequiredException - if a lock mode other than NONE has been set and there is no transaction or the persistence context has not been joined to the transaction
	 *                              PessimisticLockException - if pessimistic locking fails and the transaction is rolled back
	 *                              LockTimeoutException - if pessimistic locking fails and only the statement is rolled back
	 *                              PersistenceException - if the query execution exceeds the query timeout value set and the transaction is
	 *                              rolled back.
	 */
	public Optional<Employee> findEmployeeByEmail(String email) throws PersistenceException {
		if (email == null || email.isEmpty()) {
			throw new IllegalArgumentException("Email cannot be null or empty!");
		}
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
		Root<Employee> root = cq.from(Employee.class);
		Predicate emailEqual = cb.equal(root.get("email"), email);
		cq.where(emailEqual);
		TypedQuery<Employee> typedQuery = entityManager.createQuery(cq);
		try {
			Employee employee = typedQuery.getSingleResult();
			return Optional.of(employee);
		} catch (NoResultException nre) {
			return Optional.empty();
		}
	}
	
	/**
	 * @param pageSize   Amount of Employees at once
	 * @param pageNum    Zero-based pagination.
	 * @param orderBy    Non-null and non-empty property name the Employees to be ordered by
	 * @param order      Ascending or Descending
	 * @param positionId Id of the Position
	 * @return Optional.of(List Employees) or Optional.empty() if nothing found.
	 * @throws PersistenceException If some DateBase problems occurred (Locks, timeouts etc).
	 *                              IllegalStateException  - if called for a Java Persistence query language UPDATE or DELETE statement
	 *                              QueryTimeoutException - if the query execution exceeds the query timeout value set and only the statement is rolled back
	 *                              TransactionRequiredException - if a lock mode other than NONE has been set and there is no transaction or the persistence context has not been joined to the transaction
	 *                              PessimisticLockException - if pessimistic locking fails and the transaction is rolled back
	 *                              LockTimeoutException - if pessimistic locking fails and only the statement is rolled back
	 *                              PersistenceException - if the query execution exceeds the query timeout value set and the transaction is rolled back
	 */
	public Optional<List<Employee>> findAllEmployeesByPosition(Integer pageSize,
		Integer pageNum,
		String orderBy,
		Sort.Direction order,
		Long positionId)
		throws PersistenceException {
		
		verifyPageableValues(pageSize, pageNum, orderBy, order);
		pageSize = pageSize == 0 ? getPAGE_SIZE_DEFAULT() : pageSize;
		
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
		
		Root<Employee> employeeRoot = cq.from(Employee.class);
		
		Predicate positionIdEqual = cb.equal(employeeRoot.get("position").get("identifier"), positionId);
		
		cq.where(positionIdEqual);
		
		if (order.isAscending()) {
			cq.orderBy(cb.asc(employeeRoot.get(orderBy)));
		} else {
			cq.orderBy(cb.desc(employeeRoot.get(orderBy)));
		}
		
		TypedQuery<Employee> typedQuery = entityManager.createQuery(cq);
		typedQuery.setMaxResults(pageSize);
		typedQuery.setFirstResult(pageSize * pageNum);
		
		List<Employee> employeesByPosition = typedQuery.getResultList();
		
		if (employeesByPosition != null && !employeesByPosition.isEmpty()) {
			return Optional.of(employeesByPosition);
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
