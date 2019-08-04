package internal.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Self explanatory exception. Being caught, it will be sent to a final user of the service with the HttpStatus code 404,
 * but you are free to attach another one on your choice.
 */
public class EntityNotFound extends WorkshopException {
	
	/**
	 * @see WorkshopException#WorkshopException(String)
	 */
	public EntityNotFound(String message) {
		super(message);
	}
	
	/**
	 * @see WorkshopException#WorkshopException(String, Throwable)
	 */
	public EntityNotFound(String message, Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * @see WorkshopException#WorkshopException(String, HttpStatus)
	 */
	public EntityNotFound(String message, HttpStatus httpStatusCode) {
		super(message, httpStatusCode);
	}
	
	/**
	 * @see WorkshopException#WorkshopException(String, org.springframework.http.HttpStatus, Throwable)
	 */
	public EntityNotFound(String message, HttpStatus httpStatusCode, Throwable cause) {
		super(message, httpStatusCode, cause);
	}
	
	/**
	 * @see WorkshopException#WorkshopException(String, String, HttpStatus, Throwable)
	 */
	public EntityNotFound(String message, String messageSourceKey, HttpStatus httpStatusCode, Throwable cause) {
		super(message, messageSourceKey, httpStatusCode, cause);
	}
	
	/**
	 * @see WorkshopException#WorkshopException(String, HttpStatus, String)
	 */
	public EntityNotFound(String message, HttpStatus httpStatusCode, String localizedMessage) {
		super(message, httpStatusCode, localizedMessage);
	}
	
	/**
	 * @see WorkshopException#WorkshopException(String, HttpStatus, String, Throwable)
	 */
	public EntityNotFound(String message, HttpStatus httpStatusCode, String localizedMessage, Throwable cause) {
		super(message, httpStatusCode, localizedMessage, cause);
	}
}
