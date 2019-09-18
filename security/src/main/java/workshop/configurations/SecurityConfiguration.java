package workshop.configurations;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import workshop.security.*;

import java.util.HashSet;
import java.util.Set;

/**
 * For now works only with cookies for being carried the Authentication information.
 */
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableJpaAuditing(auditorAwareRef = "userAuditorAware")
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
	
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
	
	@Value("${authenticationCookieName}")
	@Setter(AccessLevel.PACKAGE)
	private String authenticationCookieName;
	
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
			.antMatchers("/")
			.permitAll()
			.and()
			.formLogin()
			.loginPage("/internal/login")
			.failureHandler(authenticationFailureHandler())
			.and()
			.logout()
			.logoutUrl("/internal/login?logout=true")
			.deleteCookies(authenticationCookieName)
			.clearAuthentication(true)
			.logoutSuccessUrl("/internal/login?logged_out=true");
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
	public Set<AuthenticationProvider> internalAuthenticationProviders() {
		Set<AuthenticationProvider> authenticationProviders = new HashSet<>(4);
		authenticationProviders.add(employeesAuthenticationProvider());
		authenticationProviders.add(usersAuthenticationProvider());
		return authenticationProviders;
	}
	
	@Bean
	@Qualifier("workshopAuthenticationManager")
	@DependsOn("employeesAuthenticationProvider")
	public WorkshopAuthenticationManager workshopAuthenticationManager() {
		WorkshopAuthenticationManager authenticationManager = new WorkshopAuthenticationManager(
			internalAuthenticationProviders());
		return authenticationManager;
	}
	
	//	@Bean //Filters must not be injected as beans. Spring does it automatically for every Filter subclass
	public UsernamePasswordAuthenticationFilter loginAuthenticationFilter() {
		UsernamePasswordAuthenticationFilter loginAuthenticationFilter = new LoginAuthenticationFilter();
		loginAuthenticationFilter.setAuthenticationManager(workshopAuthenticationManager());
		return loginAuthenticationFilter;
	}
	
	//	@Bean //Filters must not be injected as beans. Spring does it automatically for every Filter subclass
	public JwtAuthenticationFilter jwtAuthenticationFilter() {
		JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(
			new AntPathRequestMatcher(internalPathName));
		jwtAuthenticationFilter.setAuthenticationManager(workshopAuthenticationManager());
		jwtAuthenticationFilter.setAuthenticationFailureHandler(authenticationFailureHandler());
		return jwtAuthenticationFilter;
	}
	
	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		return encoder;
	}
	
	@Bean
	public SimpleUrlAuthenticationFailureHandler authenticationFailureHandler() {
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
