package workshop;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "workshop")
@EntityScan(basePackages = "workshop.internal.entities")
@EnableJpaRepositories(basePackages = "workshop.internal.dao")
@EnableCaching
@PropertySources({@PropertySource(value = "classpath:applicationTest.properties", encoding = "UTF-8")})
public class SecurityTestApplication {
}
