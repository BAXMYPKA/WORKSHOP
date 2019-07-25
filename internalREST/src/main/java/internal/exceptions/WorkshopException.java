package internal.exceptions;

public abstract class WorkshopException extends RuntimeException {
	
	public WorkshopException(String message) {
		super(message);
	}
	
	public WorkshopException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public WorkshopException(Throwable cause) {
		super(cause);
	}
	
	public WorkshopException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
