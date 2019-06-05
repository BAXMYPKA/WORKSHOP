package internal.httpSecurity;

import internal.dao.DaoAbstract;
import internal.dao.EmployeesDao;
import internal.entities.Employee;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Slf4j
@Setter
public class WorkshopAuthenticationProvider implements AuthenticationProvider {
	
	@Autowired
	private EmployeesDao employeesDao;
	
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		log.trace("Provide authentication...");
		Employee entity = employeesDao.findEntity(1L);
		log.error("Found email={}", entity.getEmail());
		if (true){
			throw new UsernameNotFoundException("NOT FOUND");
		}
		return null;
	}
	
	@Override
	public boolean supports(Class<?> authentication) {
		log.trace("Check is Authentication.class supported...");
		return authentication.isInstance(Authentication.class);
	}
}
