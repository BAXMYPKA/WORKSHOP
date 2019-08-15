package internal.http;

import internal.configurations.SecurityConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponseWrapper;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CookieUtilsTest {
	
	@Mock
	SecurityConfiguration securityConfiguration;
	@Mock
	HttpServletRequestWrapper requestWrapper;
	@Mock
	HttpServletResponseWrapper responseWrapper;
	@InjectMocks
	CookieUtils cookieUtils;
	
	@BeforeEach
	public void init() {
		Mockito.lenient().when(securityConfiguration.getDomainName()).thenReturn("workshop.pro");
		Mockito.lenient().when(securityConfiguration.getInternalPathName()).thenReturn("/internal/");
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
		assertEquals(securityConfiguration.getDomainName(), argumentCaptor.getValue().getDomain());
		assertEquals(securityConfiguration.getInternalPathName(), argumentCaptor.getValue().getPath());
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