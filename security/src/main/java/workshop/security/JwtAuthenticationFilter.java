package workshop.security;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import workshop.http.CookieUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

/**
 * Now works with cookies only.
 * Inspects every incoming HttpServletRequest on the JwtAuthentication Cookie presence. If yes, attempts to
 * authenticate
 */
@Slf4j
//@Component
public class JwtAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
	
	@Setter
	@Autowired
	private JwtUtils jwtUtils;
	
//	@Autowired
//	@Setter
//	private WorkshopAuthenticationManager workshopAuthenticationManager;
	
	@Getter
	@Setter
	@Autowired
	private CookieUtils cookieUtils;
	
	@Value("${internalAuthCookieName}")
	@Setter
	private String authenticationCookieName;
	
	public JwtAuthenticationFilter(String defaultFilterProcessesUrl) {
		super(defaultFilterProcessesUrl);
	}
	
	public JwtAuthenticationFilter(RequestMatcher requiresAuthenticationRequestMatcher) {
		super(requiresAuthenticationRequestMatcher);
	}
	
/*
	public JwtAuthenticationFilter(RequestMatcher requiresAuthenticationRequestMatcher, JwtUtils jwtUtils) {
		super(requiresAuthenticationRequestMatcher);
		this.jwtUtils = jwtUtils;
	}
*/
	
	/**
	 * Checks incoming cookies for authentication.
	 *
	 * @throws IOException
	 * @throws ServletException
	 */
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		
		if (!requiresAuthentication(request, response)) { //Super method depending on path matcher
			chain.doFilter(request, response);
			return;
		} else if (request.getCookies() == null || Arrays.stream(request.getCookies())
			.noneMatch(cookie -> authenticationCookieName.equals(cookie.getName()))) {
			log.trace("No {} cooke present", authenticationCookieName);
			//Continue filtering in case the Authentication Cookie is not presented
			chain.doFilter(request, response);
			return;
		}
		//TODO: Also implement CSRF protection
		log.trace("doFilter: Extracting JWT from HTTP request...");
		
		Authentication authResult;
		try {
			authResult = attemptAuthentication(request, response);
			if (authResult == null) {
				// return immediately as subclass has indicated that it hasn't completed
				// authentication
				return;
			}
			successfulAuthentication(request, response, chain, authResult);
		} catch (InternalAuthenticationServiceException failed) {
			log.error("An internal error occurred while trying to authenticate the user.", failed);
			unsuccessfulAuthentication(request, response, failed);
		} catch (AuthenticationException | IllegalArgumentException failed) {
			unsuccessfulAuthentication(request, response, new BadCredentialsException(failed.getMessage()));
		}
	}
	
	/**
	 * 1) Gets the JWT from a Cookie
	 * 2) Checks if the JWT is valid
	 * 3) Derives the Email
	 * 4) Obtains the Authentication with the Authorities
	 * 5) Pass the Authentication back to be set into SecurityContext
	 */
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
		throws AuthenticationException {
		log.trace("Attempting to valid a JWT from a Cookie...");
		//Gets a jwt
		String jwtFromCookie = Arrays
			.stream(request.getCookies())
			.filter(cookie -> authenticationCookieName.equals(cookie.getName()))
			.findFirst()
			.orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Authentication Cookie is absent!"))
			.getValue();
		//Check if the JWT is valid
		if (jwtUtils.validateJwt(jwtFromCookie)) {
			String email = jwtUtils.getUsernameFromJwt(jwtFromCookie);
//			Authentication authenticationByEmail = workshopAuthenticationManager.getAuthenticationByEmail(email);
			Authentication authenticationByEmail =
				((WorkshopAuthenticationManager)getAuthenticationManager()).getAuthenticationByEmail(email);
			return authenticationByEmail;
		} else { //If JWT is not valid
			log.trace("JWT is not valid!");
			throw new BadCredentialsException("JWT is not a valid one!");
		}
	}
	
	@Override
	protected void successfulAuthentication(
		HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult)
		throws IOException, ServletException {
		
		log.trace("Successful authentication is being set to SecurityContext...");
		
		SecurityContextHolder.getContext().setAuthentication(authResult);
		
		getRememberMeServices().loginSuccess(request, response, authResult);
		
		chain.doFilter(request, response);
	}
	
	/**
	 * Almost a full copy of the super method
	 */
	@Override
	protected void unsuccessfulAuthentication(
		HttpServletRequest request,
		HttpServletResponse response,
		AuthenticationException failed) throws IOException, ServletException {
		
		SecurityContextHolder.clearContext();
		
		if (logger.isDebugEnabled()) {
			logger.debug("Authentication request failed: " + failed.getMessage());
			logger.debug("Updated SecurityContextHolder to contain null Authentication");
			logger.debug("Delegating to authentication failure handler " + getFailureHandler());
		}
		
		super.getRememberMeServices().loginFail(request, response);
		
		super.getFailureHandler().onAuthenticationFailure(request, response, failed);
	}
}
