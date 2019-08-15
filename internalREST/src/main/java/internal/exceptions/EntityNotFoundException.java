package internal.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Self explanatory exception. Being caught, it will be sent to a final user of the services with the HttpStatus code 404,
 * but you are free to attach another one on your choice.
 */
public class EntityNotFoundException extends WorkshopException {
	
	/**
	 * @see WorkshopException#WorkshopException(String)
	 */
	public EntityNotFoundException(String message) {
		super(message);
	}
	
	/**
	 * @see WorkshopException#WorkshopException(String, Throwable)
	 */
	public EntityNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
	
	
	/**
	 * @see WorkshopException#WorkshopException(String, String, org.springframework.http.HttpStatus)
	 */
	public EntityNotFoundException(String message, String messageSourceKey, org.springframework.http.HttpStatus httpStatusCode) {
		super(message, messageSourceKey, httpStatusCode);
	}
	
	/**
	 * @see WorkshopException#WorkshopException(String, HttpStatus)
	 */
	public EntityNotFoundException(String message, HttpStatus httpStatusCode) {
		super(message, httpStatusCode);
	}
	
	/**
	 * @see WorkshopException#WorkshopException(String, org.springframework.http.HttpStatus, Throwable)
	 */
	public EntityNotFoundException(String message, HttpStatus httpStatusCode, Throwable cause) {
		super(message, httpStatusCode, cause);
	}
	
	/**
	 * @see WorkshopException#WorkshopException(String, String, HttpStatus, Throwable)
	 */
	public EntityNotFoundException(String message, String messageSourceKey, HttpStatus httpStatusCode, Throwable cause) {
		super(message, messageSourceKey, httpStatusCode, cause);
	}
	
	/**
	 * @see WorkshopException#WorkshopException(String, HttpStatus, String)
	 */
	public EntityNotFoundException(String message, HttpStatus httpStatusCode, String localizedMessage) {
		super(message, httpStatusCode, localizedMessage);
	}
	
	/**
	 * @see WorkshopException#WorkshopException(String, HttpStatus, String, Throwable)
	 */
	public EntityNotFoundException(String message, HttpStatus httpStatusCode, String localizedMessage, Throwable cause) {
		super(message, httpStatusCode, localizedMessage, cause);
	}
}
