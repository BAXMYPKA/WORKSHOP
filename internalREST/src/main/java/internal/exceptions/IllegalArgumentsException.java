package internal.exceptions;

import org.springframework.http.HttpStatus;

public class IllegalArgumentsException extends WorkshopException {
	
	/**
	 * @see WorkshopException#WorkshopException(String)
	 */
	public IllegalArgumentsException(String message) {
		super(message);
	}
	
	/**
	 * @see WorkshopException#WorkshopException(String, Throwable)
	 */
	public IllegalArgumentsException(String message, Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * @see WorkshopException#WorkshopException(String, HttpStatus)
	 */
	public IllegalArgumentsException(String message, HttpStatus httpStatusCode) {
		super(message, httpStatusCode);
	}
	
	/**
	 * @see WorkshopException#WorkshopException(String, String, org.springframework.http.HttpStatus)
	 */
	public IllegalArgumentsException(String message, String messageSourceKey, HttpStatus httpStatusCode) {
		super(message, messageSourceKey, httpStatusCode);
	}
	
	/**
	 * @see WorkshopException#WorkshopException(String, HttpStatus, Throwable)
	 */
	public IllegalArgumentsException(String message, HttpStatus httpStatusCode, Throwable cause) {
		super(message, httpStatusCode, cause);
	}
	
	/**
	 * @see WorkshopException#WorkshopException(String, String, HttpStatus, Throwable)
	 */
	public IllegalArgumentsException(String message, String messageSourceKey, HttpStatus httpStatusCode, Throwable cause) {
		super(message, messageSourceKey, httpStatusCode, cause);
	}
	
	/**
	 * @see WorkshopException#WorkshopException(String, HttpStatus, String)
	 */
	public IllegalArgumentsException(String message, HttpStatus httpStatusCode, String localizedMessage) {
		super(message, httpStatusCode, localizedMessage);
	}
	
	/**
	 * @see WorkshopException#WorkshopException(String, HttpStatus, String, Throwable)
	 */
	public IllegalArgumentsException(String message, HttpStatus httpStatusCode, String localizedMessage, Throwable cause) {
		super(message, httpStatusCode, localizedMessage, cause);
	}
}
