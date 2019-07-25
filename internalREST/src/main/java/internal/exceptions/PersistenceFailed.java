package internal.exceptions;

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
