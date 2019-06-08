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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Slf4j
@Setter
public class EmployeesAuthenticationProvider implements AuthenticationProvider {
	
	@Autowired
	@Qualifier("employeesDetailsService")
	private UserDetailsService employeesDetailsService;
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	/**
	 * User is ready to use implementation of the UserDetails
	 * UsernamePasswordAuthenticationToken.getPrincipal() returns a username String
	 * UsernamePasswordAuthenticationToken.getCredentials() returns a password String
	 *
	 * @param authenticationToken Only a UsernameAuthenticationToken with a raw (non encrypted) username and password
	 * @return Fully verified (by email & password) and authenticated UsernamePasswordAuthenticationToken
	 * @throws AuthenticationException in case of authentication failure
	 */
	@Override
	public Authentication authenticate(Authentication authenticationToken) throws AuthenticationException {
		log.trace("Provide authentication...");
		
		User user = (User) employeesDetailsService.loadUserByUsername(authenticationToken.getPrincipal().toString());
		
		log.debug("Employee={} is found. Proceeding with matching passwords...", user.getUsername());
		
		//The raw password must match an encoded one from the Employee with that email
		if (!passwordEncoder.matches((String) authenticationToken.getCredentials(), user.getPassword())) {
			throw new BadCredentialsException("Username or Password is incorrect!");
		}
		
		return new UsernamePasswordAuthenticationToken(user.getUsername(), "", user.getAuthorities());
	}
	
	@Override
	public boolean supports(Class<?> authentication) {
		log.trace("Check is Authentication.class supported...");
		return authentication.isInstance(Authentication.class);
	}
}
