package workshop.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * First intended to obtain HttpRequest 'referer' header to redirect back to page a User has tried to log in to.
 * If that header is not presented, 'defaultFailureUrl' will be used.
 * Otherwise default behaviour will occur.
 */
@Slf4j
public class ExternalAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
	public ExternalAuthenticationFailureHandler(String defaultFailureUrl) {
		super(defaultFailureUrl);
	}
	
	@Override
	public void onAuthenticationFailure(
		HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
		throws IOException, ServletException {
		
		String refererUrl = request.getHeader("referer");
		
		if (refererUrl != null) {
			
			log.debug("HttpHeader referer={} will be used to redirect User back", refererUrl);
			
			RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
			redirectStrategy.sendRedirect(request, response, refererUrl);
		} else {
			super.onAuthenticationFailure(request, response, exception);
		}
	}
}
