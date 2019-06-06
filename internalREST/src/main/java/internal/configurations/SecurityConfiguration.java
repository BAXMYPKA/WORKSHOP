package internal.configurations;

import internal.httpSecurity.EmployeesDetailsService;
import internal.httpSecurity.JwtAuthenticationFilter;
import internal.httpSecurity.WorkshopAuthenticationManager;
import internal.httpSecurity.EmployeesAuthenticationProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.HashSet;
import java.util.Set;

@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.csrf().disable()
			.sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			.and()
			.authorizeRequests()
				.antMatchers("/internal/login")
					.permitAll()
				.antMatchers("/internal/**")
					.hasAnyAuthority("Employee")
			.and()
				.formLogin()
					.loginPage("/internal/login")
					.failureForwardUrl("/internal/login?logged=false");
//					.successForwardUrl("/internal/login?logged=true");
	}
	
	@Bean
	@Qualifier("employeesDetailsService")
	@DependsOn("employeesDao")
	public EmployeesDetailsService employeesDetailsService(){
		return new EmployeesDetailsService();
	}
	
	@Bean
	@Qualifier("employeesAuthenticationProvider")
	@DependsOn("employeesDetailsService")
	public EmployeesAuthenticationProvider employeesAuthenticationProvider(){
		return new EmployeesAuthenticationProvider();
	}
	
	@Bean
	public Set<AuthenticationProvider> internalAuthenticationProviders(){
		Set<AuthenticationProvider> authenticationProviders = new HashSet<>(4);
		authenticationProviders.add(employeesAuthenticationProvider());
		return authenticationProviders;
	}
	
	@Bean
	@Qualifier("workshopAuthenticationManager")
	@DependsOn("employeesAuthenticationProvider")
	public WorkshopAuthenticationManager workshopAuthenticationManager(){
		WorkshopAuthenticationManager authenticationManager = new WorkshopAuthenticationManager();
		authenticationManager.setInternalAuthenticationProviders(internalAuthenticationProviders());
		return new WorkshopAuthenticationManager();
	}
	
	@Bean
	@DependsOn("workshopAuthenticationManager")
	public UsernamePasswordAuthenticationFilter authenticationFilter() {
		UsernamePasswordAuthenticationFilter authenticationFilter = new JwtAuthenticationFilter();
		authenticationFilter.setAuthenticationManager(workshopAuthenticationManager());
		return authenticationFilter;
	}
	
	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder(){
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		return encoder;
	}
}
