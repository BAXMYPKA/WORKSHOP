package workshop.security;

import workshop.internal.dao.EmployeesDao;
import workshop.internal.entities.Employee;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.persistence.PersistenceException;

/**
 * Special class for providing the UserDetails for the Spring Security process with the help of EmployeesDao
 */
@Slf4j
@Setter
public class EmployeesDetailsService implements UserDetailsService {
	
	@Autowired
	@Qualifier("employeesDao")
	private EmployeesDao employeesDao;
	
	/**
	 * @param email Employee.email will be used instead of username according to an application specification
	 * @return UserDetailsEmployee with an encoded embedded password which has to be checked
	 * by EmployeesAuthenticationProvider. Also includes the Employee object.
	 * @throws UsernameNotFoundException used by inner SpringSecurity's checks for the authorization process
	 */
	@Override
	public UserDetailsEmployee loadUserByUsername(String email) throws UsernameNotFoundException {
		try {
			Employee employee = employeesDao.findEmployeeByEmail(email).orElseThrow(() ->
				new UsernameNotFoundException("Such an email=(" + email + ") is not found."));
/*
			//User.builder.authorities can receive String as a name for an WorkshopGrantedAuthority and cannot be null
			UserDetails userDetails = User.builder()
				.username(employee.getEmail())
				.password(employee.getPassword())
				.authorities(employee.getPosition() != null ? employee.getPosition().getName() : "")
				.build();
*/
			UserDetailsEmployee userDetailsEmployee = new UserDetailsEmployee(employee);
			
			log.debug("User={} is found by email and passing to the AuthenticationProvider to check the password",
				userDetailsEmployee.getUsername());
			
			return userDetailsEmployee;
			
		} catch (PersistenceException e) {
			log.debug("In this message may be presented any PersistenceException causing by as an EmployeeNotFound " +
				"as the JPA failure");
			throw new UsernameNotFoundException(
				"Such an email=(" + email + ") is not found. The message from DataBase=" + e.getMessage());
		}
	}
}
