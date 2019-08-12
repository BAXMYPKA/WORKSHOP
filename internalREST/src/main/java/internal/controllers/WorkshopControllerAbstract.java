package internal.controllers;

import internal.entities.WorkshopEntity;
import internal.entities.WorkshopEntityAbstract;
import internal.entities.hateoasResources.WorkshopEntityResourceAssembler;
import internal.entities.hibernateValidation.MergingValidation;
import internal.entities.hibernateValidation.PersistenceValidation;
import internal.exceptions.IllegalArgumentsException;
import internal.exceptions.InvalidMethodArgumentsException;
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
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.validation.groups.Default;
import java.util.*;

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
public abstract class WorkshopControllerAbstract<T extends WorkshopEntity> implements WorkshopController {
	
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
	@Autowired
	private WorkshopEntitiesServiceAbstract<T> workshopEntitiesService;
	@Autowired
	private WorkshopEntityResourceAssembler<T> workshopEntityResourceAssembler;
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
	 * Http header 'Allow:' with set of HttpMethods allowed within this controller (GET, PUT, DELETE etc)
	 */
	private Set<HttpMethod> httpAllowedMethods;
	
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
		httpAllowedMethods = new HashSet<>(8);
		httpAllowedMethods.addAll(Arrays.asList(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE));
	}
	
	/**
	 * @param pageSize @Nullable. Amount of WorkshopEntities on one page (at once).
	 *                 Default = {@link #DEFAULT_PAGE_SIZE}.
	 * @param pageNum  @Nullable. Number of desires page.
	 *                 Default = 1.
	 *                 (!) Spring Page interface and WorkshopDaoAbstract accordingly both internally starts page count
	 *                 from 0 so to adapt it to more convenient count from 1 for the end Users we do '--pageNum'
	 *                 before passing it into domain logic and then '++pageNum' when returning pageNum back to the end
	 *                 User.
	 * @param orderBy  @Nullable The name of property the WorkshopEntities have to be ordered by.
	 *                 Default = {@link #DEFAULT_ORDER_BY}
	 * @param order    @Nullable. 'asc' or 'desc' (ascending or descending) order.
	 *                 Default = {@link #DEFAULT_ORDER}
	 * @return Paged and sorted WorkshopEntities collection with embedded navigation Links through it (i.e. prevPage,
	 * nextPage etc). Also every WorkshopEntity has its own self-link to be obtained as a HATEOAS resource.
	 */
	@Override
	@GetMapping
	public ResponseEntity<String> getAll(
		@RequestParam(value = "pageSize", required = false, defaultValue = "${page.size.default}") Integer pageSize,
		@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
		@RequestParam(name = "order-by", required = false, defaultValue = "${default.orderBy}") String orderBy,
		@RequestParam(name = "order", required = false, defaultValue = "${default.order}") String order) {
		
		Pageable pageRequest = getPageable(pageSize, pageNum, orderBy, order);
		Page<T> entitiesPage = workshopEntitiesService.findAllEntities(pageRequest, orderBy);
		//Add a self-Link to every WorkshopEntity to be a Resource
//		entitiesPage.get().forEach(this::addSelfLink);
		
//		Resources<T> resources = new Resources<>(entitiesPage.getContent());
		//The following are preparing paged Links for this resources (i.e. nextPage, previousPage etc)
//		Collection<Link> pagedLinks = getPagedLinks(entitiesPage, orderBy, order);
		
//		resources.add(pagedLinks);
		
		Resources<T> entitiesPageResources = workshopEntityResourceAssembler.toPagedResources(entitiesPage);
		
		String pagedResourcesToJson = jsonServiceUtils.workshopEntityObjectsToJson(entitiesPageResources);
		
		log.debug("{}s Page with pageNumber={} and pageSize={} has been written as JSON",
			workshopEntityClassName, entitiesPage.getNumber(), entitiesPage.getSize());
		
		ResponseEntity<String> responseEntity = new ResponseEntity<>(pagedResourcesToJson, HttpStatus.OK);
//		responseEntity.getHeaders().setAllow(httpAllowedMethods);
		return responseEntity;
	}
	
