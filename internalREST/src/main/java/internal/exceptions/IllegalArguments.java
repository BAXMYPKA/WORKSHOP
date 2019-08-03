package internal.exceptions;

import org.springframework.http.HttpStatus;

public class IllegalArguments extends WorkshopException {
	public IllegalArguments(String message, Throwable cause, org.springframework.http.HttpStatus httpStatus, String localizedMessage, String messageSourceKey) {
		super(message, cause, httpStatus, localizedMessage, messageSourceKey);
	}
	
	public IllegalArguments(String message) {
		super(message);
	}
	
	public IllegalArguments(String message, Throwable cause) {
		super(message, cause);
	}
	
	public IllegalArguments(String message, org.springframework.http.HttpStatus httpStatusCode) {
		super(message, httpStatusCode);
	}
	
	public IllegalArguments(String message, org.springframework.http.HttpStatus httpStatusCode, Throwable cause) {
		super(message, httpStatusCode, cause);
	}
	
	public IllegalArguments(String message, String messageSourceKey, org.springframework.http.HttpStatus httpStatusCode, Throwable cause) {
		super(message, messageSourceKey, httpStatusCode, cause);
	}
	
	public IllegalArguments(String message, org.springframework.http.HttpStatus httpStatusCode, String localizedMessage) {
		super(message, httpStatusCode, localizedMessage);
	}
	
	public IllegalArguments(String message, org.springframework.http.HttpStatus httpStatusCode, String localizedMessage, Throwable cause) {
		super(message, httpStatusCode, localizedMessage, cause);
	}
}
