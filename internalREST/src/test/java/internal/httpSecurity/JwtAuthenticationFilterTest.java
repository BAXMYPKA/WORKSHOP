package internal.httpSecurity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.util.Arrays;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {
	@Mock
	RequestMatcher matcher;
	@Mock
	FilterChain filterChain;
	@Mock
	HttpServletRequestWrapper request;
	@Mock
	HttpServletResponseWrapper response;
	@Mock
	Authentication validAuthentication;
	@Mock
	RememberMeServices rememberMeServices;
	@Mock
	CookieUtils cookieUtils;
	@Mock
	JwtUtils jwtUtils;
	@Mock
	WorkshopAuthenticationManager workshopAuthenticationManager;
	@InjectMocks
	JwtAuthenticationFilter jwtAuthenticationFilter;
	//Valid cookie name
	String authCookieName = "workshopAuthentication";
	String authCookieJwtValue = "header.claims.signature";
	//Valid email from the JWT
	String validEmail = "workshopEmployee";
	
	@BeforeEach
	public void init() {
		jwtAuthenticationFilter = new JwtAuthenticationFilter(matcher);
		jwtAuthenticationFilter.setCookieUtils(cookieUtils);
		jwtAuthenticationFilter.setJwtUtils(jwtUtils);
		jwtAuthenticationFilter.setWorkshopAuthenticationManager(workshopAuthenticationManager);
		jwtAuthenticationFilter.setRememberMeServices(rememberMeServices);
		
		//TODO: test cookieUtils not return null with this name
		
		//CookieUtils must return that valid auth cookie name
		Mockito.lenient().when(cookieUtils.getAuthenticationCookieName()).thenReturn(authCookieName);
		
		//Request must contain a valid authentication cookie with a valid name
		request = Mockito.mock(HttpServletRequestWrapper.class);
		Mockito.lenient().when(request.getCookies()).thenReturn(new Cookie[]{
			new Cookie("BadCookie", "BadValue"), new Cookie(authCookieName, authCookieJwtValue)});
		
		Mockito.lenient().when(matcher.matches(request)).thenReturn(true);
		
		//To successfully validate the AuthCookie
		Mockito.lenient().when(jwtUtils.validateJwt(authCookieJwtValue)).thenReturn(true);
		//To successfully return a valid email from the AuthCookie
		Mockito.lenient().when(jwtUtils.getUsernameFromJwt(authCookieJwtValue)).thenReturn(validEmail);
		//To include into
		validAuthentication = new UsernamePasswordAuthenticationToken(
			validEmail, "", Arrays.asList(new SimpleGrantedAuthority("Admin")));
		//To return the valid Authentication
		Mockito.lenient().when(workshopAuthenticationManager.getAuthenticationByEmail(validEmail)).thenReturn(validAuthentication);
	}
	
	@Test
	public void valid_JWT_with_valid_email_will_invoke_successfulAuthentication_and_further_filterChain()
		throws IOException, ServletException {
		//GIVEN all inputs are valid from init method
		
		//WHEN
		jwtAuthenticationFilter.doFilter(request, response, filterChain);
		
		//THEN jwtAuthenticationFilter.successfulAuthentication.getRememberMeServices.loginSuccess MUST be called
		Mockito.verify(rememberMeServices, Mockito.atLeastOnce()).loginSuccess(request, response, validAuthentication);
	}
	
	@Test
	public void not_valid_cookie_name_invokes_only_filterChain_in_doFiller() throws IOException, ServletException {
		//GIVEN a different authentication cookie name
		Mockito.when(cookieUtils.getAuthenticationCookieName()).thenReturn("NotValidName");
		
		//WHEN
		jwtAuthenticationFilter.doFilter(request, response, filterChain);
		
		//THEN only first filterChain in doFilter is invoked
		Mockito.verify(filterChain, Mockito.atLeastOnce()).doFilter(request, response);
		
		Mockito.verify(rememberMeServices, Mockito.never()).loginFail(request, response);
		Mockito.verify(rememberMeServices, Mockito.never()).loginSuccess(request, response, validAuthentication);
		Mockito.verify(jwtUtils, Mockito.never()).validateJwt(authCookieJwtValue);
	}
	
	@Test
	public void not_valid_email() throws IOException, ServletException {
		//GIVEN not a valid email
		Mockito.lenient().when(jwtUtils.getUsernameFromJwt(authCookieJwtValue)).thenReturn("notValidEmail");
		Mockito.lenient().when(workshopAuthenticationManager.getAuthenticationByEmail("notValidEmail")).
			thenThrow(new BadCredentialsException("Not valid email"));
		
		//WHEN
		jwtAuthenticationFilter.doFilter(request, response, filterChain);
		
		//THEN
		Mockito.verify(rememberMeServices, Mockito.atLeastOnce()).loginFail(request, response);
	}
}