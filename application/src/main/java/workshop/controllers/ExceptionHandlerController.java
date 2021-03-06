package workshop.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import workshop.controllers.utils.UserMessagesCreator;
import workshop.exceptions.*;
import workshop.internal.services.serviceUtils.JsonServiceUtils;

import javax.persistence.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Approaches differently for External and Internal parts of the application.
 *
 * For the Internal it should return MediaType.APPLICATION_JSON_UTF8 with a JsonObject as:
 * {"userMessage":"Localized exception message text."}
 * For the External it should either return the message as above and/or redirect User to another page.
 */
@Slf4j
@Getter
@Setter
@ControllerAdvice
public class ExceptionHandlerController {
	
	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private JsonServiceUtils jsonServiceUtils;
	
	@Autowired
	private UserMessagesCreator userMessagesCreator;
	
	@Value("${spring.servlet.multipart.max-request-size}")
	private String maxUploadImageSize;
	
	//TODO: to distinguish Internal and External errors treatment. E.g., org.springframework.dao.DataIntegrityViolationException
	
	@ExceptionHandler({HttpMessageConversionException.class})
	@ResponseBody
	public ResponseEntity<String> httpMessageNotReadableException(Exception ex, Locale locale) {
		if (ex.getClass().isAssignableFrom(HttpMessageNotReadableException.class)) {
			//400
			log.info(ex.getMessage(), ex);
			return getResponseEntityWithErrorMessage(
				HttpStatus.BAD_REQUEST,
				messageSource.getMessage("httpStatus.badRequest", null, locale));
		} else {
			// ex = HttpMessageNotWritableException.class, 422
			log.error(ex.getMessage(), ex);
			return getResponseEntityWithErrorMessage(
				HttpStatus.UNPROCESSABLE_ENTITY,
				messageSource.getMessage("httpStatus.unprocessableEntity.HttpMessageNotWritable", null, locale));
		}
	}
	
	@ExceptionHandler({HttpMediaTypeException.class})
	@ResponseBody
	public ResponseEntity<String> httpMediaTypeFailure(Exception ex, Locale locale) {
		if (ex.getClass().isAssignableFrom(HttpMediaTypeNotSupportedException.class)) {
			//415
			log.debug(ex.getMessage(), ex);
			return getResponseEntityWithErrorMessage(
				HttpStatus.UNSUPPORTED_MEDIA_TYPE,
				messageSource.getMessage("httpStatus.unsupportedMediaType", null, locale));
		} else {
			//MediaTypeNotAcceptable.class 406 Not Acceptable 
			log.debug(ex.getMessage(), ex);
			return getResponseEntityWithErrorMessage(
				HttpStatus.NOT_ACCEPTABLE,
				messageSource.getMessage("httpStatus.notAcceptable.mediaType", null, locale));
		}
	}
	
	@ExceptionHandler({JsonProcessingException.class}) //422
	public ResponseEntity<String> jsonProcessingFailure(Exception ex, Locale locale) {
		log.info(ex.getMessage());
		return getResponseEntityWithErrorMessage(
			HttpStatus.UNPROCESSABLE_ENTITY,
			messageSource.getMessage("httpStatus.unprocessableEntity.JsonException", null, locale));
	}
	
	@ExceptionHandler({AuthenticationCredentialsNotFoundException.class})
	public ResponseEntity<String> authenticationFailure(Exception ex) {
		log.info(ex.getMessage());
		return getResponseEntityWithErrorMessage(HttpStatus.UNAUTHORIZED, ex.getMessage());
	}
	
	/**
	 * Thrown if an Authentication object does not hold a required authority.
	 * @return HttpStatus.UNAUTHORIZED (401) with an error message,
	 */
	@ExceptionHandler({AccessDeniedException.class}) //401
	public ResponseEntity<String> accessDenied(AccessDeniedException ade) {
		log.debug(ade.getMessage(), ade);
		String unauthorizedMessageBody =
			getMessageSource().getMessage("httpStatus.unauthorized", null, LocaleContextHolder.getLocale());
		return getResponseEntityWithErrorMessage(HttpStatus.UNAUTHORIZED, unauthorizedMessageBody);
	}
	
