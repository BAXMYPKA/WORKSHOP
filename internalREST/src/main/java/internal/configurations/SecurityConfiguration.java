package internal.configurations;

import internal.filters.JwtAuthenticationFilter;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
	
//	@Autowired
//	public JwtAuthenticationFilter jwtAuthenticationFilter;
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.csrf().disable()
//			.addFilter(jwtAuthenticationFilter)
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
	public BCryptPasswordEncoder bCryptPasswordEncoder(){
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		return encoder;
	}
}
