package internal.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import internal.service.JsonService;
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

import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Getter
@Setter
@ControllerAdvice
public class ExceptionHandlerController {
	
	@Autowired
	private JsonService jsonService;
	
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
	
	private void setResponseContentType(HttpServletResponse response) {
		response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
	}
}
