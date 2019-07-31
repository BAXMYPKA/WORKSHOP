package internal.exceptions;

import org.springframework.http.HttpStatus;

/**
 * The common exception for passing the i118n of unsuccessful DELETE-UPDATE-CREATE to the end users of the Workshop
 * for any CRUD failures.
 * Also it is advisable to set a desirable HttpStatus code for your kind of error to be sent to a final service user.
 */
public class PersistenceFailure extends WorkshopException {
	
	public PersistenceFailure(String message) {
		super(message);
	}
	
	public PersistenceFailure(String message, Throwable cause) {
		super(message, cause);
	}
	
	public PersistenceFailure(Throwable cause) {
		super(cause);
	}
	
	public PersistenceFailure(String message, org.springframework.http.HttpStatus httpStatusCode) {
		super(message, httpStatusCode);
	}
	
	public PersistenceFailure(String message, org.springframework.http.HttpStatus httpStatusCode, Throwable cause) {
		super(message, httpStatusCode, cause);
	}
	
	public PersistenceFailure(String message, String messageSourceProperty, org.springframework.http.HttpStatus httpStatusCode, Throwable cause) {
		super(message, messageSourceProperty, httpStatusCode, cause);
	}
	
	public PersistenceFailure(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
