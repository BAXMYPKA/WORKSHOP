package workshop.security;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Set;

@Slf4j
@Getter
@Setter
public class WorkshopAuthenticationManager implements AuthenticationManager {
	
	private Set<AuthenticationProvider> workshopAuthenticationProviders;
	
	@Autowired
	private EmployeesAuthenticationProvider employeesAuthenticationProvider;
	
	@Autowired
	private UsersAuthenticationProvider usersAuthenticationProvider;
	
	@Autowired
	public WorkshopAuthenticationManager(Set<AuthenticationProvider> workshopAuthenticationProviders) {
		this.workshopAuthenticationProviders = workshopAuthenticationProviders;
	}
	
	/**
	 * @param authenticationToken Only a UsernameAuthenticationToken with a raw (non encrypted) username and password
	 * @return fully completed Authentication object with all credentials
	 * @throws AuthenticationException may be received from AuthenticatedProvider in case of an authenticationToken failure
	 */
	@Override
	public Authentication authenticate(Authentication authenticationToken) throws AuthenticationException {
		log.trace("Receiving authenticationToken={}", authenticationToken.getName());
		Authentication authentication;
		for (AuthenticationProvider authProvider : workshopAuthenticationProviders) {
			try {
				authentication = authProvider.authenticate(authenticationToken);
			} catch (AuthenticationException e) {
				continue;
			}
			if (authentication != null) {
				return authentication;
			}
		}
		throw new UsernameNotFoundException(
			"No such a username or a password has been found in any AuthenticationProvider!");
	}
	
	public Authentication getAuthenticationByEmail(String email) {
		Authentication authentication = null;
		try {
			authentication = employeesAuthenticationProvider.authenticateByEmail(email);
		} catch (AuthenticationException e) {
			log.debug("No Employee found for email={}", email);
			try {
				authentication = usersAuthenticationProvider.authenticateByEmail(email);
			} catch (AuthenticationException ae) {
				log.debug("No User found for email={}", email);
			}
		}
		if (authentication != null) {
			return authentication;
		} else {
			throw new UsernameNotFoundException(
				"No such a username or a password has been found in any AuthenticationProvider!");
		}
	}
}
