package internal.httpSecurity;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

@Slf4j
@Setter
public class EmployeesAuthenticationProvider implements AuthenticationProvider {
	
	@Autowired
	@Qualifier("employeesDetailsService")
	private UserDetailsService employeesDetailsService;
	
	/**
	 * User is ready to use implementation of the UserDetails
	 * UsernamePasswordAuthenticationToken.getPrincipal() returns a username String
	 * UsernamePasswordAuthenticationToken.getCredentials() returns a password String
	 *
	 * @param authenticationToken Only a UsernameAuthenticationToken with a raw (non encrypted) username and password
	 * @return
	 * @throws AuthenticationException
	 */
	@Override
	public Authentication authenticate(Authentication authenticationToken) throws AuthenticationException {
		log.trace("Provide authentication...");
		User user = (User) employeesDetailsService.loadUserByUsername(authenticationToken.getPrincipal().toString());
		log.debug("Employee={} is found. Proceeding with checking password...", user.getUsername());
		//TODO: to implement passwords BCrypt checker
		if (user.getPassword() != authenticationToken.getCredentials().toString()){
			throw new BadCredentialsException("Username or Password is incorrect!");
		}
		
		//Authentication.getAuthorities is nullable so it is safe if an User has null GrantedAuthority (Position.class)
		Authentication authentication = new UsernamePasswordAuthenticationToken(
			user.getUsername(), "", user.getAuthorities());
		
		if (user.getAuthorities() == null || user.getAuthorities().isEmpty()) {
			authentication.setAuthenticated(false);
		} else {
			authentication.setAuthenticated(true);
		}
		return authentication;
	}
	
	@Override
	public boolean supports(Class<?> authentication) {
		log.trace("Check is Authentication.class supported...");
		return authentication.isInstance(Authentication.class);
	}
}
