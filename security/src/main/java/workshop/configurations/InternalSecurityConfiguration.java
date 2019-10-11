package workshop.configurations;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import workshop.http.CookieUtils;
import workshop.security.*;

/**
 * REST servicing stateless Internal domain for Employees with SPA API.
 * For now works only with cookies for being carried the Authentication information.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "userAuditorAware")
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Order(2)
public class InternalSecurityConfiguration extends WebSecurityConfigurerAdapter {
	
	@Value("${authorizationHeaderName}")
	@Setter(AccessLevel.PACKAGE)
	private String authorizationHeaderName;
	
	@Value("${domainName}")
	@Getter //For test purposes
	@Setter(AccessLevel.PACKAGE)
	private String domainName;
	
	@Value("${internalPathName}")
	@Getter //For test purposes
	@Setter(AccessLevel.PACKAGE)
	private String internalPathName;
	
	@Value("${internalAuthCookieName}")
	@Setter(AccessLevel.PACKAGE)
	private String internalAuthCookieName;
	
	@Autowired
	private JwtUtils jwtUtils;
	
	@Autowired
	private CookieUtils cookieUtils;
	
	@Autowired
	private WorkshopPermissionEvaluator workshopPermissionEvaluator;
	
	@Autowired
	private WorkshopAuthenticationManager workshopAuthenticationManager;
	
	@Override
	public void configure(WebSecurity web) {
		DefaultWebSecurityExpressionHandler webSecurityExpressionHandler = new DefaultWebSecurityExpressionHandler();
		webSecurityExpressionHandler.setPermissionEvaluator(workshopPermissionEvaluator);
		web.expressionHandler(webSecurityExpressionHandler);
	}
	
	/**
	 * SessionManagement = STATELESS.
	 * PermitAll to "/"
	 * PermitAll to /internal/login**
	 * Authenticated() to /internal**
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.csrf().disable()
//			.cors()
//			.and()
			.sessionManagement()
			.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			.and()
			.addFilterAt(loginAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
			.addFilterAt(jwtAuthenticationFilter(), BearerTokenAuthenticationFilter.class)
			.authorizeRequests()
			.antMatchers("/internal/login**")
			.permitAll()
			.antMatchers("/internal**")
			.authenticated()
			.and()
			.formLogin()
			.loginPage("/internal/login")
			.failureHandler(internalAuthenticationFailureHandler())
			.and()
			.logout()
			.logoutUrl("/internal/login?logout=true")
			.deleteCookies(internalAuthCookieName)
			.clearAuthentication(true)
			.logoutSuccessUrl("/internal/login?logged_out=true")
			.and();
	}
	
	@Bean
	@Qualifier("employeesDetailsService")
	@DependsOn("employeesDao")
	public EmployeesDetailsService employeesDetailsService() {
		return new EmployeesDetailsService();
	}
	
	@Bean
	@DependsOn("usersDao")
	public UsersDetailsService usersDetailsService() {
		return new UsersDetailsService();
	}
	
/*
	@Bean
	@Qualifier("workshopAuthenticationManager")
	@DependsOn("employeesAuthenticationProvider")
	public WorkshopAuthenticationManager workshopAuthenticationManager() {
		WorkshopAuthenticationManager authenticationManager = new WorkshopAuthenticationManager(
			internalAuthenticationProviders());
		return authenticationManager;
	}
*/
	
	//TODO: to check double invocation of this
//	@Bean //Filters must not be injected as beans. Spring does it automatically for every Filter subclass
//	@DependsOn(value = {"jwtUtils", "cookieUtils"})
	public UsernamePasswordAuthenticationFilter loginAuthenticationFilter() {
		LoginAuthenticationFilter loginAuthenticationFilter = new LoginAuthenticationFilter();
		loginAuthenticationFilter.setAuthenticationManager(workshopAuthenticationManager);
		loginAuthenticationFilter.setCookieUtils(cookieUtils);
		loginAuthenticationFilter.setJwtUtils(jwtUtils);
		loginAuthenticationFilter.setAuthenticationCookieName(internalAuthCookieName);
		return loginAuthenticationFilter;
	}
	
	//TODO: to check double invocation if this
//	@Bean //Filters must not be injected as beans. Spring does it automatically for every Filter subclass
	public JwtAuthenticationFilter jwtAuthenticationFilter() {
		JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(
			new AntPathRequestMatcher(internalPathName));
		jwtAuthenticationFilter.setAuthenticationManager(workshopAuthenticationManager);
		jwtAuthenticationFilter.setAuthenticationFailureHandler(internalAuthenticationFailureHandler());
		jwtAuthenticationFilter.setCookieUtils(cookieUtils);
		jwtAuthenticationFilter.setJwtUtils(jwtUtils);
		jwtAuthenticationFilter.setAuthenticationCookieName(internalAuthCookieName);
		return jwtAuthenticationFilter;
	}
	
	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public SimpleUrlAuthenticationFailureHandler internalAuthenticationFailureHandler() {
		SimpleUrlAuthenticationFailureHandler authenticationFailureHandler =
			new SimpleUrlAuthenticationFailureHandler("/internal/login?login=failure");
		return authenticationFailureHandler;
	}
	
	@Bean
	public AuthenticationSuccessHandler authenticationSuccessHandler() {
		SimpleUrlAuthenticationSuccessHandler successHandler = new SimpleUrlAuthenticationSuccessHandler();
		successHandler.setDefaultTargetUrl("/internal/a");
		return successHandler;
	}
	
	@Bean
	public UserAuditorAware userAuditorAware() {
		return new UserAuditorAware();
	}
}
