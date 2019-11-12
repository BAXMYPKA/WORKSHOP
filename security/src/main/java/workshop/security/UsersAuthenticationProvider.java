package workshop.security;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import workshop.exceptions.UuidAuthenticationException;
import workshop.internal.entities.Uuid;
import workshop.exceptions.EntityNotFoundException;
import workshop.internal.services.UuidsService;

import java.util.Objects;

@Slf4j
@Setter
public class UsersAuthenticationProvider implements AuthenticationProvider {
	
	@Autowired
	@Qualifier("usersDetailsService")
	private UsersDetailsService usersDetailsService;
	@Autowired
	private UuidsService uuidsService;
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Override
	public Authentication authenticate(Authentication authenticationToken) throws AuthenticationException {
		if (authenticationToken == null || authenticationToken.getPrincipal() == null ||
			authenticationToken.getPrincipal().toString().isEmpty()) {
			throw new BadCredentialsException("Authentication or Principal cannot be null or empty!");
		}
		if (UsernamePasswordUuidAuthenticationToken.class.isAssignableFrom(authenticationToken.getClass())) {
			authenticateUuid((UsernamePasswordUuidAuthenticationToken) authenticationToken);
		}
		log.trace("Provide authentication...");
		
		UserDetailsUser user =
			usersDetailsService.loadUserByUsername(authenticationToken.getPrincipal().toString());
		
		log.debug("User={} is found.", user.getUsername());
		
		isUserEnabled(user);
		
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
	
	void authenticateUuid(UsernamePasswordUuidAuthenticationToken uuidAuthenticationToken) throws AuthenticationException {
		if (uuidAuthenticationToken.getUuid() == null ||
			uuidAuthenticationToken.getUuid().isEmpty()) {
			throw new BadCredentialsException("Neither Authentication nor Principal nor Uuid cannot be null or empty!");
		}
		log.trace("Provide authentication with UUID...");
		try {
			Uuid uuid = uuidsService.findByProperty("uuid", uuidAuthenticationToken.getUuid()).get(0);
			log.debug("Uuid={} is found.", uuid.getUuid());
			if (!uuid.getUser().getEmail().equals(uuidAuthenticationToken.getPrincipal())) {
				throw new UuidAuthenticationException(
					"The given User="+uuidAuthenticationToken.getName()+" doesn't math the given UUID="+uuid.getUuid()+" !");
			}
		} catch (EntityNotFoundException e) {
			throw new workshop.exceptions.UuidAuthenticationException("The given UUID cannot be found in the DataBase!", e);
		}
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
		
		isUserEnabled(userDetailsUser);
		
		UsernamePasswordAuthenticationToken authenticatedToken =
			new UsernamePasswordAuthenticationToken(userDetailsUser, "", userDetailsUser.getAuthorities());
		return authenticatedToken;
	}
	
	private void isUserEnabled(UserDetailsUser user) {
		if (!Objects.requireNonNull(user).isEnabled()) {
			throw new InsufficientAuthenticationException("User " + user.getUser() + " is not enabled!");
		}
	}
}
