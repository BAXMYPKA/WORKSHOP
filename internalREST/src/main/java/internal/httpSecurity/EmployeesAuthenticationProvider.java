package internal.httpSecurity;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.persistence.NoResultException;

@Slf4j
@Setter
public class EmployeesAuthenticationProvider implements AuthenticationProvider {
	
	@Autowired
	@Qualifier("employeesDetailsService")
	private UserDetailsService employeesDetailsService;
	
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		log.trace("Provide authentication...");
		try {
			
			User user = (User) employeesDetailsService.loadUserByUsername("");
			log.error("Found employee={}", user.getUsername());
		} catch (NoResultException e) {
			log.error(e.getMessage());
		}
		if (true) {
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
