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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.StringJoiner;

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
	 * @param pageNum  Number of page desires page. From 1 to max.
	 *                 (!) Spring Page interface internally starts page count from 0 so to adapt it to more convenient
	 *                 count from 1 we do 'pageNum--' and '++pageNum' when return pageNum back to the end User.
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
			System.out.println("NUMBER ELEMENTS: " + entitiesPage.getNumberOfElements());
			System.out.println("TOTAL PAGES: " + entitiesPage.getTotalPages());
			System.out.println("NUMBER: " + entitiesPage.getNumber());
//			System.out.println("getPageNumber(): " + entitiesPage.getPageable().getPageNumber());
//			System.out.println("getPageSize(): " + entitiesPage.getPageable().getPageSize());
			System.out.println("next(): " + entitiesPage.getPageable().next());
			System.out.println("next().next(): " + entitiesPage.getPageable().next().next());
			System.out.println(".getPageable().next().next().next()': " + entitiesPage.getPageable().next().next().next().toOptional().isPresent());
			System.out.println("hasNext: " + entitiesPage.hasNext());
			System.out.println("hasPrevious: " + entitiesPage.hasPrevious());
			
			
			log.debug("{}s Page found for pageNumber={}, with pageSize={} and written as JSON", workshopEntityClassName,
				entitiesPage.getNumber(), entitiesPage.getSize());
			
			return ResponseEntity.ok(pagedResourcesToJson);
		} else {
			throw new PersistenceFailure("No " + workshopEntityClassName + "s found!",
				HttpStatus.NOT_FOUND,
				messageSource.getMessage("message.notFound(1)", new Object[]{workshopEntityClassName + "s"},
					LocaleContextHolder.getLocale()));
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
	
	/**
	 * This method sets originally passed parameters to defaults if they are wrong
	 * so that they subsequently could be passed into {@link #getPagedLinks)}
	 *
	 * @param pageSize Will be set to default if it wrong.
	 * @param pageNum  Will be set to default if it wrong.
	 *                 Spring Page interface internally starts page count from 0 so to adapt it to more convenient
	 *                 count from 1 we do '--pageNum' and '++pageNum' when return pageNum back to the end User.
	 * @param orderBy  Will be set to default if it wrong.
	 * @param order    Will be set to default if it wrong.
	 * @return A current Page with the WorkshopEntities collection.
	 */
	@Override
	public Pageable getPageable(Integer pageSize, Integer pageNum, String orderBy, String order) {
		Sort.Direction direction = null;
		if (order != null && !order.isEmpty()) { //The request has to be ordered by asc or desc
			try {
				direction = Sort.Direction.fromString(order);
			} catch (IllegalArgumentException e) { //If 'order' doesn't math asc or desc
				throw new IllegalArguments("'order' parameter must be equal 'asc' or 'desc' value!", e);
			}
		} else { //DESC is the default value if 'order' param is not presented in the Request
			direction = Sort.Direction.DESC;
			order = DEFAULT_ORDER;
		}
		//PageRequest doesn't allow empty parameters strings, so "created" as the default is used
		orderBy = orderBy == null || orderBy.isEmpty() ? DEFAULT_ORDER_BY : orderBy;
		pageSize = (pageSize == null || pageSize <= 0 || pageSize > MAX_PAGE_SIZE) ? DEFAULT_PAGE_SIZE : pageSize;
		pageNum = pageNum == null || pageNum < 0 || pageNum > MAX_PAGE_NUM ? 0 : --pageNum;
		
		return PageRequest.of(
			pageNum,
			pageSize,
			new Sort(direction, orderBy));
	}
	
	private Collection<Link> getPagedLinks(Page page, String orderBy, String order) {
		Collection<Link> pagedLinks = new ArrayList<>(7);
		
		String currentPageRel = "currentPage";
		String previousPageRel = "previousPage";
		String nexPageRel = "nexPage";
		String firstPageRel = "firstPage";
		String lastPageRel = "lastPage";
		String hrefLang = LocaleContextHolder.getLocale().toLanguageTag();
		String media = "json";
		String currentPageTitle = "Page " + page.getNumber() + " of " + page.getTotalPages() + " pages total with " +
			page.getNumberOfElements() + " elements of " + page.getTotalElements() + " elements total.";
		
		Link currentPageLink =
			ControllerLinkBuilder.linkTo(
				ControllerLinkBuilder.methodOn(this.getClass())
					.getAll(page.getSize(), page.getNumber(), orderBy, order))
				.withRel(currentPageRel)
				.withHreflang(hrefLang)
				.withMedia(media)
				.withTitle(currentPageTitle);
		
		pagedLinks.add(currentPageLink);
		
		if (page.getTotalPages() == 1) {
			return pagedLinks;
		}
		if (page.hasPrevious()) {
//			Pageable previousOrFirstPage = pageable.previousOrFirst();
//			Link previousOrFirstPageLink =
//				ControllerLinkBuilder.linkTo(
//					ControllerLinkBuilder.methodOn(this.getClass())
//						.getAll(page.getSize(), previousOrFirstPage.getPageNumber(), orderBy, order))
//					.withRel("previousPage");
//			pagedLinks.add(previousOrFirstPageLink);
		}
//		if (pageable.next() != null)
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
