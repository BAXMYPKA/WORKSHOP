package internal.exceptions;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

/**
 * Final frontier exceptions.
 * {@link #getMessage()} first is used for the logging purposes. Second, can be exposed to the end Users.
 * {@link #getLocalizedMessage()} is used as the ready to be shown to the end Users according their Locales
 * {@link #getMessageSourceKey()} is the key into the Spring MessageSource localization messages for the end Users
 * according their Locales.
 * {@link #getHttpStatus()} is used to determine a special HttpStatus to be sent with a response to the end Users.
 */
public abstract class WorkshopException extends RuntimeException {
	
	@Getter
	private HttpStatus HttpStatus;
	@Setter
	private String localizedMessage;
	@Getter
	@Setter
	private String messageSourceKey;
	
	public WorkshopException(String message, Throwable cause, org.springframework.http.HttpStatus httpStatus, String localizedMessage, String messageSourceKey) {
		super(message, cause);
		HttpStatus = httpStatus;
		this.localizedMessage = localizedMessage;
		this.messageSourceKey = messageSourceKey;
	}
	
	/**
	 * @param message First is used for the logging purposes. Second, can be exposed to the end Users.
	 */
	public WorkshopException(String message) {
		super(message);
	}
	
	/**
	 * @param message {First is used for the logging purposes. Second, can be exposed to the end Users.
	 */
	public WorkshopException(String message, Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * @param message        First is used for the logging purposes. Second, can be exposed to the end Users.
	 * @param httpStatusCode To indicate the desirable HttpStatus code for this kind of exception to be returned to a client
	 */
	public WorkshopException(String message, HttpStatus httpStatusCode) {
		super(message);
		this.HttpStatus = httpStatusCode;
	}
	
	/**
	 * @param message        First is used for the logging purposes. Second, can be exposed to the end Users.
	 * @param httpStatusCode To indicate the desirable HttpStatus code for this kind of exception to be returned to a client
	 * @param cause          Only for inner logging use.
	 */
	public WorkshopException(String message, HttpStatus httpStatusCode, Throwable cause) {
		super(message, cause);
		this.HttpStatus = httpStatusCode;
	}
	
	/**
	 * @param message          First is used for the logging purposes. Second, can be exposed to the end Users.
	 * @param messageSourceKey The key from messages.properties to be used for exposing its localized value
	 *                         (along with Locale.class) to the end User.
	 *                         If presented, it will be automatically translated according end User's Locale and
	 *                         inserted into the HttpResponse.
	 * @param httpStatusCode   To indicate the desirable HttpStatus code for this kind of exception to be returned to a client
	 * @param cause            Only for inner logging use.
	 */
	public WorkshopException(String message, String messageSourceKey, HttpStatus httpStatusCode, Throwable cause) {
		super(message, cause);
		this.HttpStatus = httpStatusCode;
		this.messageSourceKey = messageSourceKey;
	}
	
	/**
	 * @param message          First is used for the logging purposes. Second, can be exposed to the end Users.
	 * @param httpStatusCode   To indicate the desirable HttpStatus code for this kind of exception to be returned to a client
	 * @param localizedMessage The fully localized message to be exposed to the end Users according to their Locales.
	 *                         If presented, it will be automatically inserted into the end User's HttpResponse.
	 */
	public WorkshopException(String message, HttpStatus httpStatusCode, String localizedMessage) {
		super(message);
		this.HttpStatus = httpStatusCode;
		this.localizedMessage = localizedMessage;
	}
	
	/**
	 * @param message          First is used for the logging purposes. Second, can be exposed to the end Users.
	 * @param httpStatusCode   To indicate the desirable HttpStatus code for this kind of exception to be returned to a client
	 * @param localizedMessage The fully localized message to be exposed to the end Users according to their Locales.
	 *                         If presented, it will be automatically inserted into the end User's HttpResponse
	 */
	public WorkshopException(String message, HttpStatus httpStatusCode, String localizedMessage, Throwable cause) {
		super(message, cause);
		this.HttpStatus = httpStatusCode;
		this.localizedMessage = localizedMessage;
	}
	
	/**
	 * @return The fully localized message to be exposed to the end Users according to their Locales.
	 * If presented, it will be automatically inserted into the end User's HttpResponse
	 */
	@Override
	public String getLocalizedMessage() {
		return localizedMessage;
	}
}
