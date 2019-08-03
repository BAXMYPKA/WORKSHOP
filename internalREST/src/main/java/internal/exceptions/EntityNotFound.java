package internal.exceptions;

/**
 * Self explanatory exception. Being caught, it will be sent to a final user of the service with the HttpStatus code 404,
 * but you are free to attach another one on your choice.
 */
public class EntityNotFound extends WorkshopException {
	
	public EntityNotFound(String message, Throwable cause, org.springframework.http.HttpStatus httpStatus, String localizedMessage, String messageSourceKey) {
		super(message, cause, httpStatus, localizedMessage, messageSourceKey);
	}
	
	public EntityNotFound(String message) {
		super(message);
	}
	
	public EntityNotFound(String message, Throwable cause) {
		super(message, cause);
	}
	
	public EntityNotFound(String message, org.springframework.http.HttpStatus httpStatusCode) {
		super(message, httpStatusCode);
	}
	
	public EntityNotFound(String message, org.springframework.http.HttpStatus httpStatusCode, Throwable cause) {
		super(message, httpStatusCode, cause);
	}
	
	public EntityNotFound(String message, String messageSourceKey, org.springframework.http.HttpStatus httpStatusCode, Throwable cause) {
		super(message, messageSourceKey, httpStatusCode, cause);
	}
	
	public EntityNotFound(String message, org.springframework.http.HttpStatus httpStatusCode, String localizedMessage) {
		super(message, httpStatusCode, localizedMessage);
	}
	
	public EntityNotFound(String message, org.springframework.http.HttpStatus httpStatusCode, String localizedMessage, Throwable cause) {
		super(message, httpStatusCode, localizedMessage, cause);
	}
}
