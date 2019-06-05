package internal.configurations;

import internal.httpSecurity.JwtAuthenticationFilter;
import internal.httpSecurity.WorkshopAuthenticationManager;
import internal.httpSecurity.WorkshopAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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
	}
	
	@Bean
	public AuthenticationProvider authenticationProvider(){
		return new WorkshopAuthenticationProvider();
	}
	
	@Bean
	@DependsOn("authenticationProvider")
	public WorkshopAuthenticationManager authenticationManager(){
		WorkshopAuthenticationManager authenticationManager = new WorkshopAuthenticationManager();
		authenticationManager.setAuthenticationProvider(authenticationProvider());
		return new WorkshopAuthenticationManager();
	}
	
	@Bean
	@DependsOn(value = "authenticationManager")
	public UsernamePasswordAuthenticationFilter authenticationFilter() {
		UsernamePasswordAuthenticationFilter authenticationFilter = new JwtAuthenticationFilter();
		authenticationFilter.setAuthenticationManager(authenticationManager());
		return authenticationFilter;
	}
	
//	@Override
//	public UserDetailsService userDetailsServiceBean() throws Exception {
//		return super.userDetailsServiceBean();
//	}
	
	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder(){
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		return encoder;
	}
}
