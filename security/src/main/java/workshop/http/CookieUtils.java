package workshop.http;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

@Slf4j
@Setter(AccessLevel.PROTECTED)
@Getter(AccessLevel.PACKAGE)
@NoArgsConstructor
@Component
public class CookieUtils {
	
	private final String COOKIE_PATH = "/workshop.pro/";
	
	@Setter(AccessLevel.PACKAGE)
	@Value("${domainName}")
	private String domainName;
	
	@Setter(AccessLevel.PACKAGE)
	@Value("${internalPathName}")
	private String internalPathName;
	
	@Value("${internalAuthCookieName}")
	@Getter(AccessLevel.PUBLIC)
	@Setter(AccessLevel.PUBLIC) //For the test purposes
	private String authenticationCookieName;
	
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
		//TODO: 'workshop.pro' cannot be set for localhost
//		cookie.setDomain(domainName);
		cookie.setPath(COOKIE_PATH); //Should be set to be identical
		response.addCookie(cookie);
	}
	
	/**
	 * ALL the Cookie parameters MUST BE identical to those which had been added in
	 * {@link #addCookieToResponse(HttpServletResponse, String, String, Integer)} for the Cookie to be deleted!
	 */
	public void deleteCookieFromResponse(HttpServletRequest request, HttpServletResponse response, String cookieName) {
		if (request == null || response == null || cookieName == null || cookieName.isEmpty()) {
			throw new IllegalArgumentException("Request or CookieName is null or empty!");
		}
		Cookie[] cookies = request.getCookies();
		Arrays.stream(cookies)
			.filter(cookie -> cookieName.equals(cookie.getName()))
			.findFirst()
			.ifPresent(cookie -> {
				cookie.setMaxAge(0);
				cookie.setValue("");
				cookie.setHttpOnly(true);
				cookie.setPath(COOKIE_PATH);
				response.addCookie(cookie);
			});
	}
}