	/**
	 * Produces 422 HttpStatus.UNPROCESSABLE_ENTITY with Json object as "fieldName : fieldErrorDescription"
	 *
	 * @param ex IllegalMethodArgumentException or MethodArgumentNotValidException
	 * @return Json object with fully described field errors as: "fieldName : fieldErrorDescription"
	 */
	@ExceptionHandler({InvalidMethodArgumentsException.class, MethodArgumentNotValidException.class}) //422
	@ResponseBody
	public ResponseEntity<String> validationFailure(
		InvalidMethodArgumentsException ex) {
		BindingResult bindingResult = ex.getBindingResult();
		Map<String, String> fieldErrors = bindingResult
			.getFieldErrors()
			.stream()
			.collect(Collectors.toMap(fieldError -> fieldError.getField(), fieldError -> fieldError.getDefaultMessage()));
		try {
			String jsonFieldErrors = jsonServiceUtils.getObjectMapper().writeValueAsString(fieldErrors);
			return getResponseEntityWithErrorMessage(HttpStatus.UNPROCESSABLE_ENTITY, jsonFieldErrors);
		} catch (JsonProcessingException e) {
			log.error(e.getMessage());
			return getResponseEntityWithErrorMessage(HttpStatus.UNPROCESSABLE_ENTITY, fieldErrors.toString());
		}
	}
	
