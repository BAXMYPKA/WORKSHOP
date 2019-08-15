package internal.http;

import internal.configurations.SecurityConfiguration;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

@Slf4j
@Setter
@Getter
@NoArgsConstructor
@Component
public class CookieUtils {
	
	@Autowired
	SecurityConfiguration securityConfiguration;
	private String authenticationCookieName = "workshopAuthentication";
	/**
	 * In seconds. Default is 259_200 (3 days)
	 */
	private int authenticationCookieTtl = 60 * 60 * 24 * 3;
	
	public void addCookieToResponse(HttpServletResponse response, String cookieName, String cookieValue, @Nullable Integer ttl) {
		if (response == null || cookieName == null || cookieName.isEmpty() || cookieValue == null || cookieValue.isEmpty()) {
			throw new IllegalArgumentException("Response, CookieName or Cookie value is null or empty!");
		}
		Cookie cookie = new Cookie(cookieName, cookieValue);
		cookie.setHttpOnly(true);
		cookie.setMaxAge(ttl == null ? authenticationCookieTtl : ttl);
		cookie.setDomain(securityConfiguration.getDomainName());
		cookie.setPath(securityConfiguration.getInternalPathName());
		response.addCookie(cookie);
	}
	
	public void deleteCookieFromResponse(HttpServletRequest request, HttpServletResponse response, String cookieName) {
		if (request == null || response == null || cookieName == null || cookieName.isEmpty()) {
			throw new IllegalArgumentException("Request or CookieName is null or empty!");
		}
		Cookie[] cookies = request.getCookies();
		Arrays.stream(cookies)
			.filter(cookie -> cookieName.equals(cookie.getName()))
			.findFirst()
			.ifPresent(cookie -> {cookie.setMaxAge(0); response.addCookie(cookie);});
	}
}
