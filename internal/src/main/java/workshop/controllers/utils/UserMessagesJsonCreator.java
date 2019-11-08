package workshop.controllers.utils;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

/**
 * {@literal External Json Errors Object:
 * '{"errorFieldName":"errorFieldMessage"}'}
 */
@NoArgsConstructor
@Slf4j
@Component
public class UserMessagesJsonCreator {
	
	private StringBuilder jsonErrorObject;
	
	public String getJsonMessageForUser(String userMessage) {
		userMessage = userMessage == null ? "" : userMessage;
		
		jsonErrorObject = new StringBuilder();
		jsonErrorObject.append("\"").append("userMessage").append("\":").append(getObjectFieldValue(userMessage));
		return getFinishedString(jsonErrorObject);
	}
	
	public String convertBindingResultToJson(BindingResult bindingResult) {
		jsonErrorObject = new StringBuilder();
		for (FieldError fieldError : bindingResult.getFieldErrors()) {
			
			if (jsonErrorObject.length() > 1) jsonErrorObject.append(",");
			
			jsonErrorObject.append(getObjectFieldName(fieldError.getField()))
				.append(getObjectFieldValue(fieldError.getDefaultMessage()));
/*
			jsonErrorObject
				.append("\"").append(fieldError.getField()).append("\"")
				.append(":")
				.append("\"").append(fieldError.getDefaultMessage()).append("\"");
*/
		}
		return getFinishedString(jsonErrorObject);
	}
	
	private String getObjectFieldName(String objectFieldName) {
		return "\"" + objectFieldName + ("\":");
	}
	
	private String getObjectFieldValue(String objectFieldValue) {
		return "\"" + objectFieldValue + ("\"");
	}
	
	private String getFinishedString(StringBuilder jsonErrorObject) {
		jsonErrorObject.insert(0, "{").insert(jsonErrorObject.length(), "}");
		return jsonErrorObject.toString().replace("'", "\"");
	}
}
