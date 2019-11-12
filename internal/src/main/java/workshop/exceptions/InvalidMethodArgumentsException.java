package workshop.exceptions;

import lombok.Getter;
import org.springframework.validation.BindingResult;

/**
 * RuntimeException class for holding and passing BindingResults validation errors after @Validated
 */
public class InvalidMethodArgumentsException extends IllegalArgumentsException {
	
	@Getter
	private BindingResult bindingResult;
	
	public InvalidMethodArgumentsException(String message) {
		super(message);
	}
	
	public InvalidMethodArgumentsException(String message, BindingResult bindingResult) {
		super(message);
		this.bindingResult = bindingResult;
	}
	
	public InvalidMethodArgumentsException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public InvalidMethodArgumentsException(String message, org.springframework.http.HttpStatus httpStatusCode) {
		super(message, httpStatusCode);
	}
	
	public InvalidMethodArgumentsException(String message, String messageSourceKey, org.springframework.http.HttpStatus httpStatusCode) {
		super(message, messageSourceKey, httpStatusCode);
	}
	
	public InvalidMethodArgumentsException(String message, org.springframework.http.HttpStatus httpStatusCode, Throwable cause) {
		super(message, httpStatusCode, cause);
	}
	
	public InvalidMethodArgumentsException(String message, String messageSourceKey, org.springframework.http.HttpStatus httpStatusCode, Throwable cause) {
		super(message, messageSourceKey, httpStatusCode, cause);
	}
	
	public InvalidMethodArgumentsException(String message, org.springframework.http.HttpStatus httpStatusCode, String localizedMessage) {
		super(message, httpStatusCode, localizedMessage);
	}
	
	public InvalidMethodArgumentsException(String message, org.springframework.http.HttpStatus httpStatusCode, String localizedMessage, Throwable cause) {
		super(message, httpStatusCode, localizedMessage, cause);
	}
}
