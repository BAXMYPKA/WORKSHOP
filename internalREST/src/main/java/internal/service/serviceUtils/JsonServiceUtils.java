package internal.service.serviceUtils;

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
import java.time.ZoneId;
import java.util.Collection;
import java.util.TimeZone;

/**
 * Serialized objects will keep their original time with TimeZone.
 * Deserialized objects will be returned with UTC time and TimeZone.
 */
@Slf4j
@Getter
@Setter
@Component
public class JsonServiceUtils {
	
	private ObjectMapper objectMapper;
	
	/**
	 * ObjectMapper with the "jackson-datatype-jsr310" dependency as the registered module
	 * and simplified view of LocalDateTime datatypes.
	 */
	public JsonServiceUtils() {
		this.objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		objectMapper.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	
	public String convertEntityToJson(WorkshopEntity entity) throws JsonProcessingException {
		String value = objectMapper.writeValueAsString(entity);
		return value;
	}
	
	/**
	 *
	 * @param jsonEntity JSON string with an Entity
	 * @param entity     An exact Entity.class
	 * @param <T>
	 * @return An Entity with all the ZonedDateTime fields set to UTC value.
	 * @throws IOException
	 */
	public <T extends WorkshopEntity> T convertEntityFromJson(String jsonEntity, Class<T> entity) throws IOException {
		T convertedEntity = objectMapper.readValue(jsonEntity, entity);
		return convertedEntity;
	}
	
	/**
	 * @param entities
	 * @param <T>
	 * @return A Json String with Entity. All the ZonedDateTime fields will be saved as it (i.e. with presented
	 * Timezone), but during deserialization all those fields will be converted to the UTC Timezone.
	 * @throws JsonProcessingException
	 */
	public <T extends WorkshopEntity> String convertEntitiesToJson(Collection<T> entities) throws JsonProcessingException {
		String values = objectMapper.writeValueAsString(entities);
		return values;
	}
}
