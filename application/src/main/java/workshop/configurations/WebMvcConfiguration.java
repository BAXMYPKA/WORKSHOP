package workshop.configurations;

import lombok.AccessLevel;
import lombok.Setter;
import nz.net.ultraq.thymeleaf.LayoutDialect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.config.EnableEntityLinks;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.spring5.SpringTemplateEngine;
import workshop.http.ResponseHeadersInternalFilter;

@Configuration
@EnableWebMvc
@EnableEntityLinks
public class WebMvcConfiguration implements WebMvcConfigurer {
	
	@Value("${internalPathName}")
	private String internalPathName;
	
	//TODO: to remake as an Array of Strings
	@Value("${corsAllowedOrigins}")
	@Setter(AccessLevel.PACKAGE)
	private String corsAllowedOrigins;
	
	@Value("@{Allow}")
	@Setter(AccessLevel.PACKAGE)
	private String allowedMethods;
	
	@Value("${corsRegistryMaxAge}")
	@Setter(AccessLevel.PACKAGE)
	private Integer corsRegistryMaxAge;
	
	@Value("${Allow}")
	private String headerAllowValue;
	
	@Value("${Content-Language}")
	private String headerContentLanguageValue;
	
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry
			.addMapping(internalPathName + "**")
			.allowCredentials(true)
			.allowedOrigins(corsAllowedOrigins)
			.allowedMethods(allowedMethods.split(","))
			.allowedHeaders("Authorization", "Cache-Control", "Content-Type", "Accept")
			.maxAge(corsRegistryMaxAge);
	}
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/dist/assets/**")
			.addResourceLocations("classpath:/dist/assets/");
	}
	
	//	@Bean //Filters don't have to be beans
	public FilterRegistrationBean<ResponseHeadersInternalFilter> filterRegistrationBean() {
		FilterRegistrationBean<ResponseHeadersInternalFilter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(new ResponseHeadersInternalFilter(headerAllowValue, headerContentLanguageValue));
		registrationBean.addUrlPatterns("/internal/*");
		
		return registrationBean;
	}
	
	@Bean
	public LayoutDialect layoutDialect() {
		return new LayoutDialect();
	}
	
/*
	@Bean
	public SpringTemplateEngine templateEngine() {
		SpringTemplateEngine templateEngine = new SpringTemplateEngine();
		templateEngine.addDialect(layoutDialect());
		return templateEngine;
	}
*/
}
