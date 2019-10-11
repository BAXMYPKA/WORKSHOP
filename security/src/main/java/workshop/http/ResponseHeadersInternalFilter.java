package workshop.http;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

/**
 * Add HttpHeaders to REST HttpServletResponses within 'workshop.internal' domain.
 */
public class ResponseHeadersInternalFilter implements Filter {
	
	public static final HashMap<String, String> httpHeaders = new HashMap<>();
	private String headerAllowValue;
	private String headerContentLanguageValue;
	
	public ResponseHeadersInternalFilter(String headerAllowValue, String headerContentLanguageValue) {
		this.headerAllowValue = headerAllowValue;
		this.headerContentLanguageValue = headerContentLanguageValue;
		httpHeaders.put("Allow", headerAllowValue);
		httpHeaders.put("Content-Language", headerContentLanguageValue);
	}
	
	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
		throws IOException, ServletException {
		
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		httpHeaders.entrySet().forEach(entry -> response.addHeader(entry.getKey(), entry.getValue()));
		filterChain.doFilter(servletRequest, servletResponse);
	}
}
