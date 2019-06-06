package internal.httpSecurity;

import lombok.Getter;
import lombok.Setter;
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
@Getter
@Setter
public class WorkshopAuthenticationManager implements AuthenticationManager {
	
	@Autowired
	private Set<AuthenticationProvider> internalAuthenticationProviders;
	
	/**
	 * @param authenticationToken Only a UsernameAuthenticationToken with a raw (non encrypted) username and password
	 * @return fully completed Authentication object with all credentials
	 * @throws AuthenticationException may be received from AuthenticatedProvider in case of an authentication failure
	 */
	@Override
	public Authentication authenticate(Authentication authenticationToken) throws AuthenticationException {
		log.trace("Receiving authenticationToken={}", authenticationToken.getName());
		Authentication authentication;
		for (AuthenticationProvider authProvider : internalAuthenticationProviders) {
			authentication = authProvider.authenticate(authenticationToken);
			if (authentication != null) {
				return authentication;
			}
		}
		throw new UsernameNotFoundException(
			"No such a username or a password has been found in any AuthenticationProvider!");
	}
	
/*
	public void addAuthenticationProvider(AuthenticationProvider authenticationProvider) {
		this.internalAuthenticationProviders.add(authenticationProvider);
	}
*/
}
