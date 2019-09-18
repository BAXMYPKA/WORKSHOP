package workshop.http;

import org.springframework.beans.factory.annotation.Value;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

/**
 * Add HttpHeaders to REST HttpServletResponses within 'workshop.internal' domain.
 */
public class ResponseHeadersInternalFilter implements Filter {
	
	@Value("${Allow}")
	private String headerAllowValue;
	@Value("${Content-Language}")
	private String headerContentLanguageValue;
	public static final HashMap<String, String> httpHeaders = new HashMap<>();
	
	public ResponseHeadersInternalFilter() {
		httpHeaders.put("Allow", headerAllowValue);
		httpHeaders.put("Content-Language", headerContentLanguageValue);
	}
	
	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		httpHeaders.entrySet().forEach(entry -> response.addHeader(entry.getKey(), entry.getValue()));
		filterChain.doFilter(servletRequest, servletResponse);
	}
}
