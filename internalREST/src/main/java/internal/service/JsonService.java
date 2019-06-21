package internal.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.corba.se.spi.ior.TaggedProfileTemplate;
import internal.entities.Position;
import internal.entities.WorkshopEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
	 * @param jsonEntity   JSON string with an Entity
	 * @param entity An exact Entity.class
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
	
	public <T extends WorkshopEntity> List convertJsonToEntities(String jsonEntities, Class<T> entityClass)
		throws IOException, ClassCastException, ClassNotFoundException {
		Class<T[]> clazz = (Class<T[]>) Class.forName(entityClass.getName());
		System.out.println(clazz);
		return Arrays.asList(objectMapper.readValue(jsonEntities, clazz));
//		List<T> entities = new ArrayList<T>(Arrays.asList(clk));
//		System.out.println(entities.get(0));
//		List entities = objectMapper.readValue(jsonEntities, List.class);
//		return Arrays.asList(ts);
	}
	
	public Collection<WorkshopEntity> convert(){
		
		return  null;
	}
}
