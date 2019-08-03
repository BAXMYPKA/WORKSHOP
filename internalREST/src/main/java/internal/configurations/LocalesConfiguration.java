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

/**
 * To determine a User's Locale:
 * 1) The Cookie with "lang" parameter will be examined.
 * 2) If nothing found, 'Accept-Language' header will be used.
 * 	(For this happen don't 'setDefaultLocale()' on the LocaleResolver!)
 * 3) If nothing was found or User's Locales are unsupported, Default messages (english) will be used.
 */
@Configuration
public class LocalesConfiguration implements WebMvcConfigurer {
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(this.localeChangeInterceptor());
	}
	
	/**
	 * @return implementation that uses a cookie sent back to the user in case of a custom setting,
	 * with a fallback to the specified default locale or the request's accept-header locale
	 */
	@Bean(name = "localeResolver")
	public LocaleResolver localeResolver() {
		CookieLocaleResolver cookieLocaleResolver = new CookieLocaleResolver();
		cookieLocaleResolver.setCookieName("lang");
		cookieLocaleResolver.setCookieMaxAge(60 * 60 * 24 * 30);//30 days
		cookieLocaleResolver.setCookieHttpOnly(true);
		return cookieLocaleResolver;
	}
	
/*
	@Bean(name = "localeResolver")
	public LocaleResolver acceptHeaderLocaleResolver() {
		AcceptHeaderLocaleResolver headerLocaleResolver = new AcceptHeaderLocaleResolver();
		headerLocaleResolver.setDefaultLocale(Locale.forLanguageTag("ru"));
		headerLocaleResolver.setSupportedLocales(Arrays.asList(
			Locale.forLanguageTag("ru"),
			Locale.ENGLISH));
		return headerLocaleResolver;
	}
*/
	
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
