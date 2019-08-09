package internal.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import internal.entities.WorkshopEntity;
import internal.service.WorkshopEntitiesServiceAbstract;
import internal.service.serviceUtils.JsonServiceUtils;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.PostConstruct;
import java.util.Locale;

/**
 * To be inherited by all the REST Controllers to work with WokrshopEntities.
 *
 * @param <T> Every implementation must be typed with WorkshopEntity of internal domain.
 */
public interface WorkshopController<T extends WorkshopEntity> {
	
	MessageSource getMessageSource();
	
	void setMessageSource(MessageSource messageSource);
	
	WorkshopEntitiesServiceAbstract<T> getWorkshopEntitiesService();
	
//	void setWorkshopEntitiesService(EntitiesServiceAbstract<T> workshopEntitiesService);
	
	int getDEFAULT_PAGE_SIZE();
	
	void setDEFAULT_PAGE_SIZE(int defaultPageSize);
	
	int getMAX_PAGE_NUM();
	
	void setMAX_PAGE_NUM(int maxPageNum);
	
	int getMAX_PAGE_SIZE();
	
	void setMAX_PAGE_SIZE(int maxPageSize);
	
	void setJsonServiceUtils(JsonServiceUtils jsonServiceUtils);
	
	JsonServiceUtils getJsonServiceUtils();
	
	void setObjectMapper(ObjectMapper objectMapper);
	
	ObjectMapper getObjectMapper();
	
//	Class getWorkshopEntityClass();
	
//	void setWorkshopEntityClass(Class workshopEntityClass);
	
	//The controller methods are the following...
	
	ResponseEntity<String> getAll(Integer pageSize, Integer pageNum, String orderBy, String order);
	
	ResponseEntity<String> getOne(long id);
	
	ResponseEntity<String> postOne(WorkshopEntity workshopEntity);
	
	ResponseEntity<String> putOne(WorkshopEntity workshopEntity);
	
	ResponseEntity<String> deleteOne(long id);
	
	Pageable getPageable(Integer pageSize, Integer pageNum, String orderBy, String order);
	
	/**
	 * "@PostConstruct"
	 * setObjectMapper(getJsonServiceUtils().getObjectMapper());
	 */
	void postConstruct() throws JsonProcessingException;
}