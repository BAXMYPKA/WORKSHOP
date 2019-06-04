package internal.https;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class WorkshopAuthenticationManager implements AuthenticationManager {
	
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
	
//		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken();
		return null;
	}
}
