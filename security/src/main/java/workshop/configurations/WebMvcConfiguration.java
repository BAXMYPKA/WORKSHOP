package workshop.configurations;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.config.EnableEntityLinks;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import workshop.http.ResponseHeadersInternalFilter;

@Configuration
@EnableWebMvc
@EnableEntityLinks
public class WebMvcConfiguration implements WebMvcConfigurer {
	
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry
			.addMapping("/internal/**")
			.allowCredentials(true)
			.allowedOrigins("https://localhost:3000")
			.allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD")
			.allowedHeaders("Authorization", "Cache-Control", "Content-Type", "Accept")
			.maxAge(60 * 60 * 12);
	}
	
	@Bean
	public FilterRegistrationBean<ResponseHeadersInternalFilter> filterFilterRegistrationBean() {
		FilterRegistrationBean<ResponseHeadersInternalFilter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(new ResponseHeadersInternalFilter());
		registrationBean.addUrlPatterns("/internal/*");
		return registrationBean;
	}
}
