package internal.configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebMvcConfiguration implements WebMvcConfigurer {
	
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry
			.addMapping("/internal/**")
			.allowCredentials(true)
			.allowedOrigins("https://localhost:3000")
			.allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD")
			.allowedHeaders("Authorization", "Cache-Control", "Content-Type", "Accept")
			.maxAge(60*60*12);
	}
	
}
