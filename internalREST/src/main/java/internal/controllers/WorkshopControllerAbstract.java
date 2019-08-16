package internal.controllers;

import internal.entities.WorkshopEntity;
import internal.entities.hateoasResources.WorkshopEntityResourceAssemblerAbstract;
import internal.entities.hibernateValidation.MergingValidation;
import internal.entities.hibernateValidation.PersistenceValidation;
import internal.exceptions.IllegalArgumentsException;
import internal.exceptions.InvalidMethodArgumentsException;
import internal.services.WorkshopEntitiesServiceAbstract;
import internal.services.serviceUtils.JsonServiceUtils;
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
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
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
	private WorkshopEntityResourceAssemblerAbstract<T> workshopEntityResourceAssembler;
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
		Resources<Resource<T>> entitiesPageResources = workshopEntityResourceAssembler.toPagedResources(entitiesPage);
		String pagedResourcesToJson = jsonServiceUtils.workshopEntityObjectsToJson(entitiesPageResources);
		log.debug("{}s Page with pageNumber={} and pageSize={} has been written as JSON",
			  workshopEntityClassName, entitiesPage.getNumber(), entitiesPage.getSize());
		return new ResponseEntity<>(pagedResourcesToJson, HttpStatus.OK);
	}
	
	@Override
	@GetMapping("/{id}")
	public ResponseEntity<String> getOne(@PathVariable("id") long id) {
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
	public ResponseEntity<String> postOne(@Validated(value = {PersistenceValidation.class, Default.class})
										  @RequestBody WorkshopEntity workshopEntity,
										  BindingResult bindingResult) {
		if (bindingResult.hasErrors()) { //To be processed by ExceptionHandlerController.validationFailure()
			throw new InvalidMethodArgumentsException(
				  "The passed " + workshopEntityClassName + " Json object has errors!", bindingResult);
		}
		
		T persistedWorkshopEntity = workshopEntitiesService.persistEntity(workshopEntityClass.cast(workshopEntity));
		Resource<T> persistedWorkshopEntityResource = workshopEntityResourceAssembler.toResource(persistedWorkshopEntity);
		String jsonPersistedWorkshopEntity = jsonServiceUtils.workshopEntityObjectsToJson(persistedWorkshopEntityResource);
		return new ResponseEntity<>(jsonPersistedWorkshopEntity, HttpStatus.CREATED);
	}
	
	@Override
	@PutMapping(path = "/{id}", consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	public ResponseEntity<String> putOne(@PathVariable(name = "id") long id,
										 @Validated(value = {MergingValidation.class, Default.class})
										 @RequestBody WorkshopEntity workshopEntity,
										 BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			throw new InvalidMethodArgumentsException(
				  "The passed " + workshopEntityClassName + " Json object has errors!", bindingResult);
		}
		T mergedWorkshopEntity = workshopEntitiesService.mergeEntity(workshopEntityClass.cast(workshopEntity));
		Resource<T> mergedWorkshopEntityResource = workshopEntityResourceAssembler.toResource(mergedWorkshopEntity);
		String jsonMergedEntity = jsonServiceUtils.workshopEntityObjectsToJson(mergedWorkshopEntityResource);
		return new ResponseEntity<>(jsonMergedEntity, HttpStatus.OK);
	}
	
	@Override
	@DeleteMapping(path = "/{id}")
	public ResponseEntity<String> deleteOne(@PathVariable(name = "id") long id) {
		workshopEntitiesService.removeEntity(id);
		
		String localizedMessage = messageSource.getMessage(
			  "message.deletedSuccessfully(1)", new Object[]{workshopEntityClassName + " id = " + id},
			  LocaleContextHolder.getLocale());
		
		return ResponseEntity.status(HttpStatus.NO_CONTENT).body(localizedMessage);
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
	
	@Override
	@PostConstruct
	public void postConstruct() {
		//TODO: to remove if this method is dummy
	}
}
