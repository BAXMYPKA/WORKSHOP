package internal.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import internal.entities.WorkshopEntity;
import internal.entities.WorkshopEntityAbstract;
import internal.exceptions.IllegalArguments;
import internal.exceptions.PersistenceFailure;
import internal.service.WorkshopEntitiesServiceAbstract;
import internal.service.serviceUtils.JsonServiceUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.Locale;

/**
 * 1. Every REST WorkshopController has to have an instance variable of EntitiesServiceAbstract<T extends WorkshopEntity>
 * so that to get the concrete type of WorkshopEntity to operate with.
 * 2. Every REST controller has to be annotated with:
 * a. "@RequestMapping(path = "/internal/<workshop_entities>", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})"
 * b. "@RestController"
 * c. '@ExposesResourcesFor(WorkshopEntity.class)' To be Spring EntityLinks capable controller every instance has to be
 * annotated with it and has the method kind of:
 * "@RequestMapping("/{id}")
 * ResponseEntity getOne(@PathVariable("id") … ) { … }"
 *
 * @param <T> WorkshopEntity classType
 */
@Getter
@Setter
@Slf4j
@RestController
public abstract class WorkshopControllerAbstract<T extends WorkshopEntityAbstract> implements WorkshopController {
	
	@Value("${page.size.default}")
	private int DEFAULT_PAGE_SIZE;
	@Value("${page.max_num}")
	private int MAX_PAGE_NUM;
	@Value("${page.size.max}")
	private int MAX_PAGE_SIZE;
	@Value("${default.orderBy}")
	private String DEFAULT_ORDER_BY;
	@Value("${default.order}")
	private String DEFAULT_ORDER;
	@Autowired
	private EntityLinks entityLinks;
	@Autowired
	private MessageSource messageSource;
	@Autowired
	private JsonServiceUtils jsonServiceUtils;
	private ObjectMapper objectMapper;
	@Autowired
	private WorkshopEntitiesServiceAbstract<T> workshopEntitiesService;
	private Class<T> workshopEntityClass;
	/**
	 * Just a simple name for simplified "workshopEntityClass.getSimpleName()"
	 */
	private String workshopEntityClassName;
	/**
	 * "rel:getAllWorkshopEntityClassName(s), href:/internal/workshopEntities
	 */
	private Link allWorkshopEntitiesLink;
	
	/**
	 * @param workshopEntitiesService By this instance we set the concrete instance of WorkshopServiceAbstract
	 *                                and through it set the concrete type of WorkshopEntity as {@link #getWorkshopEntityClass()}
	 *                                to operate with.
	 */
	@Autowired
	public WorkshopControllerAbstract(WorkshopEntitiesServiceAbstract<T> workshopEntitiesService) {
		this.workshopEntitiesService = workshopEntitiesService;
		this.workshopEntityClass = workshopEntitiesService.getEntityClass();
		workshopEntityClassName = workshopEntityClass.getSimpleName();
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
	public ResponseEntity<String> getAll(@RequestParam(value = "pageSize", required = false, defaultValue = "${page.size.default}") Integer pageSize,
										 @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
										 @RequestParam(name = "order-by", required = false, defaultValue = "${default.orderBy}") String orderBy,
										 @RequestParam(name = "order", required = false, defaultValue = "${default.order}") String order) {
		Locale locale = LocaleContextHolder.getLocale();
		Pageable pageable = getPageable(pageSize, pageNum, orderBy, order);
		Page<T> entitiesPage = workshopEntitiesService.findAllEntities(pageable, orderBy);
		
		if (entitiesPage != null && entitiesPage.getContent().size() > 0) {
			
			Resources<T> resources = new Resources<>(entitiesPage.getContent());
			addResourcesLinks(resources);
			String pagedResourcesToJson = jsonServiceUtils.workshopEntityObjectsToJson(resources);
			
			System.out.println("TOTAL ELEMENTS: " + entitiesPage.getTotalElements());
			System.out.println("TOTAL PAGES: " + entitiesPage.getTotalPages());
			System.out.println("NUMBER: " + entitiesPage.getNumber());
			System.out.println(".getPageable().getPageNumber(): " + entitiesPage.getPageable().getPageNumber());
			System.out.println(".getPageable().getPageSize(): " + entitiesPage.getPageable().getPageSize());
			System.out.println(".getPageable().next(): " + entitiesPage.getPageable().next());
			
			log.debug("{}s Page found for pageNumber={}, with pageSize={} and written as JSON", workshopEntityClassName,
				entitiesPage.getPageable().getPageNumber(), entitiesPage.getPageable().getPageSize());
			
			return ResponseEntity.ok(pagedResourcesToJson);
		} else {
			String localizedMessage =
				messageSource.getMessage("message.notFound(1)", new Object[]{workshopEntityClassName}, locale);
			PersistenceFailure persistenceFailure =
				new PersistenceFailure("No " + workshopEntityClassName + "s found!", HttpStatus.NOT_FOUND);
			persistenceFailure.setLocalizedMessage(localizedMessage);
			throw persistenceFailure;
		}
	}
	
	@Override
	@GetMapping("/{id}")
	public ResponseEntity<String> getOne(@PathVariable("id") long id) {
		T entity = workshopEntityClass.cast(workshopEntitiesService.findById(id));
		addSelfLink(entity);
		String entityToJson = jsonServiceUtils.workshopEntityObjectsToJson(entity);
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
	
	private Link getPagedLinks(int pageSize, int pageNum, String orderBy, String order) {
		return null;
	}
	
	private void addSelfLinks(Page<T> page) {
		page.get().forEach(this::addSelfLink);
	}
	
	private void addSelfLink(T workshopEntity) {
		Link selfLink = entityLinks
			.linkForSingleResource(workshopEntityClass, workshopEntity.getIdentifier()).withSelfRel()
			.withHreflang(LocaleContextHolder.getLocale().toLanguageTag()).withMedia("json");
		workshopEntity.add(selfLink);
	}
	
	/**
	 * Ass self-links to all included WorkshopEntities and Link to their full collection
	 */
	private void addResourcesLinks(Resources<T> resources) {
		resources.getContent().forEach(this::addSelfLink);
//		resources.add(getPagedLinks());
		resources.add(allWorkshopEntitiesLink);
	}
	
	@Override
	@PostConstruct
	public void postConstruct() {
		setObjectMapper(getJsonServiceUtils().getObjectMapper());
		allWorkshopEntitiesLink = ControllerLinkBuilder.linkTo(
			ControllerLinkBuilder.methodOn(this.getClass()).getAll(getDEFAULT_PAGE_SIZE(), 1, DEFAULT_ORDER_BY, DEFAULT_ORDER)
		).withRel("getAll" + workshopEntityClassName + "s");
//		allWorkshopEntitiesLink = entityLinks.linkToCollectionResource(workshopEntityClass)
//			.withRel("getAll" + workshopEntityClassName + "s")
//			.withHreflang(LocaleContextHolder.getLocale().toLanguageTag()).withMedia("json");
	}
}
