package workshop.security;

import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import workshop.exceptions.UuidAuthenticationException;
import workshop.http.CookieUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Superclass in its constructor uses "super(new AntPathRequestMatcher("/login", "POST"))" to set the path to trigger
 */
@Slf4j
@Setter
@NoArgsConstructor
public class LoginAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
	
	@Setter
	private JwtUtils jwtUtils;
	
	@Setter
	private CookieUtils cookieUtils;
	
	@Setter
	private String authenticationCookieName;
	
	/**
	 * If Authentication won't be put into SecurityContext following Authorization process will be failed.
	 * All the possible AuthenticationExceptions thrown by involved following classes in this
	 * Authentication chain will be caught in this method as the signal of the authentication failure
	 */
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		
		if (!requiresAuthentication(request, response)) {
			chain.doFilter(request, response);
			return;
		}
		log.trace("Login doFilter...");
		try {
			Authentication authentication = attemptAuthentication(request, response);
			log.debug("Authentication={} with Authorities={} is received for being set into SecurityContext",
				authentication.getName(), authentication.getAuthorities());
			successfulAuthentication(request, response, chain, authentication);
		} catch (UuidAuthenticationException e) { //The caught POST loginAuthentication request with wrong UUID
			log.debug(e.getMessage(), e);
			if (e.getValidUuid() != null) { //Informs User to reenter wrong credentials with this valid UUID
				request.setAttribute("uuid", e.getValidUuid().getUuid());
				response.sendRedirect("login?uuid="+e.getValidUuid().getUuid());
			} else {
				request.setAttribute("uuid", "notValid");
				response.sendRedirect("login?uuid=notValid");
			}
		} catch (AuthenticationException e) { //Other types of not valid credentials
			log.debug(e.getMessage(), e);
			super.unsuccessfulAuthentication(request, response, e);
		} catch (Exception e) {
			log.warn("There is a possible critical error occurred: ", e);
			super.unsuccessfulAuthentication(request, response, new AuthenticationServiceException(e.getMessage()));
		}
	}
	
	/**
	 * The method is an approximate copy of same from the superclass
	 * Except inserting a Cookie with a valid JWT into the response.
	 */
	@Override
	protected void successfulAuthentication(
		HttpServletRequest request,	HttpServletResponse response, FilterChain chain, Authentication authResult)
		throws IOException, ServletException {
		log.debug("Authentication success. Setting JWT into request and updating SecurityContextHolder to contain: {}",
			authResult);
		
		SecurityContextHolder.getContext().setAuthentication(authResult);
		
		getRememberMeServices().loginSuccess(request, response, authResult);
		
		// Fire event
		if (this.eventPublisher != null) {
			eventPublisher.publishEvent(new InteractiveAuthenticationSuccessEvent(
				authResult, this.getClass()));
		}
		String jwt = jwtUtils.generateJwt(authResult);
		cookieUtils.addCookieToResponse(response, authenticationCookieName, jwt, null);
		
		getSuccessHandler().onAuthenticationSuccess(request, response, authResult);
	}
	
	/**
	 * UsernamePasswordAuthenticationToken.getPrincipal() returns a username String
	 * UsernamePasswordAuthenticationToken.getCredentials() returns a password String
	 */
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
		log.trace("Extraction email & password from header...");
		
		String email = request.getParameter("email");
		String password = request.getParameter("password");
		String uuid = request.getParameter("uuid");
		
		if ((email == null || password == null) || (email.isEmpty() && password.isEmpty())) {
			throw new BadCredentialsException("Email or password is null or empty!");
		}
		UsernamePasswordAuthenticationToken authenticationToken;
		if (uuid != null && !uuid.isEmpty()) {
			authenticationToken = new UsernamePasswordUuidAuthenticationToken(email, password, uuid);
		} else {
			authenticationToken = new UsernamePasswordAuthenticationToken(email, password);
		}
		Authentication authentication = getAuthenticationManager().authenticate(authenticationToken);
		// Allow subclasses to set the "details" property (given from the superclass)
		setDetails(request, authenticationToken);
		return authentication;
	}
}
