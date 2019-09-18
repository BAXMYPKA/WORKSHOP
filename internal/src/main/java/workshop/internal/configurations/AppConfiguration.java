package workshop.internal.configurations;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@Configuration
@EnableCaching
@PropertySources({
	@PropertySource(value = "classpath:configs/workshop.properties", encoding = "UTF-8")})
public class AppConfiguration {

}
