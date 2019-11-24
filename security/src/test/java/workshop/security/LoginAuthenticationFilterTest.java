package workshop.security;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.util.matcher.RequestMatcher;
import workshop.http.CookieUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class LoginAuthenticationFilterTest {
	
	@Mock
	CookieUtils cookieUtils;
	@Mock
	JwtUtils jwtUtils;
	@Mock
	HttpServletRequestWrapper request;
	@Mock
	HttpServletResponseWrapper response;
	@Mock
	FilterChain filterChain;
	@Mock
	AuthenticationManager authenticationManager;
	@Mock
	RequestMatcher matcher;
	@InjectMocks
	LoginAuthenticationFilter loginAuthenticationFilter;
	
	private List<GrantedAuthority> authorities;
	private Authentication authentication;
	private String email = "email@workthop.pro";
	private String password = "123";
	private String authenticationCookieName = "workshopJwt";
	
	
	@BeforeEach
	public void init() {
		authorities = Arrays.asList(
			new SimpleGrantedAuthority("Admin"),
			new SimpleGrantedAuthority("User"));
		
		authentication = new UsernamePasswordAuthenticationToken(
			email,
			password,
			authorities);
		
		Mockito.lenient().when(request.getContextPath()).thenReturn("workshop.pro/internal");
		Mockito.lenient().when(request.getRequestURI()).thenReturn("/login");
		Mockito.lenient().when(request.getMethod()).thenReturn("POST");
		Mockito.lenient().when(request.getParameter("email")).thenReturn(email);
		Mockito.lenient().when(request.getParameter("password")).thenReturn(password);
		
		Mockito.lenient().when(matcher.matches(request)).thenReturn(true);
		
		Mockito.lenient().when(authenticationManager.authenticate(Mockito.any(Authentication.class))).thenReturn(authentication);
		
		cookieUtils.setAuthenticationCookieName(authenticationCookieName);
		
		loginAuthenticationFilter.setRequiresAuthenticationRequestMatcher(matcher);
		loginAuthenticationFilter.setAuthenticationManager(authenticationManager);
	}
	
	@Test
	public void doFilter_With_Valid_Authentication_Generates_JWT() throws IOException, ServletException {
		//GIVEN Authentication that has to be passed into JwtUtils
		
		ArgumentCaptor<Authentication> authenticationCaptor = ArgumentCaptor.forClass(
			Authentication.class);
		
		//WHEN
		loginAuthenticationFilter.doFilter(request, response, filterChain);
		
		//THEN the initial Authentication from AuthenticationManager has to be sent for generating JWT
		Mockito.verify(jwtUtils, Mockito.atLeastOnce()).generateJwt(authenticationCaptor.capture());
		
		Assertions.assertSame(authentication, authenticationCaptor.getValue());
	}
	
	@Test
	public void doFilter_with_valid_Authentication_sets_Cookie_into_HttpResponse() throws IOException, ServletException {
		//GIVEN same Response and CookieName have to be passed to CookieUtils for creating an auth Cookie
		ArgumentCaptor<HttpServletResponseWrapper> responseCaptor = ArgumentCaptor.forClass(
			HttpServletResponseWrapper.class);
		ArgumentCaptor<String> cookieNameCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> jwtCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Integer> ttlCaptor = ArgumentCaptor.forClass(Integer.class);
		
		//WHEN
		loginAuthenticationFilter.doFilter(request, response, filterChain);
		
		//THEN the same response and cookieName are sent into CookieUtils
		Mockito.verify(cookieUtils, Mockito.atLeastOnce()).addCookieToResponse(
			responseCaptor.capture(),
			cookieNameCaptor.capture(),
			jwtCaptor.capture(),
			ttlCaptor.capture());
		
		Assertions.assertEquals(response, responseCaptor.getValue());
		Assertions.assertEquals(cookieUtils.getAuthenticationCookieName(), cookieNameCaptor.getValue());
	}
}