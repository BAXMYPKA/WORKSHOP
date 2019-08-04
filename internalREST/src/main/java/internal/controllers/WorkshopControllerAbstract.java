package internal.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import internal.entities.WorkshopEntity;
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
import org.springframework.web.bind.annotation.RestController;

@Getter
@Setter
@RestController
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
	private EntitiesServiceAbstract<T> entitiesServiceAbstract;
	@Autowired
	private JsonServiceUtils jsonServiceUtils;
	ObjectMapper objectMapper;
	
	@Override
	public WorkshopEntity getAll() {
		return null;
	}
	
	@Override
	public WorkshopEntity getOne(long id) {
		return null;
	}
	
	@Override
	public WorkshopEntity postOne(WorkshopEntity workshopEntity) {
		return null;
	}
	
	@Override
	public WorkshopEntity putOne(WorkshopEntity workshopEntity) {
		return null;
	}
	
	@Override
	public String deleteOne(long id) {
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
	
	@Override
	public void setEntitiesServiceAbstract(EntitiesServiceAbstract entitiesServiceAbstract) {
		this.entitiesServiceAbstract = entitiesServiceAbstract;
	}
	
	/*
	@PostConstruct
	@Override
	public void afterPropsSet() {
		setObjectMapper(getJsonServiceUtils().getObjectMapper());
	}
*/
}
