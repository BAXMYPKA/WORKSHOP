package internal.exceptions;

import org.springframework.http.HttpStatus;

/**
 * The common exception for passing the i118n of unsuccessful DELETE-UPDATE-CREATE to the end users of the Workshop
 * for any CRUD failures.
 * Also it is advisable to set a desirable HttpStatus code for your kind of error to be sent to a final service user.
 */
public class PersistenceFailure extends WorkshopException {
	
	/**
	 * @see WorkshopException#WorkshopException(String)
	 */
	public PersistenceFailure(String message) {
		super(message);
	}
	
	/**
	 * @see WorkshopException#WorkshopException(String, Throwable)
	 */
	public PersistenceFailure(String message, Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * @see WorkshopException#WorkshopException(String, org.springframework.http.HttpStatus)
	 */
	public PersistenceFailure(String message, HttpStatus httpStatusCode) {
		super(message, httpStatusCode);
	}
	
	/**
	 * @see WorkshopException#WorkshopException(String, org.springframework.http.HttpStatus, Throwable)
	 */
	public PersistenceFailure(String message, HttpStatus httpStatusCode, Throwable cause) {
		super(message, httpStatusCode, cause);
	}
	
	/**
	 * @see WorkshopException#WorkshopException(String, String, HttpStatus)
	 */
	public PersistenceFailure(String message, String messageSourceKey, HttpStatus httpStatusCode) {
		super(message, messageSourceKey, httpStatusCode);
	}
	
	/**
	 * @see WorkshopException#WorkshopException(String, String, HttpStatus, Throwable)
	 */
	public PersistenceFailure(String message, String messageSourceKey, HttpStatus httpStatusCode, Throwable cause) {
		super(message, messageSourceKey, httpStatusCode, cause);
	}
	
	/**
	 * @see WorkshopException#WorkshopException(String, HttpStatus, String)
	 */
	public PersistenceFailure(String message, HttpStatus httpStatusCode, String localizedMessage) {
		super(message, httpStatusCode, localizedMessage);
	}
	
	/**
	 * @see WorkshopException#WorkshopException(String, HttpStatus, String, Throwable)
	 */
	public PersistenceFailure(String message, HttpStatus httpStatusCode, String localizedMessage, Throwable cause) {
		super(message, httpStatusCode, localizedMessage, cause);
	}
}
