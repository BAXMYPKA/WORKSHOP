package internal.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import internal.entities.WorkshopEntity;
import internal.service.EntitiesServiceAbstract;
import internal.service.serviceUtils.JsonServiceUtils;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Pageable;

import javax.annotation.PostConstruct;

/**
 * To be inherited by all the REST Controllers to work with WokrshopEntities.
 *
 * @param <T> Every implementation must be typed with WorkshopEntity of internal domain.
 */
public interface WorkshopController<T extends WorkshopEntity> {
	
	MessageSource getMessageSource();
	
	void setMessageSource(MessageSource messageSource);
	
	EntitiesServiceAbstract<T> getEntitiesServiceAbstract();
	
	void setEntitiesServiceAbstract(EntitiesServiceAbstract<T> entitiesServiceAbstract);
	
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
	
	//The controller methods are the following...
	
	WorkshopEntity getAll();
	
	WorkshopEntity getOne(long id);
	
	WorkshopEntity postOne(WorkshopEntity workshopEntity);
	
	WorkshopEntity putOne(WorkshopEntity workshopEntity);
	
	String deleteOne(long id);
	
	Pageable getPageable(Integer pageSize, Integer pageNum, String orderBy, String order);
	
	/**
	 * "@PostConstruct"
	 * setObjectMapper(getJsonServiceUtils().getObjectMapper());
	 */
	@PostConstruct
	default void afterPropsSet() {
		setObjectMapper(getJsonServiceUtils().getObjectMapper());
	}
	
}
