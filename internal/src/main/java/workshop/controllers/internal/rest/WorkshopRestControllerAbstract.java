package workshop.controllers.internal.rest;

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
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import workshop.internal.entities.WorkshopEntity;
import workshop.internal.entities.hibernateValidation.PersistenceValidation;
import workshop.internal.entities.hibernateValidation.MergingValidation;
import workshop.internal.exceptions.IllegalArgumentsException;
import workshop.internal.exceptions.InvalidMethodArgumentsException;
import workshop.internal.hateoasResources.WorkshopEntitiesResourceAssemblerAbstract;
import workshop.internal.services.WorkshopEntitiesServiceAbstract;
import workshop.internal.services.serviceUtils.JsonServiceUtils;

import javax.validation.groups.Default;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 1. Every REST WorkshopController has to have an instance variable of EntitiesServiceAbstract<T extends WorkshopEntity>
 * so that to get the concrete type of WorkshopEntity to operate with.
 * 2. Every REST controller has to be annotated with:
 * a. "@RequestMapping(path = "/internal/<workshop_entities>", produces = {MediaTypes.HAL_JSON_UTF8_VALUE})"
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
public abstract class WorkshopRestControllerAbstract<T extends WorkshopEntity> implements WorkshopRestController<T> {
	
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
	private WorkshopEntitiesResourceAssemblerAbstract<T> workshopEntityResourceAssembler;
	private Class<T> workshopEntityClass;
	/**
	 * Just a simple name for simplified "workshopEntityClass.getSimpleName()"
	 */
	private String workshopEntityClassName;
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
	public WorkshopRestControllerAbstract(
		WorkshopEntitiesServiceAbstract<T> workshopEntitiesService,
		WorkshopEntitiesResourceAssemblerAbstract<T> workshopEntitiesResourceAssemblerAbstract) {
		
		this.workshopEntitiesService = workshopEntitiesService;
		this.workshopEntityClass = workshopEntitiesService.getEntityClass();
		this.workshopEntityResourceAssembler = workshopEntitiesResourceAssemblerAbstract;
		workshopEntityClassName = workshopEntityClass.getSimpleName();
		httpAllowedMethods = new HashSet<>(8);
		httpAllowedMethods.addAll(Arrays.asList(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE));
	}
	
