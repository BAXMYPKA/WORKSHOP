package internal.dao;

import internal.entities.Employee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@Qualifier("employeesDao")
public class EmployeesDao extends DaoAbstract<Employee, Long> {
	
	public EmployeesDao(){
		setKeyClass(Long.class);
		setEntityClass(Employee.class);
		log.trace("EntityClass={}, KeyClass={}", this.getEntityClass().getName(), this.getKeyClass().getName());
	}
}
