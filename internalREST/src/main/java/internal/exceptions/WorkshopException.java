package internal.exceptions;

import lombok.Getter;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;

public abstract class WorkshopException extends RuntimeException {
	
	@Getter
	private HttpStatus HttpStatus;
	
	public WorkshopException(String message) {
		super(message);
	}
	
	public WorkshopException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public WorkshopException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * @param message This message will be exposed to a final client with a response.
	 * @param httpStatusCode To indicate the desirable HttpStatus code for this kind of exception to be returned to a client
	 */
	public WorkshopException(String message, HttpStatus httpStatusCode) {
		super(message);
		this.HttpStatus = httpStatusCode;
	}
	
	/**
	 * @param message This message will be exposed to a final client with a response.
	 * @param httpStatusCode To indicate the desirable HttpStatus code for this kind of exception to be returned to a client
	 * @param cause Only for inner logging use.
	 */
	public WorkshopException(String message, HttpStatus httpStatusCode, Throwable cause) {
		super(message, cause);
		this.HttpStatus = httpStatusCode;
	}
	
	public WorkshopException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
