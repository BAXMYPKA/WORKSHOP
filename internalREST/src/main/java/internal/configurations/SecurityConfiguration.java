package internal.configurations;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
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
}
