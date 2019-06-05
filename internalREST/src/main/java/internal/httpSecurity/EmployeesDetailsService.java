package internal.httpSecurity;

import internal.dao.DaoAbstract;
import internal.dao.EmployeesDao;
import internal.entities.Employee;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collections;

@Slf4j
@Setter
public class EmployeesDetailsService implements UserDetailsService{
	
	@Autowired
	private EmployeesDao employeesDao;
	
	/**
	 * @param email Employee.email will be used instead username according to application specification
	 * @return User implements UserDetails (org.springframework.security.core.userdetails.User)
	 * @throws UsernameNotFoundException
	 */
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		Employee employee = employeesDao.findEntity(1L);//Will find by email
		log.error(employee.toString());
		User user = new User(employee.getEmail(), employee.getPassword(), Collections.singletonList(employee.getPosition()));
		return user;
	}
}
