package internal.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import internal.entities.WorkshopEntity;
import internal.services.WorkshopEntitiesServiceAbstract;
import internal.services.serviceUtils.JsonServiceUtils;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

/**
 * Spring specified controllers contract.
 * To be inherited by all the REST Controllers to work with WorkshopEntities.
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
	
//	Class getWorkshopEntityClass();
//	void setWorkshopEntityClass(Class workshopEntityClass);
	
	//The controller methods are the following...
	
	ResponseEntity<String> getAll(Integer pageSize, Integer pageNum, String orderBy, String order);
	
	ResponseEntity<String> getOne(long id);
	
	ResponseEntity<String> postOne(T workshopEntity, BindingResult bindingResult);
	
	ResponseEntity<String> putOne(long id, T workshopEntity, BindingResult bindingResult);
	
	ResponseEntity<String> deleteOne(long id);
	
	Pageable getPageable(Integer pageSize, Integer pageNum, String orderBy, String order);
}
