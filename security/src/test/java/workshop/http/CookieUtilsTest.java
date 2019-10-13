package workshop.http;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import workshop.configurations.InternalSecurityConfiguration;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponseWrapper;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CookieUtilsTest {
	
	private final String domainName = "workshop.pro";
	private final String internalPathName = "/internal";
	
	@Mock
	private InternalSecurityConfiguration internalSecurityConfiguration;
	@Mock
	private HttpServletRequestWrapper requestWrapper;
	@Mock
	private HttpServletResponseWrapper responseWrapper;
	@InjectMocks
	private CookieUtils cookieUtils;
	
	@BeforeEach
	public void init() {
		cookieUtils.setDomainName(domainName);
		cookieUtils.setInternalPathName(internalPathName);
	}
	
	@Test
	public void addCookie() {
		//GIVEN
		ArgumentCaptor<Cookie> argumentCaptor = ArgumentCaptor.forClass(Cookie.class);
		
		//WHEN
		cookieUtils.addCookieToResponse(responseWrapper, "CookieToAdd", "CookieValue", 100);
		
		//THEN
		Mockito.verify(responseWrapper).addCookie(argumentCaptor.capture());
		assertEquals("CookieToAdd", argumentCaptor.getValue().getName());
		assertEquals("CookieValue", argumentCaptor.getValue().getValue());
		assertEquals(domainName, argumentCaptor.getValue().getDomain());
		assertEquals(internalPathName, argumentCaptor.getValue().getPath());
		assertEquals(100, argumentCaptor.getValue().getMaxAge());
		assertTrue(argumentCaptor.getValue().isHttpOnly());
		//Last control check
		assertNotEquals("Cookie", argumentCaptor.getValue().getName());
	}
	
	@Test
	public void deleteCookieProperlyFilterNameAndAddCookieIntoResponse() {
		//GIVEN
		Cookie cookie1 = new Cookie("CookieName", "CookieValue");
		cookie1.setMaxAge(100);
		Cookie cookie2 = new Cookie("CookieSecond", "CookieSecondValue");
		cookie2.setMaxAge(200);
		
		Cookie[] cookies = new Cookie[]{cookie1, cookie2};
		
		Mockito.when(requestWrapper.getCookies()).thenReturn(cookies);
		
		ArgumentCaptor<Cookie> argumentCaptor = ArgumentCaptor.forClass(Cookie.class);
		
		//WHEN
		cookieUtils.deleteCookieFromResponse(requestWrapper, responseWrapper, "CookieSecond");
		
		//THEN the method adds the definite cookie with 0 age
		Mockito.verify(responseWrapper, Mockito.never()).addCookie(cookie1);
		Mockito.verify(responseWrapper).addCookie(cookie2);
		Mockito.verify(responseWrapper).addCookie(argumentCaptor.capture());
		assertEquals(0, argumentCaptor.getValue().getMaxAge());
	}
	
}