package internal.exceptions;

/**
 * The common exception for passing the messages of unsuccessful DELETE-UPDATE-CREATE to the end users of the Workshop
 * for any CRUD failures.
 */
public class PersistenceFailed extends WorkshopException {
	
	public PersistenceFailed(String message) {
		super(message);
	}
	
	public PersistenceFailed(String message, Throwable cause) {
		super(message, cause);
	}
	
	public PersistenceFailed(Throwable cause) {
		super(cause);
	}
	
	public PersistenceFailed(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
