package workshop.exceptions;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import workshop.internal.entities.Uuid;
import workshop.security.UsernamePasswordUuidAuthenticationToken;

import java.util.Locale;

/**
 * To be thrown in the {@link workshop.security.UsersDetailsService#authenticateNewUserByUuid(UsernamePasswordUuidAuthenticationToken)}
 * and bubbled up by LoginAuthenticationFilter.doFilter(ServletRequest, ServletResponse, FilterChain)
 * to inform that the given UUID doesn't math the new {@link workshop.internal.entities.User} who is trying to login
 * and confirm the registration for the first time with it.
 */

public class UuidAuthenticationException extends WorkshopException {
	
	/**
	 * If the found {@link Uuid} is valid (not outdated etc) we have to inform the
	 * {@link workshop.controllers.external.ExternalLoginController#getLoginRegistrationConfirmation(String, Model, Locale, RedirectAttributes, Authentication)}
	 * that {@link workshop.internal.entities.User} just has entered wrong credentials and has to try the first time
	 * login again to be confirmed and activated.
	 */
	@Setter
	@Getter
	private Uuid validUuid;
	
	public UuidAuthenticationException(String message, Throwable cause, org.springframework.http.HttpStatus httpStatus, String localizedMessage, String messageSourceKey) {
		super(message, cause, httpStatus, localizedMessage, messageSourceKey);
	}
	
	public UuidAuthenticationException(String message) {
		super(message);
	}
	
	public UuidAuthenticationException(String message, Uuid validUuid) {
		super(message);
		this.validUuid = validUuid;
	}
	
	public UuidAuthenticationException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public UuidAuthenticationException(String message, org.springframework.http.HttpStatus httpStatusCode) {
		super(message, httpStatusCode);
	}
	
	public UuidAuthenticationException(String message, String messageSourceKey, org.springframework.http.HttpStatus httpStatusCode) {
		super(message, messageSourceKey, httpStatusCode);
	}
	
	public UuidAuthenticationException(String message, org.springframework.http.HttpStatus httpStatusCode, Throwable cause) {
		super(message, httpStatusCode, cause);
	}
	
	public UuidAuthenticationException(String message, String messageSourceKey, org.springframework.http.HttpStatus httpStatusCode, Throwable cause) {
		super(message, messageSourceKey, httpStatusCode, cause);
	}
	
	public UuidAuthenticationException(String message, org.springframework.http.HttpStatus httpStatusCode, String localizedMessage) {
		super(message, httpStatusCode, localizedMessage);
	}
	
	public UuidAuthenticationException(String message, org.springframework.http.HttpStatus httpStatusCode, String localizedMessage, Throwable cause) {
		super(message, httpStatusCode, localizedMessage, cause);
	}
}
