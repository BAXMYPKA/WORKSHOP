package workshop.configurations;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.MimeMappings;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmbeddedServerConfiguration implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {
	
	@Override
	public void customize(TomcatServletWebServerFactory factory) {
		MimeMappings mappings = new MimeMappings(MimeMappings.DEFAULT);
		mappings.add("es5", "application/javascript");
		mappings.add("es6", "application/javascript");
		factory.setMimeMappings(mappings);
	}
}