	/**
	 * 1) First if WorkshopException.getLocalizedMessage() != null it will be used to be shown to the end Users.
	 * 2) Second if WorkshopException.getMessageSourceKey() != null the messageSource will be obtained by this key to
	 * be shown to the end Users according their Locales.
	 * 3) Last, WorkshopException.getMessage will be used for such a purpose.
	 *
	 * @param wx WorkshopException.class instance.
	 * @return Json "errorMessage"
	 */
	@ExceptionHandler({WorkshopException.class})
	@ResponseBody
	public ResponseEntity<String> catchWorkshopExceptions(
		WorkshopException wx, Locale locale, HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes) throws ServletException, IOException {
		log.info(wx.getMessage(), wx);
		
		String message;
		if (wx.getLocalizedMessage() != null) {
			message = wx.getLocalizedMessage();
		} else if (wx.getMessageSourceKey() != null) {
			message = messageSource.getMessage(wx.getMessageSourceKey(), null, locale);
		} else {
			message = wx.getMessage();
		}
		
		if (wx instanceof workshop.exceptions.EntityNotFoundException) {
			workshop.exceptions.EntityNotFoundException enf = (workshop.exceptions.EntityNotFoundException) wx;
			log.info(enf.getMessage(), enf);
			return getResponseEntityWithErrorMessage(
				enf.getHttpStatus() != null ? enf.getHttpStatus() : HttpStatus.NOT_FOUND,
				message);
			
		} else if (wx instanceof PersistenceFailureException) {
			PersistenceFailureException pf = (PersistenceFailureException) wx;
			log.info(pf.getMessage(), pf);
			return getResponseEntityWithErrorMessage(
				pf.getHttpStatus() != null ? pf.getHttpStatus() : HttpStatus.UNPROCESSABLE_ENTITY,
				message);
			
		} else if (wx instanceof InternalServerErrorException) {
			InternalServerErrorException ise = (InternalServerErrorException) wx;
			log.error(ise.getMessage(), ise);
			return getResponseEntityWithErrorMessage(
				ise.getHttpStatus() != null ? ise.getHttpStatus() : HttpStatus.INTERNAL_SERVER_ERROR,
				message);
			
		} else if (wx instanceof IllegalArgumentsException) {
			IllegalArgumentsException iae = (IllegalArgumentsException) wx;
			log.info(iae.getMessage(), iae);
			return getResponseEntityWithErrorMessage(
				iae.getHttpStatus() != null ? iae.getHttpStatus() : HttpStatus.UNPROCESSABLE_ENTITY,
				message);
			
		} else if (wx instanceof UuidAuthenticationException) {
			UuidAuthenticationException uuidex = (UuidAuthenticationException) wx;
			log.info(uuidex.getMessage(), uuidex);
			if (!request.getRequestURL().toString().contains("/internal")) {
				//TODO: to complete
				redirectAttributes.addAttribute("TEST", "TEST VALUE");
				request.setAttribute("TEST", "TEST VALUE");
				request.getRequestDispatcher("/login").forward(request, response);
			}
			return getResponseEntityWithErrorMessage(
				uuidex.getHttpStatus() != null ? uuidex.getHttpStatus() : HttpStatus.UNPROCESSABLE_ENTITY,
				message);
			
		}
		//In case of not enlisted instance has been caught
		return getResponseEntityWithErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR, wx.getMessage());
	}
	
	/**
	 * Instanceof check is only for logging the exact subclass of WorkshopException.
	 *
	 * @param exception PersistenceException or any subclass.
	 * @return
	 */
	@ExceptionHandler({PersistenceException.class})
	@ResponseBody
	public ResponseEntity<String> catchWorkshopExceptions(PersistenceException exception, Locale locale) {
		if (exception instanceof EntityExistsException) {
			log.debug(exception.getMessage(), exception);
			return getResponseEntityWithErrorMessage(HttpStatus.UNPROCESSABLE_ENTITY,
				messageSource.getMessage("error.entityIdExists", null, locale));
			
		} else if (exception instanceof javax.persistence.EntityNotFoundException) {
			log.debug(exception.getMessage(), exception);
			return getResponseEntityWithErrorMessage(HttpStatus.NOT_FOUND,
				messageSource.getMessage("message.entityNotFound", null, locale));
			
		} else if (exception instanceof NoResultException) {
			log.debug(exception.getMessage(), exception);
			return getResponseEntityWithErrorMessage(HttpStatus.NOT_FOUND,
				messageSource.getMessage("httpStatus.notFound", null, locale));
			
		} else if (exception instanceof RollbackException) {
			log.debug(exception.getMessage(), exception);
			return getResponseEntityWithErrorMessage(HttpStatus.FAILED_DEPENDENCY, exception.getMessage());
			
		} else if (exception instanceof NonUniqueResultException) {
			log.debug(exception.getMessage(), exception);
			return getResponseEntityWithErrorMessage(HttpStatus.CONFLICT,
				messageSource.getMessage("warning.resultNonUnique", null, locale));
			
		} else {
			log.warn(exception.getMessage(), exception);
			return getResponseEntityWithErrorMessage(520,
				messageSource.getMessage("error.unknownError", null, locale));
		}
	}
	
	@ExceptionHandler({IllegalArgumentException.class})
	@ResponseBody
	public ResponseEntity<String> illegalArgumentsFailure(IllegalArgumentException iex) {
		log.error(iex.getMessage(), iex);
		return getResponseEntityWithErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR, messageSource.getMessage(
			"httpStatus.internalServerError.common", null, LocaleContextHolder.getLocale()));
	}
	
	@ExceptionHandler({MaxUploadSizeExceededException.class})
	public ResponseEntity<String> maxUploadFileSizeExceeded(MaxUploadSizeExceededException ex, Locale locale) {
		log.info(ex.getMessage(), ex);
		return getResponseEntityWithErrorMessage(HttpStatus.NOT_ACCEPTABLE, getMessageSource().getMessage(
			"message.uploadImageSizeExceeded(1)",
			new Object[]{maxUploadImageSize},
			locale));
	}
	
	@ExceptionHandler({JwtException.class, ExpiredJwtException.class})
	public ResponseEntity jwtExceptions(JwtException jwte, HttpServletRequest request, Locale locale) {
		log.info(jwte.getMessage(), jwte);
		if (jwte instanceof ExpiredJwtException) {
			String userMessage = messageSource.getMessage(
				"message.jwtTokenExpired", null, locale);
			if (!request.getHeader("Referer").contains("/internal/")) { //External redirection
				return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).header("Location", "/").body(userMessage);
			} else { //Internal error message
				return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(userMessagesCreator.getJsonMessageForUser(userMessage));
			}
		}
		return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
	}
	
	@ExceptionHandler({Throwable.class})
	@ResponseBody
	public ResponseEntity<String> commonFailure(Throwable throwable) {
		log.error(throwable.getMessage(), throwable);
		return getResponseEntityWithErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected internal error occurred!");
	}
	
	private ResponseEntity<String> getResponseEntityWithErrorMessage(HttpStatus httpStatus, @Nullable String messageBody)
		throws IllegalArgumentException {
		if (httpStatus == null) {
			throw new IllegalArgumentException("HttpStatus cannot be null!");
		} else if (messageBody == null) {
			messageBody = "";
		}
		messageBody = "{\"errorMessage\":\"" + messageBody + "\"}";
		return ResponseEntity.status(httpStatus).contentType(MediaType.APPLICATION_JSON_UTF8).body(messageBody);
	}
	
	private ResponseEntity<String> getResponseEntityWithErrorMessage(int httpStatus, String messageBody)
		throws IllegalArgumentException {
		if (httpStatus <= 0) {
			throw new IllegalArgumentException("HttpStatus '" + httpStatus + "' cannot be 0 or below!");
		}
		HttpStatus resolve =
			HttpStatus.resolve(httpStatus) != null ? HttpStatus.resolve(httpStatus) : HttpStatus.INTERNAL_SERVER_ERROR;
		
		messageBody = messageBody == null ? "" : messageBody;
		return ResponseEntity.status(resolve).contentType(MediaType.APPLICATION_JSON_UTF8).body(messageBody);
	}
}
