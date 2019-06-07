package internal.httpSecurity;

import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Extracts only Username and raw Password from a given JwtToken out of the "Authentication" HTTP header with the help of the JWT decoder.
 * Then pass them with the UsernamePasswordAuthenticationToken to the AuthenticationManager for the further check.
 * All the possible AuthenticationExceptions thrown by involved following classes in this
 * Authentication chain will be caught in the 'doFilter' method to fail the authentication
 */
@Slf4j
@Setter
@NoArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
	
	/**
	 * If Authentication won't be put into SecurityContext following Authorization process will be failed.
	 * All the possible AuthenticationExceptions thrown by involved following classes in this
	 * Authentication chain will be caught in this method as the signal of the authentication failure
	 */
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		//TODO: to implement OAuth2 JWT tokens attached by cookie. Also implement CSRF protection
		log.trace("doFilter...");
		try {
			Authentication authentication = attemptAuthentication((HttpServletRequest) req, (HttpServletResponse) res);
			SecurityContextHolder.getContext().setAuthentication(authentication);
			log.debug("Authentication={} is successfully set into the SecurityContext", authentication.getName());
			super.doFilter(req, res, chain);
		} catch (AuthenticationException e) {
			log.debug(e.getMessage());
			super.doFilter(req, res, chain);
		}
	}
	
	/**
	 * UsernamePasswordAuthenticationToken.getPrincipal() returns a username String
	 * UsernamePasswordAuthenticationToken.getCredentials() returns a password String
	 */
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
		log.trace("Authentication filter attempt...");
		//TODO: if the Authentication header with credentials is not presented skip auth process up to super.doFilter
//		String jwt = request.getHeader("Authentication");
//		if (jwt == null || jwt.isEmpty()) {
//			throw new AuthenticationCredentialsNotFoundException("Authentication HTTP header is not presented!");
//		}
//		String[] usernamePassword = obtainUsernameWithPassword(jwt);
		String[] usernamePassword = obtainUsernameWithPassword(""); //to delete this string of code
		UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
			usernamePassword[0], usernamePassword[1]);
		authenticationToken.setAuthenticated(false);
		Authentication authentication = getAuthenticationManager().authenticate(authenticationToken);
		// Allow subclasses to set the "details" property (given from the superclass)
		setDetails(request, authenticationToken);
		return authentication;
	}
	
	private String[] obtainUsernameWithPassword(String jwt) throws BadCredentialsException {
		//returns String[]{"username", "password"}
		return new String[]{"administrator@workshop.pro", "12345"};
	}
}
