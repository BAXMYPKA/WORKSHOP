package workshop.configurations;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.*;
import org.springframework.security.web.session.SessionManagementFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import workshop.http.CookieUtils;
import workshop.security.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Http Web servicing stateful External domain for Users.
 */
@EnableWebSecurity
@Order(1)
public class ExternalSecurityConfiguration extends WebSecurityConfigurerAdapter {
	
	@Value("${authorizationHeaderName}")
	@Setter(AccessLevel.PACKAGE)
	private String authorizationHeaderName;
	
	@Value("${domainName}")
	@Getter //For test purposes
	@Setter(AccessLevel.PACKAGE)
	private String domainName;
	
	@Value("${internalAuthCookieName}")
	@Setter(AccessLevel.PACKAGE)
	private String internalAuthCookieName;
	
	@Value("${externalAuthCookieName}")
	private String externalAuthCookieName;
	
	@Autowired
	private JwtUtils jwtUtils;
	
	@Autowired
	private CookieUtils cookieUtils;
	
/*
	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().mvcMatchers("/dist/css/**", "/dist/img/**", "/dist/css/**").anyRequest();
	}
*/
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.csrf().disable()
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
			.and()
			.addFilterAt(loginAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
			.addFilterAt(jwtAuthenticationFilter(), BearerTokenAuthenticationFilter.class)
//			.addFilterAt(jwtAuthenticationFilter(), SessionManagementFilter.class)
			.authorizeRequests()
			.antMatchers("/**")
			.permitAll()
			.antMatchers("/profile**")
			.authenticated()
			.antMatchers("/**", "/login**")
			.permitAll()
			.and()
			.formLogin()
			.loginPage("/login")
			.failureHandler(externalAuthenticationFailureHandler())
			.successHandler(externalAuthenticationSuccessHandler())
			.and()
			.logout()
			.logoutUrl("/login?logout=true")
			.deleteCookies(internalAuthCookieName)
			.clearAuthentication(true)
			.logoutSuccessUrl("/")
			.and();
		
	}
	
//	@Bean //Filters must not be injected as beans. Spring does it automatically for every Filter subclass
//	@DependsOn(value = {"jwtUtils", "cookieUtils"})
	public UsernamePasswordAuthenticationFilter loginAuthenticationFilter() {
		LoginAuthenticationFilter loginAuthenticationFilter = new LoginAuthenticationFilter();
		loginAuthenticationFilter.setAuthenticationFailureHandler(externalAuthenticationFailureHandler());
		loginAuthenticationFilter.setAuthenticationSuccessHandler(externalAuthenticationSuccessHandler());
		loginAuthenticationFilter.setAuthenticationManager(workshopAuthenticationManager());
		loginAuthenticationFilter.setCookieUtils(cookieUtils);
		loginAuthenticationFilter.setJwtUtils(jwtUtils);
		loginAuthenticationFilter.setAuthenticationCookieName(externalAuthCookieName);
		return loginAuthenticationFilter;
	}
	
//	@Bean //Filters must not be injected as beans. Spring does it automatically for every Filter subclass
	public JwtAuthenticationFilter jwtAuthenticationFilter() {
		JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(
			new AntPathRequestMatcher("/**"));
		jwtAuthenticationFilter.setAuthenticationManager(workshopAuthenticationManager());
		jwtAuthenticationFilter.setAuthenticationFailureHandler(externalAuthenticationFailureHandler());
		jwtAuthenticationFilter.setCookieUtils(cookieUtils);
		jwtAuthenticationFilter.setJwtUtils(jwtUtils);
		jwtAuthenticationFilter.setAuthenticationCookieName(externalAuthCookieName);
		return jwtAuthenticationFilter;
	}
	
	@Bean
	@DependsOn("usersDao")
	public UsersDetailsService usersDetailsService() {
		return new UsersDetailsService();
	}
	
	@Bean
	@Qualifier("employeesAuthenticationProvider")
	@DependsOn("employeesDetailsService")
	public EmployeesAuthenticationProvider employeesAuthenticationProvider() {
		return new EmployeesAuthenticationProvider();
	}
	
	@Bean
	@Qualifier("usersAuthenticationProvider")
	@DependsOn("usersDetailsService")
	public UsersAuthenticationProvider usersAuthenticationProvider() {
		return new UsersAuthenticationProvider();
	}
	
	@Bean
	public Set<AuthenticationProvider> authenticationProviders() {
		Set<AuthenticationProvider> authenticationProviders = new HashSet<>(4);
		authenticationProviders.add(employeesAuthenticationProvider());
		authenticationProviders.add(usersAuthenticationProvider());
		return authenticationProviders;
	}
	
	@Bean
	@Qualifier("workshopAuthenticationManager")
	@DependsOn("employeesAuthenticationProvider")
	public WorkshopAuthenticationManager workshopAuthenticationManager() {
		WorkshopAuthenticationManager authenticationManager =
			new WorkshopAuthenticationManager(authenticationProviders());
		return authenticationManager;
	}
	
	@Bean
	public ExternalAuthenticationFailureHandler externalAuthenticationFailureHandler() {
		ExternalAuthenticationFailureHandler externalAuthenticationFailureHandler =
			new ExternalAuthenticationFailureHandler("/login?login=false");
		return externalAuthenticationFailureHandler;
	}
	
	@Bean
	public AuthenticationSuccessHandler externalAuthenticationSuccessHandler() {
		SimpleUrlAuthenticationSuccessHandler successHandler =
			new SimpleUrlAuthenticationSuccessHandler();
		successHandler.setUseReferer(true);
		return successHandler;
	}
}