	/**
	 * Need to be overridden with {@link PreAuthorize}("hasPermission('#authentication,
	 * 'WorkshopEntitySimpleClassName', 'get')").
	 *
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
	@PreAuthorize("hasPermission(#webRequest, 'get')")
	public ResponseEntity<String> getAll(
		@RequestParam(value = "pageSize", required = false, defaultValue = "${page.size.default}") Integer pageSize,
		@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
		@RequestParam(name = "order-by", required = false, defaultValue = "${default.orderBy}") String orderBy,
		@RequestParam(name = "order", required = false, defaultValue = "${default.order}") String order,
		@Nullable WebRequest webRequest) {
		
		Pageable pageRequest = getPageable(pageSize, pageNum, orderBy, order);
		Page<T> entitiesPage = workshopEntitiesService.findAllEntities(pageRequest);
		
		Resources<Resource<T>> entitiesPageResources = workshopEntityResourceAssembler.toPagedResources(entitiesPage);
		
		String pagedResourcesToJson = jsonServiceUtils.workshopEntityObjectsToJson(entitiesPageResources);
		log.debug("{}s Page with pageNumber={} and pageSize={} has been written as JSON",
			workshopEntityClassName, entitiesPage.getNumber(), entitiesPage.getSize());
		return new ResponseEntity<>(pagedResourcesToJson, HttpStatus.OK);
	}
	
	
	/**
	 * Need to be overridden with {@link PreAuthorize}("hasPermission('#authentication,
	 * 'WorkshopEntitySimpleClassName', 'get')").
	 */
	@Override
	@GetMapping(path = "/{id}")
	@PreAuthorize("hasPermission(#webRequest, 'get')")
	public ResponseEntity<String> getOne(@PathVariable(name = "id") Long id, @Nullable WebRequest webRequest) {
		
		T entity = workshopEntityClass.cast(workshopEntitiesService.findById(id));
		Resource<T> entityResource = workshopEntityResourceAssembler.toResource(entity);
		String entityToJson = jsonServiceUtils.workshopEntityObjectsToJson(entityResource);
		
		return new ResponseEntity<>(entityToJson, HttpStatus.OK);
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
	@PreAuthorize("hasPermission(#webRequest, 'post')")
	public ResponseEntity<String> postOne(
		@Validated(value = {PersistenceValidation.class}) @RequestBody T workshopEntity,
		BindingResult bindingResult,
		WebRequest webRequest) {
		
		validateBindingResult(bindingResult);
		T persistedWorkshopEntity = workshopEntitiesService.persistEntity(workshopEntityClass.cast(workshopEntity));
		Resource<T> persistedWorkshopEntityResource = workshopEntityResourceAssembler.toResource(persistedWorkshopEntity);
		String jsonPersistedWorkshopEntity = jsonServiceUtils.workshopEntityObjectsToJson(persistedWorkshopEntityResource);
		return new ResponseEntity<>(jsonPersistedWorkshopEntity, HttpStatus.CREATED);
	}
	
	
	@Override
	@PutMapping(path = "/{id}", consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	@PreAuthorize("hasPermission(#webRequest, 'put')")
	public ResponseEntity<String> putOne(
		@PathVariable(name = "id") Long id,
		@Validated(value = {MergingValidation.class, Default.class})
		@RequestBody T workshopEntity,
		BindingResult bindingResult,
		WebRequest webRequest) {
		
		validateBindingResult(bindingResult);
		T mergedWorkshopEntity = workshopEntitiesService.mergeEntity(workshopEntityClass.cast(workshopEntity));
		Resource<T> mergedWorkshopEntityResource = workshopEntityResourceAssembler.toResource(mergedWorkshopEntity);
		String jsonMergedEntity = jsonServiceUtils.workshopEntityObjectsToJson(mergedWorkshopEntityResource);
		return new ResponseEntity<>(jsonMergedEntity, HttpStatus.OK);
	}
	
	/**
	 * Need to be overridden with {@link PreAuthorize}("hasPermission('#authentication,
	 * 'WorkshopEntitySimpleClassName', 'delete')").
	 */
	@Override
	@DeleteMapping(path = "/{id}")
	@PreAuthorize("hasPermission(#webRequest, 'delete')")
	public ResponseEntity<String> deleteOne(@PathVariable(name = "id") Long id, @Nullable WebRequest webRequest) {
		
		workshopEntitiesService.removeEntity(id);
		return ResponseEntity.status(HttpStatus.NO_CONTENT)
			.body(getDeleteMessageSuccessLocalized(workshopEntityClassName + " id = " + id));
	}
	
	/**
	 * This method sets originally passed parameters to defaults if they are wrong
	 * so that they subsequently could be passed as valid data for getting Page<T>
	 *
	 * @param pageSize Will be set to default if it's <= 0
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
	 * Validates the given BindingResult and throws the InvalidMethodArgumentsException to be processed by
	 * ExceptionHandlerController.validationFailure()
	 *
	 * @throws InvalidMethodArgumentsException to be processed by ExceptionHandlerController.validationFailure()
	 */
	protected void validateBindingResult(BindingResult bindingResult) throws InvalidMethodArgumentsException {
		if (bindingResult.hasErrors()) { //To be processed by ExceptionHandlerController.validationFailure()
			throw new InvalidMethodArgumentsException(
				"The passed " + workshopEntityClassName + " Json object has errors!", bindingResult);
		}
	}
	
	protected String getDeleteMessageSuccessLocalized(String whatIsDeleted) {
		String localizedMessage = messageSource.getMessage(
			"message.deletedSuccessfully(1)", new Object[]{whatIsDeleted},
			LocaleContextHolder.getLocale());
		return localizedMessage;
	}
	
	protected ResponseEntity<String> getResponseEntityWithErrorMessage(HttpStatus httpStatus, String messageBody)
		throws IllegalArgumentException {
		if (httpStatus == null || messageBody == null) {
			throw new IllegalArgumentException("HttpStatus or messageBody cannot be null!");
		}
		messageBody = "{\"errorMessage\":\"" + messageBody + "\"}";
		return ResponseEntity.status(httpStatus).contentType(MediaType.APPLICATION_JSON_UTF8).body(messageBody);
	}
	
}
