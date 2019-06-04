package internal.httpSecurity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

//@Component
@Slf4j
public class WorkshopAuthenticationManager implements AuthenticationManager {
	
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		log.trace("AUTHENTICATING");
//		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken();
		return null;
	}
	
}
