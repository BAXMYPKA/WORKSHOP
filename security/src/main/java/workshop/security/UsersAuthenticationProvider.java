package workshop.security;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Slf4j
@Setter
public class UsersAuthenticationProvider implements AuthenticationProvider {
	
	@Autowired
	@Qualifier("usersDetailsService")
	private UsersDetailsService usersDetailsService;
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Override
	public Authentication authenticate(Authentication authenticationToken) throws AuthenticationException {
		if (authenticationToken == null || authenticationToken.getPrincipal() == null ||
			authenticationToken.getPrincipal().toString().isEmpty()) {
			throw new BadCredentialsException("Authentication or Principal cannot be null or empty!");
		}
		log.trace("Provide authentication...");
		
		UserDetailsUser user =
			usersDetailsService.loadUserByUsername(authenticationToken.getPrincipal().toString());
		
		log.debug("User={} is found. Proceeding with matching passwords...", user.getUsername());
		
		//The raw password must match an encoded one from the Employee with that email
		if (!passwordEncoder.matches((String) authenticationToken.getCredentials(), user.getPassword())) {
			throw new BadCredentialsException("Username or Password is incorrect!");
		}
		
		UsernamePasswordAuthenticationToken authenticatedToken =
		 new UsernamePasswordAuthenticationToken(user, "", user.getAuthorities());
		authenticatedToken.setAuthenticated(true);
		return authenticatedToken;
	}
	
	/**
	 * Just returns the Authentication with email and Authorities.
	 * Use this method when prerequisites (JWT or something else) are valid and checked!
	 * Also this method has to be supported by all the further implementations of custom Authentication providers
	 */
	public Authentication authenticateByEmail(String userEmail) {
		if (userEmail == null || userEmail.isEmpty()) {
			throw new BadCredentialsException("User's email cannot be null or empty!");
		}
		log.trace("Trying to find User by email {}", userEmail);
		UserDetailsUser userDetailsUser = usersDetailsService.loadUserByUsername(userEmail);
		log.trace("User={} is found", userDetailsUser.getUsername());
		UsernamePasswordAuthenticationToken authenticatedToken =
		new  UsernamePasswordAuthenticationToken(userDetailsUser, "", userDetailsUser.getAuthorities());
		return authenticatedToken;
	}
	
	@Override
	public boolean supports(Class<?> aClass) {
		return false;
	}
}
