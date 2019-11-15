package workshop.controllers.utils;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Objects;

/**
 * {@literal External Json Errors Object:
 * '{"errorFieldName":"errorFieldMessage"}'}
 */
@NoArgsConstructor
@Slf4j
@Component
public class UserMessagesCreator {
	
	private final String USER_MESSAGE_ATTRIBUTE_NAME = "userMessage";
	
	private StringBuilder jsonErrorObject;
	
	public String getJsonMessageForUser(String userMessage) {
		userMessage = userMessage == null ? "" : userMessage;
		
		jsonErrorObject = new StringBuilder();
		jsonErrorObject.append("\"").append("userMessage").append("\":").append(getObjectFieldValue(userMessage));
		return getFinishedString(jsonErrorObject);
	}
	
	/**
	 * Just sets any kind of {@link String} messages as a special formatted 'userMessage' attribute into the given
	 * {@link Model} to be displayed onto html pages which support 'userMessage' attribute.
	 *
	 * @param model          The {@link Model} from any WorkshopController the following message for User has to be inserted into
	 * @param userMessage {@link String} with localized (or not) special message for the end Users to be displayed
	 *                       onto any html page that supports this kind of messages.
	 */
	public void setMessageForUser(Model model, String userMessage) {
		Objects.requireNonNull(model).addAttribute(USER_MESSAGE_ATTRIBUTE_NAME, Objects.requireNonNull(userMessage));
	}
	
	/**
	 * Just sets any kind of {@link String} messages as a special formatted 'userMessage' attribute into the given
	 * {@link Model} to be displayed onto html pages which support 'userMessage' attribute.
	 *
	 * @param redirectAttributes          The {@link RedirectAttributes} from any WorkshopController the following message for User has to be inserted into
	 * @param userMessage {@link String} with localized (or not) special message for the end Users to be displayed
	 *                       onto any html page that supports this kind of messages.
	 */
	public void setMessageForUser(RedirectAttributes redirectAttributes, String userMessage) {
		Objects.requireNonNull(redirectAttributes)
			.addAttribute(USER_MESSAGE_ATTRIBUTE_NAME, Objects.requireNonNull(userMessage));
	}
	
	public String convertBindingResultToJson(BindingResult bindingResult) {
		jsonErrorObject = new StringBuilder();
		for (FieldError fieldError : bindingResult.getFieldErrors()) {
			
			if (jsonErrorObject.length() > 1) jsonErrorObject.append(",");
			
			jsonErrorObject.append(getObjectFieldName(fieldError.getField()))
				.append(getObjectFieldValue(fieldError.getDefaultMessage()));
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
		return jsonErrorObject.toString().replace("'", "\\\\'");
	}
}
