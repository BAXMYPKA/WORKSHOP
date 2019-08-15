package internal.services.serviceUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import internal.entities.WorkshopEntity;
import internal.exceptions.InternalServerErrorException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
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
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
	}
	
	/**
	 * @param jsonEntity JSON string with an Entity
	 * @param entity     An exact Entity.class
	 * @param <T>
	 * @return An Entity with all the ZonedDateTime fields set to UTC value.
	 * @throws IOException
	 */
	public <T extends WorkshopEntity> T workshopEntityObjectsFromJson(String jsonEntity, Class<T> entity) {
		try {
			T convertedEntity = objectMapper.readValue(jsonEntity, entity);
			log.debug("Received JSON successfully converted to {}", entity.getSimpleName());
			return convertedEntity;
		} catch (IOException e) {
			throw new InternalServerErrorException(e.getMessage(), "error.jsonUnprocessable",
				HttpStatus.UNPROCESSABLE_ENTITY, e);
		}
	}
	
	public String workshopEntityObjectsToJson(WorkshopEntity entity) {
		return getJson(entity);
	}
	
//	public String workshopEntityObjectsToJson(Resource<WorkshopEntity> entityResource) {
//		return getJson(entityResource);
//	}
	
	/**
	 * @param entities
	 * @param <T>
	 * @return A Json String with Entity. All the ZonedDateTime fields will be saved as it (i.e. with presented
	 * Timezone), but during deserialization all those fields will be converted to the UTC Timezone.
	 * @throws JsonProcessingException
	 */
	public <T extends WorkshopEntity> String workshopEntityObjectsToJson(Collection<T> entities) {
		return getJson(entities);
	}
	
	/**
	 * @param resource Resource<T extends WorkshopEntityAbstract> - the Resources with a Collection of WorkshopEntities
	 *                 with embedded self Links. The collection itself contains Links (prevPage, nexPage, LastPage etc).
	 * @param <T>      <T extends WorkshopEntityAbstract>
	 * @return
	 * @throws JsonProcessingException
	 */
	public <T extends WorkshopEntity> String workshopEntityObjectsToJson(Resource<T> resource) {
		return getJson(resource);
	}
	
//	public <T extends WorkshopEntity> String workshopEntityObjectsToJson(Resources<T> resources) {
//		return getJson(resources);
//	}
	
	/**
	 * @param resources Resource<T extends WorkshopEntityAbstract> - the Resources with a Collection of WorkshopEntities
	 *                 with embedded self Links. The collection itself contains Links (prevPage, nexPage, LastPage etc).
	 * @param <T>      <T extends WorkshopEntityAbstract>
	 * @return
	 * @throws JsonProcessingException
	 */
	public <T extends WorkshopEntity> String workshopEntityObjectsToJson(Resources<Resource<T>> resources) {
		return getJson(resources);
	}
	
	private String getJson(Object o) {
		try {
			String value = objectMapper.writeValueAsString(o);
			log.debug("{} successfully converted to JSON.", o.getClass().getSimpleName());
			return value;
		} catch (JsonProcessingException e) {
			throw new InternalServerErrorException(e.getMessage(), "error.jsonInternalError",
				HttpStatus.INTERNAL_SERVER_ERROR, e);
		}
		
	}
}
