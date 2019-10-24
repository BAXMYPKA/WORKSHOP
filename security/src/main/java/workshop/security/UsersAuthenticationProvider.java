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
		
		return new UsernamePasswordAuthenticationToken(user, "", user.getAuthorities());
	}
	
	@Override
	public boolean supports(Class<?> aClass) {
		return false;
	}
}
