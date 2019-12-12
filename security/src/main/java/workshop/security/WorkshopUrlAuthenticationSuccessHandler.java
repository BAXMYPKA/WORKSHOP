package workshop.security;

import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@NoArgsConstructor
@Component
public class WorkshopUrlAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
	
	@Setter
	private boolean useReferer = false;
	
	@Setter
	private boolean useInternalReferer = false;
	
	@Setter
	private boolean useExternalReferer = false;
	
	@Setter
	private String externalTargetUrl;
	
	@Setter
	private String internalTargetUrl;
	
	public WorkshopUrlAuthenticationSuccessHandler(String defaultUrl) {
		super(defaultUrl);
	}
	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
		throws IOException, ServletException {
		
		handle(request, response, authentication);
		clearAuthenticationAttributes(request);
	}
	
	/**
	 * Invokes the configured {@code RedirectStrategy} with the URL returned by the
	 * {@code determineTargetUrl} method.
	 * <p>
	 * The redirect will not be performed if the response has already been committed.
	 */
	@Override
	protected void handle(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException, ServletException {
		String targetUrl = determineTargetUrl(request, response);
		
		if (response.isCommitted()) {
			logger.debug("Response has already been committed. Unable to redirect to "
				+ targetUrl);
			return;
		}
		
		getRedirectStrategy().sendRedirect(request, response, targetUrl);
	}
	
	/**
	 * Builds the target URL according to the logic defined in the main class Javadoc.
	 */
	@Override
	protected String determineTargetUrl(HttpServletRequest request,
		HttpServletResponse response) {
		if (isAlwaysUseDefaultTargetUrl()) {
			return getDefaultTargetUrl();
		}
		
		// Check for the parameter and use that if available
		String targetUrl = null;
		
		if (getTargetUrlParameter() != null) {
			targetUrl = request.getParameter(getTargetUrlParameter());
			
			if (StringUtils.hasText(targetUrl)) {
				logger.debug("Found targetUrlParameter in request: " + targetUrl);
				
				return targetUrl;
			}
		}
		
		if (useInternalReferer && request.getHeader("Referer").contains("/internal/")) {
			targetUrl =
				request.getHeader("Referer").contains("/internal/login") && internalTargetUrl != null ?
					internalTargetUrl : request.getHeader("Referer");
			return targetUrl;
		}
		
		if (useExternalReferer && !request.getHeader("Referer").contains("/internal/")) {
			targetUrl =
				request.getHeader("Referer").contains("/login") && externalTargetUrl != null ?
					internalTargetUrl : request.getHeader("Referer");
			return targetUrl;
		}
		
		if (useReferer && !StringUtils.hasLength(targetUrl)) {
			targetUrl = request.getHeader("Referer");
			logger.debug("Using Referer header: " + targetUrl);
		}
		
		if (!StringUtils.hasText(targetUrl)) {
			targetUrl = getDefaultTargetUrl();
			logger.debug("Using default Url: " + targetUrl);
		}
		
		return targetUrl;
	}
	
}
