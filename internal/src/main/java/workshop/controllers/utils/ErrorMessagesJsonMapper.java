package workshop.controllers.utils;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

/**
 * {@literal External Json Errors Object:
 * 	'{"errorFieldName":"errorFieldMessage"}'}
 */
@NoArgsConstructor
@Slf4j
@Component
public class ErrorMessagesJsonMapper {
	
	private StringBuilder jsonErrorObject;
	
	public String convertBindingResultToJson(BindingResult bindingResult) {
		jsonErrorObject = new StringBuilder();
		for (FieldError fieldError : bindingResult.getFieldErrors()) {
			if (jsonErrorObject.length() > 1) jsonErrorObject.append(",");
			jsonErrorObject
				.append("\"").append(fieldError.getField()).append("\"")
				.append(":")
				.append("\"").append(fieldError.getDefaultMessage()).append("\"");
		}
		return getFinishedString(jsonErrorObject);
	}
	
	private String getFinishedString(StringBuilder jsonErrorObject) {
		jsonErrorObject.insert(0, "{").insert(jsonErrorObject.length(), "}");
		return 	jsonErrorObject.toString().replace("'", "\"");
	}
}
