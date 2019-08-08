package internal.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import internal.entities.Order;
import internal.entities.WorkshopEntity;
import internal.entities.WorkshopEntityAbstract;
import internal.entities.hateoasResources.WorkshopEntityResource;
import internal.exceptions.IllegalArguments;
import internal.exceptions.PersistenceFailure;
import internal.service.WorkshopEntitiesServiceAbstract;
import internal.service.serviceUtils.JsonServiceUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

import static org.springframework.http.ResponseEntity.ok;

/**
 * 1. Every REST WorkshopController has to have an instance variable of EntitiesServiceAbstract<T extends WorkshopEntity>
 * so that to get the concrete type of WorkshopEntity to operate with.
 * <p>
 * 2. Every REST controller has to be annotated with:
 * a. "@RequestMapping(path = "/internal/<workshop_entities>", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})"
 * b. "@RestController"
 * <p>
 * 3. To be Spring EntityLinks capable controller every instance has to be annotated with
 * '@ExposesResourcesFor(WorkshopEntity.class)' and has the method kind of:
 * "@RequestMapping("/{id}")
 * ResponseEntity getOne(@PathVariable("id") … ) { … }"
 *
 * @param <T> WorkshopEntity type
 */
@Getter
@Setter
public abstract class WorkshopControllerAbstract <T extends WorkshopEntityAbstract> implements WorkshopController {
	
	@Value("${page.size.default}")
	private int DEFAULT_PAGE_SIZE;
	@Value("${page.max_num}")
	private int MAX_PAGE_NUM;
	@Value("${page.size.max}")
	private int MAX_PAGE_SIZE;
	@Autowired
	private EntityLinks entityLinks;
	@Autowired
	private MessageSource messageSource;
	@Autowired
	private WorkshopEntitiesServiceAbstract<T> entitiesService;
	@Autowired
	private JsonServiceUtils jsonServiceUtils;
	private ObjectMapper objectMapper;
	private Class<T> entityClass;
	/**
	 * Just a simple name for simplified "entityClass.getSimpleName()"
	 */
	private final String workshopEntityName;
	
	/**
	 * @param entitiesService By this instance we set the concrete instance of WorkshopServiceAbstract
	 *                        and through it set the concrete type of WorkshopEntity as {@link #getEntityClass()}
	 *                        to operate with.
	 */
	public WorkshopControllerAbstract(WorkshopEntitiesServiceAbstract<T> entitiesService) {
		this.entitiesService = entitiesService;
		setEntityClass(entitiesService.getEntityClass());
		workshopEntityName = entityClass.getSimpleName();
	}
	
	/**
	 * @param pageSize Non-required amount of Orders on one pageNum. Default is OrdersService.PAGE_SIZE_DEFAULT
	 * @param pageNum  Number of pageNum with the list of Orders. One pageNum contains 'pageSize' amount of Orders.
	 * @param orderBy  The property of Order all the Orders have to be ordered by.
	 * @param order    Ascending or descending order.
	 * @return
	 * @throws JsonProcessingException
	 */
	@Override
	@GetMapping
	public ResponseEntity<String> getAll(@RequestParam(value = "pageSize", required = false) Integer pageSize,
										 @RequestParam(value = "pageNum", required = false) Integer pageNum,
										 @RequestParam(name = "order-by", required = false) String orderBy,
										 @RequestParam(name = "order", required = false) String order)
		throws JsonProcessingException {
		Locale locale = LocaleContextHolder.getLocale();
		Pageable pageable = getPageable(pageSize, pageNum, orderBy, order);
		Page<T> entitiesPage = entitiesService.findAllEntities(pageable, orderBy);
		
		if (entitiesPage != null && entitiesPage.getContent().size() > 0) {
			addSelfLinks(entitiesPage);
			String jsonPageEntities = jsonServiceUtils.convertEntitiesToJson(entitiesPage.getContent());
			return ResponseEntity.ok(jsonPageEntities);
		} else {
			String localizedMessage =
				messageSource.getMessage("message.notFound(1)", new Object[]{workshopEntityName}, locale);
			PersistenceFailure persistenceFailure =
				new PersistenceFailure("No " + workshopEntityName + "s found!", HttpStatus.NOT_FOUND);
			persistenceFailure.setLocalizedMessage(localizedMessage);
			throw persistenceFailure;
		}
	}
	
	@Override
	@GetMapping("/{id}")
	public ResponseEntity<String> getOne(@PathVariable("id") long id) throws JsonProcessingException {
		T entity = entityClass.cast(entitiesService.findById(id));
		Link linkToSingleResource = entityLinks.linkToSingleResource(entityClass, id);
		entity.add(linkToSingleResource);
		String entityToJson = jsonServiceUtils.convertEntityToJson(entity);
		return ResponseEntity.ok(entityToJson);
	}
	
	/**
	 * WorkshopEntity may contain some included WorkshopEntities without identifiers or 'identifier = 0' -
	 * they all will be treated as new ones and persisted in the DataBase.
	 * If any of them will throw an Exception during a persistence process - the whole WorkshopEntity won't be saved!
	 *
	 * @param workshopEntity WorkshopEntity object as JSON
	 * @return Either persisted WorkshopEntity with the 'identifier' set or an HttpStatus error with a description
	 * @throws JsonProcessingException
	 * @throws HttpMessageNotReadableException
	 */
	@Override
	@ResponseStatus(code = HttpStatus.CREATED)
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
	
	private void addSelfLinks(Page<T> page) {
		page.get().forEach(this::addSelfLink);
	}
	
	private void addSelfLink(T workshopEntity){
		Link selfLink = entityLinks.linkToSingleResource(workshopEntity.getClass(), workshopEntity.getIdentifier());
		workshopEntity.add(selfLink);
	}
}
