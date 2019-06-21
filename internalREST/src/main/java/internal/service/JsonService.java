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
import java.util.Collection;

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
	
	public String convertEntityToJson(WorkshopEntity entity) throws JsonProcessingException {
		String value = objectMapper.writeValueAsString(entity);
		return value;
	}
	
	/**
	 * @param jsonEntity JSON string with an Entity
	 * @param entity     An exact Entity.class
	 * @param <T>
	 * @return
	 * @throws IOException
	 */
	public <T extends WorkshopEntity> T convertEntityFromJson(String jsonEntity, Class<T> entity) throws IOException {
		T convertedEntity = objectMapper.readValue(jsonEntity, entity);
		return convertedEntity;
	}
	
	public <T extends WorkshopEntity> String convertEntitiesToJson(Collection<T> entities) throws JsonProcessingException {
		String values = objectMapper.writeValueAsString(entities);
		return values;
	}
}
