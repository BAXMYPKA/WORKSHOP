package workshop.controllers.internal.rest;

import org.springframework.web.context.request.WebRequest;
import workshop.internal.entities.WorkshopEntity;
import workshop.internal.services.WorkshopEntitiesServiceAbstract;
import workshop.internal.services.serviceUtils.JsonServiceUtils;
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
public interface WorkshopRestController<T extends WorkshopEntity> {
	
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
	
	ResponseEntity<String> getAll(Integer pageSize, Integer pageNum, String orderBy, String order, WebRequest webRequest);
	
	ResponseEntity<String> getOne(Long id, WebRequest webRequest);
	
	ResponseEntity<String> postOne(T workshopEntity, BindingResult bindingResult, WebRequest webRequest);
	
	ResponseEntity<String> putOne(Long id, T workshopEntity, BindingResult bindingResult, WebRequest webRequest);
	
	ResponseEntity<String> deleteOne(Long id, WebRequest webRequest);
	
	Pageable getPageable(Integer pageSize, Integer pageNum, String orderBy, String order);
}
