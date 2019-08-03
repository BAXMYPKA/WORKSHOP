package internal.exceptions;

/**
 * The common exception for passing the i118n of unsuccessful DELETE-UPDATE-CREATE to the end users of the Workshop
 * for any CRUD failures.
 * Also it is advisable to set a desirable HttpStatus code for your kind of error to be sent to a final service user.
 */
public class PersistenceFailure extends WorkshopException {
	
	public PersistenceFailure(String message, Throwable cause, org.springframework.http.HttpStatus httpStatus, String localizedMessage, String messageSourceKey) {
		super(message, cause, httpStatus, localizedMessage, messageSourceKey);
	}
	
	public PersistenceFailure(String message) {
		super(message);
	}
	
	public PersistenceFailure(String message, Throwable cause) {
		super(message, cause);
	}
	
	public PersistenceFailure(String message, org.springframework.http.HttpStatus httpStatusCode) {
		super(message, httpStatusCode);
	}
	
	public PersistenceFailure(String message, org.springframework.http.HttpStatus httpStatusCode, Throwable cause) {
		super(message, httpStatusCode, cause);
	}
	
	public PersistenceFailure(String message, String messageSourceKey, org.springframework.http.HttpStatus httpStatusCode, Throwable cause) {
		super(message, messageSourceKey, httpStatusCode, cause);
	}
	
	public PersistenceFailure(String message, org.springframework.http.HttpStatus httpStatusCode, String localizedMessage) {
		super(message, httpStatusCode, localizedMessage);
	}
	
	public PersistenceFailure(String message, org.springframework.http.HttpStatus httpStatusCode, String localizedMessage, Throwable cause) {
		super(message, httpStatusCode, localizedMessage, cause);
	}
}
