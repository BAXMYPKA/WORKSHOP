package internal.httpSecurity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Slf4j
public class WorkshopAuthenticationManager implements AuthenticationManager {
	
	private Set<AuthenticationProvider> authenticationProviders = new HashSet<>(3);
	
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		log.trace("AUTHENTICATING");
//		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken();
		throw new UsernameNotFoundException("");
	}
	
	public void addAuthenticationProvider(AuthenticationProvider authenticationProvider) {
		this.authenticationProviders.add(authenticationProvider);
	}
}
