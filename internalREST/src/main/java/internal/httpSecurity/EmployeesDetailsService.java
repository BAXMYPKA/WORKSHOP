package internal.httpSecurity;

import internal.dao.DaoAbstract;
import internal.dao.EmployeesDao;
import internal.entities.Employee;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import java.util.Collections;

@Slf4j
@Setter
public class EmployeesDetailsService implements UserDetailsService {
	
	@Autowired
	@Qualifier("employeesDao")
	private EmployeesDao employeesDao;
	
	/**
	 * @param email Employee.email will be used instead username according to application specification
	 * @return UserDetails with an encoded embedded password which has to be checked by EmployeesAuthenticationProvider
	 * @throws UsernameNotFoundException used by inner SpringSecurity's checks for the authorization process
	 */
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		try {
			//TODO: to find by email
			Employee employee = employeesDao.findEntity(1L);
			UserDetails userDetails = User.builder()
				.username(employee.getEmail())
				.password(employee.getPassword())
				.authorities(employee.getPosition())
				.build();
			log.debug("User={} is found by email and passing to the AuthenticationProvider to check the password",
				userDetails.getUsername());
			
			return userDetails;
			
		} catch (PersistenceException e) {
			log.debug("Here may be any PersistenceException causing by as an EmployeeNotFound as the JPA failure", e);
			throw new UsernameNotFoundException(
				"Such an email=(" + email + ") is not found. The message from DataBase=" + e.getMessage());
		}
	}
}
