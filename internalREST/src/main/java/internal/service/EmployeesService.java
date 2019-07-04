package internal.service;

import internal.dao.EmployeesDao;
import internal.entities.Employee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import java.util.Optional;

@Slf4j
@Service
public class EmployeesService {
	
	@Autowired
	EmployeesDao employeesDao;
	
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public Optional<Employee> findByEmail(String email) throws IllegalArgumentException {
		if (email == null || email.isEmpty()) {
			throw new IllegalArgumentException("Email cannot be null or empty!");
		}
		try {
			Employee employee = employeesDao.findEmployeeByEmail(email);
			return Optional.of(employee);
		} catch (NoResultException e) {
			return Optional.empty();
		} catch (PersistenceException ex) {
			//TODO: ?
			return Optional.empty();
		}
	}
}
