package workshop;

import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.sql.SQLException;
import java.util.Collections;

@SpringBootApplication(scanBasePackages = {"workshop"})
@EntityScan(basePackages = "workshop.internal.entities")
@EnableJpaRepositories(basePackages = "workshop.internal.dao")
@EnableCaching
@PropertySources({
	@PropertySource(value = "classpath:configs/workshop.properties", encoding = "UTF-8")})
public class WorkshopApplication {
	
	@Value("${h2.port}") //From 'application.properties as the custom property
	private String h2port;
	
	public static void main(String[] args) {
		SpringApplication.run(WorkshopApplication.class, args);
	}
	
	@Bean(initMethod = "start", destroyMethod = "stop")
	public Server h2Server() throws SQLException {
		return Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", h2port);
	}
}
