package internal.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@ControllerAdvice
public class ExceptionHandlerController {
	
	@ExceptionHandler({HttpMessageNotReadableException.class})
	public ResponseEntity<String> httpMessageNotReadableException(Exception ex, WebRequest request){
		log.info(ex.getMessage());
		return ResponseEntity.badRequest().body("Incorrect request body!");
	}
	
	@ExceptionHandler({JsonProcessingException.class})
	public ResponseEntity<String> jsonProcessingFailure(Exception ex, WebRequest request) {
		log.info(ex.getMessage());
		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("The Server hasn't been able to tread JSON from a request!");
	}
	
	@ExceptionHandler({AuthenticationCredentialsNotFoundException.class})
	public ResponseEntity<String> authenticationFailure(Exception ex) {
		log.info(ex.getMessage());
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
	}
}
