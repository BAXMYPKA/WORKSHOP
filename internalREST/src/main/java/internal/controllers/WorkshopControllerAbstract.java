package internal.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import internal.entities.WorkshopEntity;
import internal.entities.hateoasResources.WorkshopEntityResource;
import internal.exceptions.IllegalArguments;
import internal.service.EntitiesServiceAbstract;
import internal.service.serviceUtils.JsonServiceUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;

import static org.springframework.http.ResponseEntity.ok;

/**
 * Every REST WorkshopController has to have an instance variable of EntitiesServiceAbstract<T extends WorkshopEntity>
 * so that to get the concrete type of WorkshopEntity to operate with.
 * That's why the constructor with EntitiesServiceAbstract<T> parameter is obligatory!
 *
 * @param <T>
 */
@Getter
@Setter
public abstract class WorkshopControllerAbstract<T extends WorkshopEntity> implements WorkshopController {
	
	@Value("${page.size.default}")
	private int DEFAULT_PAGE_SIZE;
	@Value("${page.max_num}")
	private int MAX_PAGE_NUM;
	@Value("${page.size.max}")
	private int MAX_PAGE_SIZE;
	@Autowired
	private MessageSource messageSource;
	@Autowired
	private EntitiesServiceAbstract<T> entitiesService;
	@Autowired
	private JsonServiceUtils jsonServiceUtils;
	private ObjectMapper objectMapper;
	private Class<T> entityClass;
	
	/**
	 * @param entitiesService By this instance we set the concrete instance of WorkshopServiceAbstract
	 *                                and through it set the concrete type of WorkshopEntity as {@link #getEntityClass()}
	 *                                to operate with.
	 */
	public WorkshopControllerAbstract(EntitiesServiceAbstract<T> entitiesService) {
		this.entitiesService = entitiesService;
		setEntityClass(entitiesService.getEntityClass());
	}
	
	@Override
	public ResponseEntity<String> getAll() {
		return null;
	}
	
	@Override
	public ResponseEntity<String> getOne(long id) throws JsonProcessingException {
		T entity = entityClass.cast(entitiesService.findById(id));
		WorkshopEntityResource<T> resource;
		String entityToJson = jsonServiceUtils.convertEntityToJson(entity);
		return ResponseEntity.ok(entityToJson);
	}
	
	@Override
	public ResponseEntity<String> postOne(WorkshopEntity workshopEntity) {
		return null;
	}
	
	@Override
	public ResponseEntity<String> putOne(WorkshopEntity workshopEntity) {
		return null;
	}
	
	@Override
	public ResponseEntity<String> deleteOne(long id) {
		return null;
	}
	
	@Override
	public Pageable getPageable(Integer pageSize, Integer pageNum, String orderBy, String order) {
		Sort.Direction direction = null;
		if (order != null && !order.isEmpty()) { //The request has to be ordered by asc or desc
			try {
				direction = Sort.Direction.fromString(order);
			} catch (IllegalArgumentException e) { //If 'order' doesn't math asc or desc
				throw new IllegalArguments("'Order' parameter must be equal 'asc' or 'desc' value!", e);
			}
		} else { //DESC is the default value if 'order' param is not presented in the Request
			direction = Sort.Direction.DESC;
		}
		//PageRequest doesn't allow empty parameters strings, so "created" as the default is used
		orderBy = orderBy == null || orderBy.isEmpty() ? "created" : orderBy;
		pageSize = pageSize == null || pageSize <= 0 || pageSize > getDEFAULT_PAGE_SIZE() ? getDEFAULT_PAGE_SIZE() : pageSize;
		pageNum = pageNum == null || pageNum <= 0 || pageNum > getMAX_PAGE_NUM() ? 1 : pageNum;
		
		return PageRequest.of(
			pageNum,
			pageSize,
			new Sort(direction, orderBy));
	}
}
