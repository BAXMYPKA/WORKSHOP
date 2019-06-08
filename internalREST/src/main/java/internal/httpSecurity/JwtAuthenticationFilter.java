package internal.httpSecurity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class JwtAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
	
	
	public JwtAuthenticationFilter(String defaultFilterProcessesUrl) {
		super(defaultFilterProcessesUrl);
	}
	
	@Override
	protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
		//May be exposed as the indicator for preventing authentication for the tests
		return true;
	}
	
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		
		if (!requiresAuthentication(request, response)) {
			chain.doFilter(request, response);
			return;
		}
		
		//TODO: to implement OAuth2 JWT tokens attached by a cookie or a header. Also implement CSRF protection
		log.trace("doFilter: Extracting JWT from HTTP request...");
		
		Authentication authResult;
		try {
			authResult = attemptAuthentication(request, response);
			if (authResult == null) {
				unsuccessfulAuthentication(request, response, new InsufficientAuthenticationException(
					"Authentication is null"));
				return;
			}
		} catch (InternalAuthenticationServiceException failed) {
			log.error("An internal error occurred while trying to authenticate the user.", failed);
			unsuccessfulAuthentication(request, response, failed);
			return;
		} catch (AuthenticationException failed) {
			unsuccessfulAuthentication(request, response, failed);
			return;
		}
		
		// Authentication success
		chain.doFilter(request, response);
		
		successfulAuthentication(request, response, chain, authResult);
	}
	
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
		//TODO: to implement a class for validating and returning a JWT
		try { //Jwt exceptions may be caught to translate them into AuthenticationException
			Map<String, Object> headers = new HashMap<>(1);
			headers.put("Authorization", "null header");
			Map<String, Object> claims = new HashMap<>(1);
			claims.put("Claim", "null claim");
			Jwt jwt = new Jwt("000", Instant.now(), Instant.now().plusSeconds(2000000), headers, claims);
			JwtAuthenticationToken jwtAuthenticationToken = new JwtAuthenticationToken(jwt);
			log.debug("JwtToken extracted and passed to the AuthenticationManager.");
			AuthenticationManager ddddddddddd = getAuthenticationManager();
			Authentication authentication = getAuthenticationManager().authenticate(jwtAuthenticationToken);
			return authentication;
		} catch (Exception e) {
			throw new AuthenticationCredentialsNotFoundException("");
		}
	}
	
	
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
		super.successfulAuthentication(request, response, chain, authResult);
	}
	
	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
		super.unsuccessfulAuthentication(request, response, failed);
	}
}
