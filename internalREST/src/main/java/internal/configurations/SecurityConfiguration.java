package internal.configurations;

import internal.httpSecurity.*;
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
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.HashSet;
import java.util.Set;

@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
	
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
//			.mvcMatchers("/internal/")
			.antMatchers("/internal**")
			.authenticated()
			.antMatchers("/")
			.permitAll()
			.and()
			.formLogin()
			.loginPage("/internal/login")
			.failureForwardUrl("/internal/login?logged=false");
//			.successHandler(authenticationSuccessHandler()).permitAll();
//			.defaultSuccessUrl("/internal/", true)
//			.successForwardUrl("/internal/");
//			.and()
//			.logout()
//			.deleteCookies("workshopJwt")
//			.clearAuthentication(true)
//			.logoutSuccessUrl("/internal/login?logged_out=true");
	}
	
/*
	@Override
	public void configure(WebSecurity web) throws Exception {
		
		super.configure(web);
	}
*/
	
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
		JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter("/internal/");
		jwtAuthenticationFilter.setAuthenticationManager(workshopAuthenticationManager());
		return jwtAuthenticationFilter;
	}
	
	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		return encoder;
	}
	
//	@Bean
//	public AuthenticationSuccessHandler authenticationSuccessHandler(){
//		SimpleUrlAuthenticationSuccessHandler successHandler = new SimpleUrlAuthenticationSuccessHandler();
//		successHandler.setDefaultTargetUrl("/internal/a");
//		return successHandler;
//	}
}
