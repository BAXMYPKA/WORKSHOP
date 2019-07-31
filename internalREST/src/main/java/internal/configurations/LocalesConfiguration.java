package internal.configurations;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.util.Arrays;
import java.util.Locale;

@Configuration
public class LocalesConfiguration implements WebMvcConfigurer {
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(this.localeChangeInterceptor());
	}
	
/*
	@Bean(name = "localeResolver")
	public LocaleResolver localeResolver() {
		CookieLocaleResolver cookieLocaleResolver = new CookieLocaleResolver();
		cookieLocaleResolver.setDefaultLocale(Locale.forLanguageTag("ru"));
		cookieLocaleResolver.setCookieName("lang");
		cookieLocaleResolver.setCookieMaxAge(60 * 60 * 24 * 7);
		cookieLocaleResolver.setCookieHttpOnly(false);
		return cookieLocaleResolver;
	}
*/
	
	@Bean(name = "localeResolver")
	public LocaleResolver acceptHeaderLocaleResolver() {
		AcceptHeaderLocaleResolver headerLocaleResolver = new AcceptHeaderLocaleResolver();
		headerLocaleResolver.setDefaultLocale(Locale.forLanguageTag("ru"));
		headerLocaleResolver.setSupportedLocales(Arrays.asList(
			Locale.forLanguageTag("ru"),
			Locale.ENGLISH));
		return headerLocaleResolver;
	}
	
	@Bean
	public LocaleChangeInterceptor localeChangeInterceptor() {
		LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
		localeChangeInterceptor.setParamName("lang");
		return localeChangeInterceptor;
	}
	
	@Bean
	public MessageSource messageSource() {
		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setBasename("classpath:i118n/internal");
		messageSource.setDefaultEncoding("UTF-8");
		messageSource.setCacheSeconds(60*30); //Half an hour
		return messageSource;
	}
	
}
