package internal.dao;

import internal.entities.Employee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

@Slf4j
@Repository
@Qualifier("employeesDao")
public class EmployeesDao extends EntitiesDaoAbstract<Employee, Long> {
	
	public EmployeesDao() {
		setKeyClass(Long.class);
		setEntityClass(Employee.class);
		log.trace("EntityClass={}, KeyClass={}", this.getEntityClass().getName(), this.getKeyClass().getName());
	}
	
	public Employee findEmployeeByEmail(String email) throws PersistenceException {
		if (email == null || email.isEmpty()) {
			throw new IllegalArgumentException("Email cannot be null or empty!");
		}
		//Here is the simple practicing in the Criteria API writings
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Employee> cq = cb.createQuery(Employee.class);
		Root<Employee> root = cq.from(Employee.class);
		Predicate emailEqual = cb.equal(root.get("email"), email);
		cq.where(emailEqual);
		TypedQuery<Employee> typedQuery = entityManager.createQuery(cq);
		Employee employee = typedQuery.getSingleResult();
		return employee;
	}
}
