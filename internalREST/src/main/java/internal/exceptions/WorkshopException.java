package internal.exceptions;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

/**
 * Runtime exceptions family to be used as the final exceptions to be logged {@link #getMessage()}
 * before exposing {@link #getLocalizedMessage()} or give the {@link #getMessageSourceKey()} for its localized value
 * for the end User according their Locales.
 * Constructor with parameters (String message, String messageSourceKey, HttpStatus httpStatusCode, Throwable cause)
 * is highly recommended.
 */
public abstract class WorkshopException extends RuntimeException {
	
	@Getter
	private HttpStatus HttpStatus;
	@Setter
	private String localizedMessage;
	@Getter
	@Setter
	private String messageSourceKey;
	
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
	 * @param message        To be used for logging purposes.
	 * @param httpStatusCode To indicate the desirable HttpStatus code for this kind of exception to be returned to a client
	 */
	public WorkshopException(String message, HttpStatus httpStatusCode) {
		super(message);
		this.HttpStatus = httpStatusCode;
	}
	
	/**
	 * @param message        To be used for logging purposes.
	 * @param httpStatusCode To indicate the desirable HttpStatus code for this kind of exception to be returned to a client
	 * @param cause          Only for inner logging use.
	 */
	public WorkshopException(String message, HttpStatus httpStatusCode, Throwable cause) {
		super(message, cause);
		this.HttpStatus = httpStatusCode;
	}
	
	/**
	 * @param message               To be used for logging purposes.
	 * @param messageSourceKey The key from messages.properties to be used for exposing its localized value
	 *                              (along with Locale.class) to the end User.
	 * @param httpStatusCode        To indicate the desirable HttpStatus code for this kind of exception to be returned to a client
	 * @param cause                 Only for inner logging use.
	 */
	public WorkshopException(String message, String messageSourceKey, HttpStatus httpStatusCode, Throwable cause) {
		super(message, cause);
		this.HttpStatus = httpStatusCode;
		this.messageSourceKey = messageSourceKey;
	}

//	/**
//	 * @param message This message will be exposed to a final client with a response.
//	 * @param localizedMessage This can be used for exposing the localized message to the end user according to their Locales
//	 * @param httpStatusCode To indicate the desirable HttpStatus code for this kind of exception to be returned to a client
//	 * @param cause Only for inner logging use.
//	 */
/*
	public WorkshopException(String message, String localizedMessage, HttpStatus httpStatusCode, Throwable cause) {
		super(message, cause);
		this.HttpStatus = httpStatusCode;
		this.localizedMessage = localizedMessage;
	}
*/
	
	public WorkshopException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
	@Override
	public String getLocalizedMessage() {
		return localizedMessage;
	}
}
