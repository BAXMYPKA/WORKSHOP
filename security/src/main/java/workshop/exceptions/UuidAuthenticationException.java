package workshop.exceptions;

import lombok.Getter;
import lombok.Setter;

/**
 * To be thrown in the UsersAuthenticationProvider.authenticateUuid(UsernamePasswordUuidAuthenticationToken)
 * and bubbled up by LoginAuthenticationFilter.doFilter(ServletRequest, ServletResponse, FilterChain)
 *  to inform that the given UUID doesn't math the new {@link workshop.internal.entities.User} who is trying to login
 *  and confirm the registration for the first time with it.
 */

public class UuidAuthenticationException extends WorkshopException {
	
	//TODO: to fill up
	/**
	 *
	 */
	@Setter
	@Getter
	private boolean isUuidValid;
	
	public UuidAuthenticationException(String message, Throwable cause, org.springframework.http.HttpStatus httpStatus, String localizedMessage, String messageSourceKey) {
		super(message, cause, httpStatus, localizedMessage, messageSourceKey);
	}
	
	public UuidAuthenticationException(String message) {
		super(message);
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
