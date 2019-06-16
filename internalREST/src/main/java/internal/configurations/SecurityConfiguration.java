package internal.configurations;

import internal.httpSecurity.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.authentication.AuthenticationProvider;
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
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.HashSet;
import java.util.Set;

@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
	
	//TODO: to do all the environment variables to be loaded from outside .properties (cookie name, ttl, domain etc)
	
	@Getter @Setter
	private String domainName = "workshop.pro";
	@Getter @Setter
	private String internalPathName = "/internal/";
	private static final String AUTHORIZATION_HEADER_NAME = "Authorization";
	
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
			.antMatchers("/internal/a")
			.hasAnyAuthority("Administrator")
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
			.deleteCookies("workshopJwt")
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
	@Qualifier("employeesAuthenticationProvider")
	@DependsOn("employeesDetailsService")
	public EmployeesAuthenticationProvider employeesAuthenticationProvider() {
		return new EmployeesAuthenticationProvider();
	}
	
	@Bean
	public Set<AuthenticationProvider> internalAuthenticationProviders() {
		Set<AuthenticationProvider> authenticationProviders = new HashSet<>(4);
		authenticationProviders.add(employeesAuthenticationProvider());
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
//		authenticationFilter.setAuthenticationSuccessHandler(authenticationSuccessHandler());
		loginAuthenticationFilter.setAuthenticationManager(workshopAuthenticationManager());
		return loginAuthenticationFilter;
	}
	
//	@Bean //Filters must not be injected as beans. Spring does it automatically for every Filter subclass
	public JwtAuthenticationFilter jwtAuthenticationFilter(){
		JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(
			new AntPathRequestMatcher(internalPathName, "POST"));
//		JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(internalPathName);
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
	public AuthenticationSuccessHandler authenticationSuccessHandler(){
		SimpleUrlAuthenticationSuccessHandler successHandler = new SimpleUrlAuthenticationSuccessHandler();
		successHandler.setDefaultTargetUrl("/internal/a");
		return successHandler;
	}
}
