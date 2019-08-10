package internal.exceptions;

import org.springframework.http.HttpStatus;

public class IllegalArguments extends WorkshopException {
	
	/**
	 * @see WorkshopException#WorkshopException(String)
	 */
	public IllegalArguments(String message) {
		super(message);
	}
	
	/**
	 * @see WorkshopException#WorkshopException(String, Throwable)
	 */
	public IllegalArguments(String message, Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * @see WorkshopException#WorkshopException(String, HttpStatus)
	 */
	public IllegalArguments(String message, HttpStatus httpStatusCode) {
		super(message, httpStatusCode);
	}
	
	/**
	 * @see WorkshopException#WorkshopException(String, String, org.springframework.http.HttpStatus)
	 */
	public IllegalArguments(String message, String messageSourceKey, HttpStatus httpStatusCode) {
		super(message, messageSourceKey, httpStatusCode);
	}
	
	/**
	 * @see WorkshopException#WorkshopException(String, HttpStatus, Throwable)
	 */
	public IllegalArguments(String message, HttpStatus httpStatusCode, Throwable cause) {
		super(message, httpStatusCode, cause);
	}
	
	/**
	 * @see WorkshopException#WorkshopException(String, String, HttpStatus, Throwable)
	 */
	public IllegalArguments(String message, String messageSourceKey, HttpStatus httpStatusCode, Throwable cause) {
		super(message, messageSourceKey, httpStatusCode, cause);
	}
	
	/**
	 * @see WorkshopException#WorkshopException(String, HttpStatus, String)
	 */
	public IllegalArguments(String message, HttpStatus httpStatusCode, String localizedMessage) {
		super(message, httpStatusCode, localizedMessage);
	}
	
	/**
	 * @see WorkshopException#WorkshopException(String, HttpStatus, String, Throwable)
	 */
	public IllegalArguments(String message, HttpStatus httpStatusCode, String localizedMessage, Throwable cause) {
		super(message, httpStatusCode, localizedMessage, cause);
	}
}
