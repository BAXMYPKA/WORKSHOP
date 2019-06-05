package internal.httpSecurity;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

@Slf4j
@Setter
public class WorkshopAuthenticationManager implements AuthenticationManager {
	
	@Autowired
	private AuthenticationProvider authenticationProvider;
	
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		log.trace("Authentication manager...");
		this.authenticationProvider.authenticate(authentication);
//		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken();
		return null;
	}
}
