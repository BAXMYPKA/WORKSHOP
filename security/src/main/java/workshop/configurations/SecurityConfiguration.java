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
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import workshop.http.CookieUtils;
import workshop.security.*;

import java.util.HashSet;
import java.util.Set;

@Configuration
@EnableWebSecurity
@EnableJpaAuditing(auditorAwareRef = "userAuditorAware")
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration {
	
	@Order(1)
	@Configuration
	public static class InternalSecurityConfiguration extends WebSecurityConfigurerAdapter {
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
			web.ignoring().antMatchers("/dist/internal/css/**", "/dist/internal/img/**", "/dist/internal/css/**",
				"/dist/internal/js/**");
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
				.antMatcher("/internal**")
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
//				.antMatchers("/internal/**")
//				.hasAuthority("EMPLOYEE_READ")
				.and()
				.formLogin()
				.loginPage("/internal/login")
//				.loginProcessingUrl("/internal/login")
				.failureHandler(internalAuthenticationFailureHandler())
				.successHandler(internalAuthenticationSuccessHandler())
				.and()
				.logout()
				.logoutUrl("/internal/logout")
				.deleteCookies(internalAuthCookieName)
//				.invalidateHttpSession(true)
				.clearAuthentication(true)
				.logoutSuccessUrl("/internal/login")
				.and();
		}
		
		@Bean
		@Qualifier("employeesDetailsService")
		@DependsOn("employeesDao")
		public EmployeesDetailsService employeesDetailsService() {
			return new EmployeesDetailsService();
		}
		
		//	@Bean //Filters must not be injected as beans. Spring does it automatically for every Filter subclass
//	@DependsOn(value = {"jwtUtils", "cookieUtils"})
		public UsernamePasswordAuthenticationFilter loginAuthenticationFilter() {
			LoginAuthenticationFilter loginAuthenticationFilter = new LoginAuthenticationFilter();
			loginAuthenticationFilter.setAuthenticationFailureHandler(internalAuthenticationFailureHandler());
			loginAuthenticationFilter.setAuthenticationSuccessHandler(internalAuthenticationSuccessHandler());
			loginAuthenticationFilter.setAuthenticationManager(workshopAuthenticationManager);
			loginAuthenticationFilter.setCookieUtils(cookieUtils);
			loginAuthenticationFilter.setJwtUtils(jwtUtils);
			loginAuthenticationFilter.setAuthenticationCookieName(internalAuthCookieName);
			return loginAuthenticationFilter;
		}
		
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
	
/*
	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}
*/
		
		@Bean
		public SimpleUrlAuthenticationFailureHandler internalAuthenticationFailureHandler() {
			SimpleUrlAuthenticationFailureHandler authenticationFailureHandler =
				new SimpleUrlAuthenticationFailureHandler("/internal/login?login=false");
			return authenticationFailureHandler;
		}
		
		@Bean
		public AuthenticationSuccessHandler internalAuthenticationSuccessHandler() {
			SimpleUrlAuthenticationSuccessHandler successHandler = new SimpleUrlAuthenticationSuccessHandler();
			successHandler.setDefaultTargetUrl("/internal/application");
			successHandler.setUseReferer(true);
			return successHandler;
		}
		
		@Bean
		public UserAuditorAware userAuditorAware() {
			return new UserAuditorAware();
		}
	}
	
	
	@Order(2)
	@Configuration
	public static class ExternalSecurityConfiguration extends WebSecurityConfigurerAdapter {
		@Value("${authorizationHeaderName}")
		@Setter(AccessLevel.PACKAGE)
		private String authorizationHeaderName;
		
		@Value("${domainName}")
		@Getter //For test purposes
		@Setter(AccessLevel.PACKAGE)
		private String domainName;
		
		@Value("${externalAuthCookieName}")
		private String externalAuthCookieName;
		
		@Autowired
		private JwtUtils jwtUtils;
		
		@Autowired
		private CookieUtils cookieUtils;
		
		@Override
		public void configure(WebSecurity web) throws Exception {
			web.ignoring().antMatchers("/dist/css/**", "/dist/img/**", "/dist/css/**", "/dist/js/**");
		}
		
		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http
				.antMatcher("/**")
				.csrf().disable()
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
				.and()
				.addFilterAt(loginAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
				.addFilterAt(jwtAuthenticationFilter(), BearerTokenAuthenticationFilter.class)
				.authorizeRequests()
				.antMatchers("/profile**", "/profile/**", "/build**")
				.hasAuthority("READ-PROFILE")
				.antMatchers("/**", "/login**", "/registration**", "/registration/**")
				.permitAll()
				.and()
				.formLogin()
				.loginPage("/login")
				.failureHandler(externalAuthenticationFailureHandler())
				.successHandler(externalAuthenticationSuccessHandler())
				.and()
				.logout()
				.logoutUrl("/logout")
				.deleteCookies(externalAuthCookieName, "JSESSIONID")
				.invalidateHttpSession(true)
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
				new ExternalAuthenticationFailureHandler("/#loginModalBackground");
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
}