/*
	@Override
	@GetMapping("/{id}")
	public ResponseEntity<String> getOne(@PathVariable("id") long id) {
		T entity = workshopEntityClass.cast(workshopEntitiesService.findById(id));
		addSelfLink(entity);
		String entityToJson = jsonServiceUtils.workshopEntityObjectsToJson(entity);
		
		ResponseEntity<String> responseEntity = new ResponseEntity<>(entityToJson, HttpStatus.OK);
//		responseEntity.getHeaders().setAllow(httpAllowedMethods);
		return responseEntity;
	}
*/
	
	@Override
	@GetMapping("/{id}")
	public ResponseEntity<String> getOne(@PathVariable("id") long id) {
		T entity = workshopEntityClass.cast(workshopEntitiesService.findById(id));
		
		Resource<T> entityResource = workshopEntityResourceAssembler.toResource(entity);
		
//		addSelfLink(entity);
		
		String entityToJson = jsonServiceUtils.workshopEntityObjectsToJson(entityResource);
		
		ResponseEntity<String> responseEntity = new ResponseEntity<>(entityToJson, HttpStatus.OK);
//		responseEntity.getHeaders().setAllow();
		return responseEntity;
	}
	
	
	/**
	 * WorkshopEntity may contain some included WorkshopEntities without identifiers or 'identifier = 0' -
	 * they all will be treated as new ones and persisted in the DataBase.
	 * If any of them will throw an Exception during a persistence process - the whole WorkshopEntity won't be saved!
	 *
	 * @param workshopEntity WorkshopEntity object as JSON
	 * @return Either persisted WorkshopEntity as a HATEOAS resource with a self-obtainable Link and the
	 * 'identifier' (id) set or a HttpStatus error with its description.
	 */
	@Override
	@PostMapping(consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	public ResponseEntity<String> postOne(@Validated(value = {PersistenceValidation.class, Default.class})
										  @RequestBody WorkshopEntity workshopEntity,
										  BindingResult bindingResult) {
		if (bindingResult.hasErrors()) { //To be processed by ExceptionHandlerController.validationFailure()
			throw new InvalidMethodArgumentsException(
				"The passed " + workshopEntityClassName + " Json object has errors!", bindingResult);
		}
		
		T persistedWorkshopEntity = workshopEntitiesService.persistEntity(workshopEntityClass.cast(workshopEntity));
		addSelfLink(persistedWorkshopEntity);
		String jsonPersistedWorkshopEntity = jsonServiceUtils.workshopEntityObjectsToJson(persistedWorkshopEntity);
		
		ResponseEntity<String> responseEntity = new ResponseEntity<>(jsonPersistedWorkshopEntity, HttpStatus.CREATED);
		responseEntity.getHeaders().setAllow(httpAllowedMethods);
		return responseEntity;
	}
	
	@Override
	@PutMapping(path = "/{id}", consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	public ResponseEntity<String> putOne(@PathVariable(name = "id") long id,
										 @Validated(value = {MergingValidation.class, Default.class})
										 @RequestBody WorkshopEntity workshopEntity,
										 BindingResult bindingResult) {
		if (workshopEntity.getIdentifier() != id) {
			bindingResult.addError(
				new FieldError(workshopEntityClassName, "ID", messageSource.getMessage(
					"error.propertyHasToBe(2)", new Object[]{workshopEntityClassName + " ID", "='/{pathVariableID}'"},
					LocaleContextHolder.getLocale())));
			throw new InvalidMethodArgumentsException(
				"The passed " + workshopEntityClassName + " Json object has errors!", bindingResult);
		} else if (bindingResult.hasErrors()) {
			throw new InvalidMethodArgumentsException(
				"The passed " + workshopEntityClassName + " Json object has errors!", bindingResult);
		}
		T mergedWorkshopEntity = workshopEntitiesService.mergeEntity(workshopEntityClass.cast(workshopEntity));
		addSelfLink(mergedWorkshopEntity);
		String jsonMergedEntity = jsonServiceUtils.workshopEntityObjectsToJson(mergedWorkshopEntity);
		
		ResponseEntity<String> responseEntity = new ResponseEntity<>(jsonMergedEntity, HttpStatus.OK);
		responseEntity.getHeaders().setAllow(httpAllowedMethods);
		return responseEntity;
	}
	
	@Override
	@DeleteMapping(path = "/{id}")
	public ResponseEntity<String> deleteOne(@PathVariable(name = "id") long id) {
		workshopEntitiesService.removeEntity(id);
		
		String localizedMessage = messageSource.getMessage(
			"message.deletedSuccessfully(1)", new Object[]{workshopEntityClassName + " id = " + id},
			LocaleContextHolder.getLocale());
		
		ResponseEntity<String> responseEntity = new ResponseEntity<>(localizedMessage, HttpStatus.NO_CONTENT);
		responseEntity.getHeaders().setAllow(httpAllowedMethods);
		return responseEntity;
	}
	
	//TODO: to do a separate Hateoas service class to pass all the following methods in it
	
	/**
	 * This method sets originally passed parameters to defaults if they are wrong
	 * so that they subsequently could be passed into {@link #getPagedLinks)}
	 *
	 * @param pageSize Will be set to default if it's wrong.
	 * @param pageNum  Spring Page interface internally starts pages count from 0 so to adapt it to more convenient
	 *                 count from 1 we do '--pageNum' to prepare Page and then '++pageNum' when return pageNum back to
	 *                 the end User.
	 *                 Will be set to default if it is wrong.
	 * @param orderBy  Property the collection will be ordered by. Will be set to default if it wrong.
	 * @param order    'asc' or 'desc' Will be set to default if it's wrong.
	 * @return A current Page with the WorkshopEntities collection.
	 */
	@Override
	public Pageable getPageable(Integer pageSize, Integer pageNum, String orderBy, String order) {
		Sort.Direction direction;
		if (order != null && !order.isEmpty()) { //The request may have 'asc' or 'desc' order
			try {
				direction = Sort.Direction.fromString(order);
			} catch (IllegalArgumentException e) { //If 'order' doesn't math asc or desc
				throw new IllegalArgumentsException("'order' parameter must be equal 'asc' or 'desc' value!",
					HttpStatus.NOT_ACCEPTABLE, messageSource.getMessage(
					"error.propertyHasToBe(2)", new Object[]{"order", "'asc' || 'desc'"},
					LocaleContextHolder.getLocale()), e);
			}
		} else { //'desc' is the default value if 'order' param is not presented in the Request
			direction = Sort.Direction.DESC;
			order = DEFAULT_ORDER; //This is in place to correct the passed value for the future use
		}
		orderBy = orderBy == null || orderBy.isEmpty() ? DEFAULT_ORDER_BY : orderBy;
		pageSize = (pageSize == null || pageSize <= 0 || pageSize > MAX_PAGE_SIZE) ? DEFAULT_PAGE_SIZE : pageSize;
		//Even if the Request will contain pageNum=0 we don't have to accept it as for the end Users page count
		// starts from 1 (despite internally Spring Page uses start from 0)
		pageNum = pageNum == null || pageNum <= 0 || pageNum > MAX_PAGE_NUM ? 0 : --pageNum;
		
		return PageRequest.of(
			pageNum,
			pageSize,
			new Sort(direction, orderBy));
	}
	
	/**
	 * Don't forget: inner String Page starts with 0 but outer Link for Users starts with 1!
	 * So for page.getNumber() we must add +1
	 */
	private Collection<Link> getPagedLinks(Page page, String orderBy, String order) {
		Collection<Link> pagedLinks = new ArrayList<>(7);
		
		String currentPageRel = "currentPage";
		String previousPageRel = "previousPage";
		String nexPageRel = "nextPage";
		String firstPageRel = "firstPage";
		String lastPageRel = "lastPage";
		String hrefLang = LocaleContextHolder.getLocale().toLanguageTag();
		String media = "application/json; charset=utf-8";
		String lastPageTitle = "Page " + (page.getTotalPages());
		String currentPageTitle = "Page " + (page.getNumber() + 1) + " of " + page.getTotalPages() + " pages total " +
			"with " + page.getNumberOfElements() + " elements of " + page.getTotalElements() + " elements total.";
		
		Link currentPageLink = getPagedLink(page.getPageable(), page.getSize(), orderBy, order, currentPageRel,
			hrefLang, media, currentPageTitle);
		
		pagedLinks.add(currentPageLink);
		
		if (page.getTotalPages() == 1) {
			return pagedLinks;
		}
		if (page.hasPrevious()) {
			pagedLinks.add(getPagedLink(page.previousPageable(), page.getSize(), orderBy, order, previousPageRel,
				hrefLang, media, null));
		}
		if (page.hasNext()) {
			pagedLinks.add(getPagedLink(page.nextPageable(), page.getSize(), orderBy, order, nexPageRel, hrefLang, media,
				null));
		}
		if (!page.isFirst()) { //Add FirstPage
			pagedLinks.add(getPagedLink(page.getPageable().first(), page.getSize(), orderBy, order, firstPageRel,
				hrefLang, media, null));
		}
		if (!page.isLast()) { //Add LastPage
			Link lastPageLink =
				ControllerLinkBuilder.linkTo(
					ControllerLinkBuilder.methodOn(this.getClass())
						.getAll(page.getSize(), page.getTotalPages(), orderBy, order))
					.withRel(lastPageRel)
					.withHreflang(hrefLang)
					.withMedia(media)
					.withTitle(lastPageTitle);
			
			pagedLinks.add(lastPageLink);
		}
		
		return pagedLinks;
	}
	
	/**
	 * Don't forget: inner String Page starts with 0 but outer Link for Users starts with 1!
	 * So for page.getNumber() we must add +1
	 */
	private Link getPagedLink(Pageable pageable, int pageSize, String orderBy, String order, String relation,
							  String hrefLang, String media, @Nullable String title) {
		title = title == null ? "Page " + (pageable.getPageNumber() + 1) : title;
		
		Link link =
			ControllerLinkBuilder.linkTo(
				ControllerLinkBuilder.methodOn(this.getClass())
					.getAll(pageSize, pageable.getPageNumber() + 1, orderBy, order))
				.withRel(relation)
				.withHreflang(hrefLang)
				.withMedia(media)
				.withTitle(title);
		return link;
	}
	
	private void addSelfLink(T workshopEntity) {
		WorkshopEntityAbstract workshopEntityAbstract = (WorkshopEntityAbstract) workshopEntity;
		
		Link selfGetLink = entityLinks
			.linkForSingleResource(workshopEntityClass, workshopEntity.getIdentifier()).withSelfRel()
			.withHreflang(LocaleContextHolder.getLocale().toLanguageTag()).withMedia("application/json; charset=utf-8")
			.withTitle("title");
		
		workshopEntityAbstract.add(selfGetLink);
	}
	
	@Override
	@PostConstruct
	public void postConstruct() {
		allWorkshopEntitiesLink = ControllerLinkBuilder.linkTo(
			ControllerLinkBuilder.methodOn(
				this.getClass()).getAll(DEFAULT_PAGE_SIZE, 1, DEFAULT_ORDER_BY, DEFAULT_ORDER)
		).withRel("getAll" + workshopEntityClassName + "s");
	}
}
