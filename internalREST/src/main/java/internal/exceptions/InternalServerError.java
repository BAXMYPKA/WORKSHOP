package internal.exceptions;

import org.springframework.http.HttpStatus;

public class InternalServerError extends WorkshopException {
	public InternalServerError(String message, Throwable cause, org.springframework.http.HttpStatus httpStatus, String localizedMessage, String messageSourceKey) {
		super(message, cause, httpStatus, localizedMessage, messageSourceKey);
	}
	
	public InternalServerError(String message) {
		super(message);
	}
	
	public InternalServerError(String message, Throwable cause) {
		super(message, cause);
		
	}
	/**
	 * @see WorkshopException#WorkshopException(String, String, HttpStatus)
	 */
	public InternalServerError(String message, String messageSourceKey, HttpStatus httpStatusCode) {
		super(message, messageSourceKey, httpStatusCode);
	}
	
	public InternalServerError(String message, org.springframework.http.HttpStatus httpStatusCode) {
		super(message, httpStatusCode);
	}
	
	public InternalServerError(String message, org.springframework.http.HttpStatus httpStatusCode, Throwable cause) {
		super(message, httpStatusCode, cause);
	}
	
	
	/**
	 * @see WorkshopException#WorkshopException(String, String, org.springframework.http.HttpStatus, Throwable)
	 */
	public InternalServerError(String message, String messageSourceKey, org.springframework.http.HttpStatus httpStatusCode, Throwable cause) {
		super(message, messageSourceKey, httpStatusCode, cause);
	}
	
	public InternalServerError(String message, org.springframework.http.HttpStatus httpStatusCode, String localizedMessage) {
		super(message, httpStatusCode, localizedMessage);
	}
	
	public InternalServerError(String message, org.springframework.http.HttpStatus httpStatusCode, String localizedMessage, Throwable cause) {
		super(message, httpStatusCode, localizedMessage, cause);
	}
}
