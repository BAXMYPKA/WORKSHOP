package internal.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import internal.exceptions.EntityNotFound;
import internal.exceptions.PersistenceFailed;
import internal.exceptions.WorkshopException;
import internal.service.serviceUtils.JsonServiceUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.persistence.*;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Getter
@Setter
@ControllerAdvice
public class ExceptionHandlerController {
	
	@Autowired
	private JsonServiceUtils jsonServiceUtils;
	
	@ExceptionHandler({HttpMessageNotReadableException.class}) //400
	public ResponseEntity<String> httpMessageNotReadableException(Exception ex, HttpServletResponse response) {
		log.info(ex.getMessage());
		setResponseContentType(response);
		return ResponseEntity.badRequest().body("Incorrect request body!");
	}
	
	@ExceptionHandler({JsonProcessingException.class}) //422
	public ResponseEntity<String> jsonProcessingFailure(Exception ex, HttpServletResponse response) {
		log.info(ex.getMessage());
		setResponseContentType(response);
		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("The Server hasn't been able to tread JSON from a request!");
	}
	
	@ExceptionHandler({AuthenticationCredentialsNotFoundException.class})
	public ResponseEntity<String> authenticationFailure(Exception ex, HttpServletResponse response) {
		log.info(ex.getMessage());
		setResponseContentType(response);
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
	}
	
	@ExceptionHandler({MethodArgumentNotValidException.class}) //422
	@ResponseBody
	public ResponseEntity<String> validationFailure(MethodArgumentNotValidException ex, HttpServletResponse response) {
		BindingResult bindingResult = ex.getBindingResult();
		Map<String, String> fieldErrors = bindingResult
			.getFieldErrors()
			.stream()
			.collect(Collectors.toMap(fieldError -> fieldError.getField(), fieldError -> fieldError.getDefaultMessage()));
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			String jsonedFieldErrors = objectMapper.writeValueAsString(fieldErrors);
			setResponseContentType(response);
			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(jsonedFieldErrors);
		} catch (JsonProcessingException e) {
			log.error(e.getMessage());
			setResponseContentType(response);
			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(fieldErrors.toString());
		}
	}
	
	@ExceptionHandler({PersistenceFailed.class})
	@ResponseBody
	public ResponseEntity<String> persistenceFailed(WorkshopException wx) {
		log.error(wx.getMessage(), wx);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(wx.getMessage());
	}
	
	@ExceptionHandler({PersistenceException.class})
	@ResponseBody
	public ResponseEntity<String> persistenceFailed(PersistenceException exception) {
		if (exception instanceof EntityExistsException) {
			log.debug(exception.getMessage(), exception);
			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
				"The Entity with the same ID is already exist!");
		} else if (exception instanceof EntityNotFoundException) {
			log.debug(exception.getMessage(), exception);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The Entity is not found!");
		} else if (exception instanceof NoResultException) {
			log.debug(exception.getMessage(), exception);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No result!");
		} else if (exception instanceof RollbackException) {
			log.debug(exception.getMessage(), exception);
			return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body("The operation is rolled back!");
		} else if (exception instanceof NonUniqueResultException) {
			log.debug(exception.getMessage(), exception);
			return ResponseEntity.status(HttpStatus.CONFLICT).body("The result is not unique!");
		} else {
			log.warn(exception.getMessage(), exception);
			return ResponseEntity.status(520).body("Unknown error is occurred while performing the operation!");
		}
	}
	
	@ExceptionHandler({IllegalArgumentException.class})
	@ResponseBody
	public ResponseEntity<String> illegalArgumentsFailure(IllegalArgumentException iex) {
		log.warn(iex.getMessage(), iex);
		return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(iex.getMessage());
	}
	
	@ExceptionHandler({Throwable.class})
	@ResponseBody
	public ResponseEntity<String> commonFailure(Throwable throwable) {
		log.error(throwable.getMessage(), throwable);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected internal error occurred!");
	}
	
	private void setResponseContentType(HttpServletResponse response) {
		response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
	}
}
