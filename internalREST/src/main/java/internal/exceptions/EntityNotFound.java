package internal.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Self explanatory exception. Being caught, it will be sent to a final user of the service with the HttpStatus code 404,
 * but you are free to attach another one on your choice.
 */
public class EntityNotFound extends WorkshopException {
	
	public EntityNotFound(String message) {
		super(message);
	}
	
	public EntityNotFound(String message, Throwable cause) {
		super(message, cause);
	}
	
	public EntityNotFound(Throwable cause) {
		super(cause);
	}
	
	public EntityNotFound(String message, org.springframework.http.HttpStatus httpStatusCode) {
		super(message, httpStatusCode);
	}
	
	public EntityNotFound(String message, String messageSourceProperty, org.springframework.http.HttpStatus httpStatusCode, Throwable cause) {
		super(message, messageSourceProperty, httpStatusCode, cause);
	}
	
	public EntityNotFound(String message, org.springframework.http.HttpStatus httpStatusCode, Throwable cause) {
		super(message, httpStatusCode, cause);
	}
	
	public EntityNotFound(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
