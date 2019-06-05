package internal.httpSecurity;

import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
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
 */
@Slf4j
@Setter
@NoArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
	
	/**
	 * If Authentication won't be put into SecurityContext following Authorization process will be failed.
	 */
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		log.trace("doFilter...");
		try {
			Authentication authentication = attemptAuthentication((HttpServletRequest) req, (HttpServletResponse) res);
			SecurityContextHolder.getContext().setAuthentication(authentication);
		} catch (AuthenticationException e) {
			log.trace(e.getMessage());
			super.doFilter(req, res, chain);
		}
	}
	
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
		log.trace("Authentication filter attempt...");
		UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
			null, null);
		getAuthenticationManager().authenticate(authenticationToken);
		return authenticationToken;
	}
}
