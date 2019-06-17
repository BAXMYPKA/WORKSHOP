package internal.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Getter
@Setter
@Component
public class JsonService {
	
//	public void convertToJson(Class clazz) throws JsonProcessingException {
//		ObjectMapper objectMapper = new ObjectMapper();
//		String value = objectMapper.writeValueAsString(clazz);
//		return null;
//	}
}
