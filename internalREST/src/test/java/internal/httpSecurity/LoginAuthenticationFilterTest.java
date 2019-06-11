package internal.httpSecurity;

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
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

//TODO: IN THIS TESTS I ONLY CAN REGISTER INNER METHODS INVOCATION DEPENDING ON VALID\NOT VALID AUTHENTICATION

@ExtendWith(MockitoExtension.class)
class LoginAuthenticationFilterTest {
	
	@Mock
	CookieUtils cookieUtils;
	@Mock
	JwtUtils jwtUtils;
	@Mock
	HttpServletRequestWrapper requestWrapper;
	@Mock
	HttpServletResponseWrapper responseWrapper;
	@Mock
	FilterChain filterChain;
	@Mock
	AuthenticationManager authenticationManager;
	@InjectMocks
	LoginAuthenticationFilter loginAuthenticationFilter;
	
	List<GrantedAuthority> authorities;
	Authentication authentication;
	String email = "email@workthop.pro";
	String password = "123";
	
	
	@BeforeEach
	public void init() {
		authorities = Arrays.asList(
			new SimpleGrantedAuthority("Admin"),
			new SimpleGrantedAuthority("User"));
		
		authentication = new UsernamePasswordAuthenticationToken(
			email,
			password,
			authorities);
		
		Mockito.lenient().when(requestWrapper.getContextPath()).thenReturn("/login");
		Mockito.lenient().when(requestWrapper.getRequestURI()).thenReturn("/login");

//		Mockito.lenient().when(loginAuthenticationFilter.requiresAuthentication(Mockito.any(), Mockito.any())).thenReturn(true);
		Mockito.lenient().when(authenticationManager.authenticate(Mockito.any(Authentication.class))).thenReturn(authentication);
		loginAuthenticationFilter.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/login"));
		loginAuthenticationFilter.setAuthenticationManager(authenticationManager);
	}
	
	@Test
	public void doFilterWithValidAuthenticationGeneratesCookieWithJwt() throws IOException, ServletException {
		//GIVEN
		
		
		Mockito.lenient().when(requestWrapper.getHeader("email")).thenReturn(email);
		Mockito.lenient().when(requestWrapper.getHeader("password")).thenReturn(password);
		
		ArgumentCaptor<HttpServletResponseWrapper> responseCaptor = ArgumentCaptor.forClass(
			HttpServletResponseWrapper.class);
		
		//WHEN
		loginAuthenticationFilter.doFilter(requestWrapper, responseWrapper, filterChain);
		
		//THEN
	
	}
}