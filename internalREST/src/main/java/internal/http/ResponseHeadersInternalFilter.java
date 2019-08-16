package internal.http;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

/**
 * Add HttpHeaders to REST HttpServletResponses within 'workshop.internal' domain.
 */
public class ResponseHeadersInternalFilter implements Filter {
	
	private HashMap<String, String> httpHeaders = new HashMap<>();
	
	public ResponseHeadersInternalFilter() {
		httpHeaders.put("Allow", "GET, POST, PUT, DELETE");
		httpHeaders.put("Content-Language", "ru-RU, en-US");
//		httpHeaders.put("Content-Type", "application/hal+json");
	}
	
	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		httpHeaders.entrySet().forEach(entry -> response.addHeader(entry.getKey(), entry.getValue()));
		filterChain.doFilter(servletRequest, servletResponse);
	}
}
