package internal.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import internal.entities.WorkshopEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Getter
@Setter
@Component
public class JsonService {
	
	private ObjectMapper objectMapper;
	
	/**
	 * ObjectMapper with the "jackson-datatype-jsr310" dependency as the registered module
	 * and simplified view of LocalDateTime datatypes.
	 */
	public JsonService() {
		this.objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
	}
	
	public String convertToJson(WorkshopEntity entity) throws JsonProcessingException {
		String value = objectMapper.writeValueAsString(entity);
		return value;
	}
	
	/**
	 * @param json   JSON string with an Entity
	 * @param entity An exact Entity.class
	 * @param <T>
	 * @return
	 * @throws IOException
	 */
	public <T extends WorkshopEntity> T convertFromJson(String json, Class<T> entity) throws IOException {
		T convertedEntity = objectMapper.readValue(json, entity);
		return convertedEntity;
	}
}
