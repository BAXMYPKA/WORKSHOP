package internal.services;

import internal.dao.WorkshopEntitiesDaoAbstract;
import internal.entities.WorkshopEntity;
import internal.exceptions.EntityNotFoundException;
import internal.exceptions.IllegalArgumentsException;
import internal.exceptions.InternalServerErrorException;
import internal.exceptions.PersistenceFailureException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityExistsException;
import javax.persistence.PersistenceException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Always returns ready-to-use WorkshopEntities or throws WorkshopExceptions for ExceptionHandlerController with
 * appropriate HttpStatuses and
 * localized messages for end-users.
 * This class also intended to throw WorkshopException with fully localized messages with appropriate HttpStatus codes
 * to be shown to the end Users.
 *
 * @param <T> The class type of the Entity its instance will be serving to.
 */
@Getter(AccessLevel.PROTECTED)
@Setter(AccessLevel.PROTECTED)
@Slf4j
@NoArgsConstructor
@Transactional(propagation = Propagation.REQUIRED)
@Service
public abstract class WorkshopEntitiesServiceAbstract <T extends WorkshopEntity> {
	
	@Value("${page.size.default}")
	@Getter(AccessLevel.PUBLIC)
	private int DEFAULT_PAGE_SIZE;
	@Value("${page.max_num}")
	@Getter(AccessLevel.PUBLIC)
	private int MAX_PAGE_NUM;
	@Value("${page.size.max}")
	@Getter(AccessLevel.PUBLIC)
	private int MAX_PAGE_SIZE;
	@Value("${default.orderBy}")
	@Getter(AccessLevel.PUBLIC)
	private String DEFAULT_ORDER_BY;
	@Value("${default.order}")
	@Getter(AccessLevel.PUBLIC)
	private String DEFAULT_ORDER;
	@Autowired
	private MessageSource messageSource;
	private WorkshopEntitiesDaoAbstract<T, Long> workshopEntitiesDaoAbstract;
	@Getter(AccessLevel.PUBLIC)
	private Class<T> entityClass;
	private String entityClassSimpleName;
	
	/**
	 * @param workshopEntitiesDaoAbstract A concrete implementation of the EntitiesDaoAbstract<T,K> for the concrete
	 *                                    implementation of this EntitiesServiceAbstract<T>.
	 *                                    To be injected to all the superclasses.
	 *                                    For instance, 'public OrdersService(OrdersDao ordersDao)'
	 */
	public WorkshopEntitiesServiceAbstract(WorkshopEntitiesDaoAbstract<T, Long> workshopEntitiesDaoAbstract) {
		this.workshopEntitiesDaoAbstract = workshopEntitiesDaoAbstract;
		setEntityClass(workshopEntitiesDaoAbstract.getEntityClass());
		entityClassSimpleName = entityClass.getSimpleName();
		log.trace("{} initialized successfully", entityClass.getSimpleName());
	}
	
	/**
	 * @param id WorkshopEntity ID
	 * @return A found Entity or throws EntityNotFoundException with appropriate HttpStatus and localized message for
	 * the end Users. That Exception is intended to be intercept by ExceptionHandler controller.
	 * @throws IllegalArgumentsException With appropriate HttpStatus and fully localized error message for the end users
	 *                                   if the given parameter id is null either zero or below zero.
	 * @throws EntityNotFoundException   If nothing was found. With appropriate HttpStatus and fully localized message
	 *                                   for being intercepted by ExceptionHandlerController to be sent to the end users.
	 */
	@Transactional(propagation = Propagation.REQUIRED, readOnly = true, isolation = Isolation.READ_COMMITTED)
	public T findById(long id) throws IllegalArgumentsException, EntityNotFoundException {
		
		verifyIdForNullZeroBelowZero(id);
		
		return workshopEntitiesDaoAbstract.findById(id).orElseThrow(() ->
			new EntityNotFoundException("No " + entityClass.getSimpleName() + " with identifier=" + id + " was found!",
				HttpStatus.NOT_FOUND,
				messageSource.getMessage("message.notFound(2)",
					new Object[]{entityClass.getSimpleName(), "identifier=" + id},
					LocaleContextHolder.getLocale())));
	}
	
	/**
	 * AKA 'persistOrUpdate'. If an Entity doesn't presented in the DataBase it will be persisted.
	 * Otherwise it will update its properties in the DataBase.
	 *
	 * @param entity If an Entity.identifier = 0 it will be persisted. If an Entity.identifier > 0 it will be merged (updated into DB)
	 * @return Persisted (or merged) managed copy of the Entity.
	 * @throws IllegalArgumentsException                  If an Entity == null, with localized message and HttpStatus.NOT_ACCEPTABLE
	 * @throws AuthenticationCredentialsNotFoundException If this method is trying to be performed without an appropriate
	 *                                                    Authentication within the Spring's SecurityContext.
	 * @throws PersistenceFailureException                If some properties of the given Entity are incorrect.
	 * @throws EntityNotFoundException                    If the given Entity.ID is not presented in the DataBase.
	 */
//	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE,
//				   noRollbackFor ={InvalidDataAccessApiUsageException.class})
/*
	public T persistOrMergeEntity(T entity)
		throws IllegalArgumentsException, AuthenticationCredentialsNotFoundException, PersistenceFailureException {
		
		if (entity == null) {
			throw new IllegalArgumentsException(
				"The entity argument cannot be null!", "httpStatus.notAcceptable.null", HttpStatus.NOT_ACCEPTABLE);
		}
		Optional<T> persistedEntity;
		try {
			persistedEntity = workshopEntitiesDaoAbstract.persistEntity(entity);
		} catch (EntityExistsException | PersistentObjectException | InvalidDataAccessApiUsageException ex) {
			log.debug("{} exists, trying to merge it...", entityClass.getSimpleName(), ex);
			persistedEntity = workshopEntitiesDaoAbstract.mergeEntity(entity);
		} catch (IllegalArgumentException ie) { //Can be thrown by EntityManager if it is a removed Entity
			throw new EntityNotFoundException(
				"Couldn't neither save nor update the given " + entityClass.getSimpleName() + "! Check its properties",
				HttpStatus.GONE,
				messageSource.getMessage("error.saveOrUpdate(1)", new Object[]{entityClass.getSimpleName()},
					LocaleContextHolder.getLocale()),
				ie);
		}
		return persistedEntity.orElseThrow(() -> new PersistenceFailureException(
			"Couldn't neither save nor update the given " + entityClass.getSimpleName() + "! Check its properties."));
	}
*/
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
	public T persistOrMergeEntity(T entity)
		throws IllegalArgumentsException, AuthenticationCredentialsNotFoundException, PersistenceFailureException {
		
		if (entity == null) {
			throw new IllegalArgumentsException(
				"The entity argument cannot be null!", "httpStatus.notAcceptable.null", HttpStatus.NOT_ACCEPTABLE);
		}
		Optional<T> persistedEntity;
		if (entity.getIdentifier() == null || entity.getIdentifier() == 0) {
			persistedEntity = workshopEntitiesDaoAbstract.persistEntity(entity);
		} else {
			persistedEntity = workshopEntitiesDaoAbstract.mergeEntity(entity);
		}
		return persistedEntity.orElseThrow(() -> new PersistenceFailureException(
			"Couldn't neither save nor update the given " + entityClass.getSimpleName() + "! Check its properties."));
	}
	
	/**
	 * @param entity Array of Entities to be saved or persisted
	 * @return Collection of the managed Entities copies.
	 * @throws IllegalArgumentsException                  If an Entity == null, with localized message and HttpStatus.NOT_ACCEPTABLE
	 * @throws AuthenticationCredentialsNotFoundException If this method is trying to be performed without an appropriate
	 *                                                    Authentication within the Spring's SecurityContext.
	 * @throws PersistenceFailureException                If some properties of the given Entity are incorrect.
	 * @throws EntityNotFoundException                    If the given Entity.ID is not presented in the DataBase.
	 */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
	public Collection<T> persistOrMergeEntities(T... entity)
		throws IllegalArgumentsException, AuthenticationCredentialsNotFoundException, PersistenceFailureException, EntityNotFoundException {
		
		if (entity == null || entity.length == 0) {
			throw new IllegalArgumentsException(
				"The entities arguments cannot be null or empty!", "httpStatus.notAcceptable.null", HttpStatus.NOT_ACCEPTABLE);
		}
		
		List<T> persistedOrMergedEntities = Stream.of(entity).peek(this::persistEntity).collect(Collectors.toList());
		return persistedOrMergedEntities;
	}
	
	/**
	 * @param entity WorkshopEntity instance. Has to have 'identifier' property == null or 0.
	 * @return Persisted and managed copy of a given WorkshopEntity with the Identifier.
	 * @throws IllegalArgumentsException   with 422 HttpStatus.UNPROCESSABLE_ENTITY in a given WorkshopEntity is null!
	 * @throws EntityExistsException       If such a WorkshopEntity is already exists.
	 * @throws IllegalArgumentException    If a given Entity is not an entity either is null or its identifier != 0
	 * @throws PersistenceFailureException 1) With 409 HttpStatus.CONFLICT in case of persistence failure.
	 *                                     It may happen due to wrong properties.
	 *                                     2) With 422 HttpStatus.UNPROCESSABLE_ENTITY if its 'identifier' not null
	 *                                     and > 0. As to be persisted Entities dont't have to have their own ids.
	 */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
	public T persistEntity(T entity)
		throws IllegalArgumentException, IllegalArgumentsException, EntityExistsException, PersistenceFailureException {
		if (entity == null) {
			throw new IllegalArgumentsException("Entity cannot be null!", "httpStatus.notAcceptable.null",
				HttpStatus.UNPROCESSABLE_ENTITY);
		} else if (entity.getIdentifier() != null && entity.getIdentifier() > 0) {
			throw new PersistenceFailureException("Id (identifier) must by null or zero!",
				HttpStatus.UNPROCESSABLE_ENTITY, messageSource.getMessage(
				"error.propertyHasToBe(2)",
				new Object[]{entity.getClass().getSimpleName() + ".ID", "empty (null) or 0"},
				LocaleContextHolder.getLocale()));
		}
		return workshopEntitiesDaoAbstract.persistEntity(entity).orElseThrow(() -> new PersistenceFailureException(
			"Couldn't save" + entityClass.getSimpleName() + "! Check its properties.",
			HttpStatus.CONFLICT,
			messageSource.getMessage("error.saveFailure(1)", new Object[]{entityClass.getSimpleName()},
				LocaleContextHolder.getLocale())));
	}
	
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
	public Collection<T> persistEntities(T... entity)
		throws IllegalArgumentsException, AuthenticationCredentialsNotFoundException, PersistenceFailureException, EntityNotFoundException {
		
		if (entity == null || entity.length == 0) {
			throw new IllegalArgumentsException(
				"The entities arguments cannot be null or empty!", "httpStatus.notAcceptable.null", HttpStatus.NOT_ACCEPTABLE);
		}
		
		List<T> persistedEntities = Stream.of(entity).peek(this::persistEntity).collect(Collectors.toList());
		return persistedEntities;
	}
	
	/**
	 * @param entity Entity to be merged (updated) in the DataBase
	 * @return An updated and managed copy of the given entity.
	 * @throws IllegalArgumentException    If the given entity == null or its id == null or id <= 0. As to be updated
	 *                                     (merged) this WorkshopEntity has to be exist in the DataBase.
	 * @throws PersistenceFailureException If the given entity is in the removed state (not found in the DataBase)
	 *                                     with 410 HttpStatus.GONE and the explicit localized message for the end User.
	 */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
	public T mergeEntity(T entity) throws IllegalArgumentException, PersistenceFailureException {
		if (entity == null) {
			throw new IllegalArgumentsException("Entity cannot be null!", "httpStatus.notAcceptable.null",
				HttpStatus.UNPROCESSABLE_ENTITY);
		} else if (entity.getIdentifier() == null || entity.getIdentifier() <= 0) {
			throw new PersistenceFailureException("Id (identifier) must by null or zero!",
				HttpStatus.UNPROCESSABLE_ENTITY, messageSource.getMessage(
				"error.propertyHasToBe(2)", new Object[]{"id", "> 0"}, LocaleContextHolder.getLocale()));
		}
		return workshopEntitiesDaoAbstract.mergeEntity(entity).orElseThrow(() -> new PersistenceFailureException(
			"Updating the " + entityClass.getSimpleName() + " is failed! Such an object wasn't found to be updated!",
			HttpStatus.GONE));
	}
	
	
	/**
	 * @param entity The WorkshopEntity to be removed
	 * @throws IllegalArgumentsException If the given WorkshopEntity == null
	 * @throws EntityNotFoundException   If such an WorkshopEntity was not found in the DataBase
	 */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
	public void removeEntity(T entity) throws IllegalArgumentsException, EntityNotFoundException {
		if (entity == null) {
			throw new IllegalArgumentsException("Entity cannot be null!", "httpStatus.notAcceptable.null",
				HttpStatus.UNPROCESSABLE_ENTITY);
		}
		try {
			workshopEntitiesDaoAbstract.removeEntity(entity);
			log.debug("{} successfully removed.", entityClass.getSimpleName());
		} catch (IllegalArgumentException ex) {
			throw new EntityNotFoundException(
				"Removing the " + entityClass.getSimpleName() + " is failed! Such an object wasn't found to be " +
					"removed!",
				HttpStatus.NOT_FOUND,
				messageSource.getMessage(
					"error.removeNotFoundFailure(1)",
					new Object[]{entityClass.getSimpleName()},
					LocaleContextHolder.getLocale()),
				ex);
		}
	}
	
	/**
	 * @param id Accepts only above zero values.
	 * @throws IllegalArgumentsException If the given identifier <= 0 the 406 HttpStatus.NOT_ACCEPTABLE will be
	 *                                   thrown with the explicit localized message to the end User.
	 * @throws EntityNotFoundException   If no WorkshopEntity with such an ID will be found 404 NotFound Http status
	 *                                   will
	 *                                   be thrown.
	 */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
	public void removeEntity(long id) throws IllegalArgumentException, EntityNotFoundException {
		
		verifyIdForNullZeroBelowZero(id);
		
		T foundBiId = workshopEntitiesDaoAbstract.findById(id).orElseThrow(() -> new EntityNotFoundException(
			"No " + entityClass.getSimpleName() + " for identifier=" + id + " was found to be deleted!",
			HttpStatus.NOT_FOUND,
			messageSource.getMessage("error.removeNotFoundFailure(2)",
				new Object[]{entityClass.getSimpleName(), id}, LocaleContextHolder.getLocale())));
		
		workshopEntitiesDaoAbstract.removeEntity(foundBiId);
	}
	
	/**
	 * @throws IllegalArgumentsException If a given Collection is null it will be thrown with HttpStatus.NOT_ACCEPTABLE
	 */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
	public void removeEntities(Collection<T> entities) throws IllegalArgumentsException {
		if (entities == null) {
			throw new IllegalArgumentsException("Entities collection cannot be null!",
				"httpStatus.notAcceptable.null",
				HttpStatus.NOT_ACCEPTABLE);
		} else if (entities.isEmpty()) {
			return;
		}
		entities.forEach(this::removeEntity);
	}
	
	/**
	 * @param entities
	 * @return {@link WorkshopEntitiesServiceAbstract#persistEntities(Collection)} If the given collection doesn't exceed
	 * the {@link WorkshopEntitiesDaoAbstract#getBatchSize()} a collection of persisted and managed copy of entities will be returned.
	 * Otherwise an Collections.emptyList() will be returned (not to overload the memory and JPA first-level cache) and you
	 * will have to get entities from your collection yourself.
	 */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
	public Collection<T> persistEntities(Collection<T> entities) {
		if (entities == null || entities.size() == 0) {
			throw new IllegalArgumentException("Collection<Entity> cannot be null or have a zero size!");
		}
		return workshopEntitiesDaoAbstract.persistEntities(entities).orElse(Collections.emptyList());
	}
	
	/**
	 * @param entities {@link WorkshopEntitiesServiceAbstract#persistEntities(Collection)}
	 * @return A collection of only those entities which were able to be persisted.
	 * If the given collection doesn't exceed the {@link WorkshopEntitiesDaoAbstract#getBatchSize()} a collection of
	 * persisted and managed copy of entities will be returned.
	 * Otherwise the collection of detached entities will be returned (not to overload the memory and JPA first-level cache)
	 * and you will have to get entities from your collection yourself.
	 */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
	public Collection<T> mergeEntities(Collection<T> entities) {
		if (entities == null || entities.size() == 0) {
			throw new IllegalArgumentException("Collection<Entity> cannot be null or have a zero size!");
		}
		return workshopEntitiesDaoAbstract.mergeEntities(entities).orElseThrow(() -> new EntityNotFoundException(
			"Internal services failure!", HttpStatus.INTERNAL_SERVER_ERROR, messageSource.getMessage(
			"error.unknownError", null, LocaleContextHolder.getLocale())));
	}
	
	
	/**
	 * @param pageable If doesn't contain 'Sort' with property to be ordered by and Sort.Direction,
	 *                 {@link #DEFAULT_ORDER_BY} with {@link #DEFAULT_ORDER} will be used
	 * @return A Page<WorkshopEntity> with a collection of Entities or {@link EntityNotFoundException} will be thrown if nothing found or
	 * something went wrong during the search.
	 * The Page consists of number of pages, all the included pages parameters, total elements etc. to be processed
	 * for subsequent requests for all possible Pages.
	 * @throws EntityNotFoundException      If nothing was found or 'orderBy' property isn't presented among {@link #entityClass}
	 *                                      properties.
	 * @throws InternalServerErrorException If Pageable argument is null.
	 */
	@Transactional(propagation = Propagation.SUPPORTS, isolation = Isolation.READ_COMMITTED, readOnly = true)
	public Page<T> findAllEntities(Pageable pageable) throws InternalServerErrorException, EntityNotFoundException {
		if (pageable == null) {
			throw new InternalServerErrorException("Pageable cannot by null!");
		}
		pageable = getVerifiedAndCorrectedPageable(pageable);
		
		String orderBy = pageable.getSort().iterator().next().getProperty();
		Sort.Direction order = pageable.getSort().getOrderFor(orderBy).getDirection();
		try {
			Optional<List<T>> entities = workshopEntitiesDaoAbstract.findAllEntities(
				pageable.getPageSize(), pageable.getPageNumber(), orderBy, order);
			
			return getVerifiedEntitiesPage(pageable, entities);
		} catch (PersistenceException e) {
			throw new EntityNotFoundException(e.getMessage(), HttpStatus.NOT_FOUND, messageSource.getMessage(
				"error.notFoundByProperty(2)", new Object[]{entityClass.getSimpleName(), orderBy},
				LocaleContextHolder.getLocale()), e);
		}
	}
	
	/**
	 * @param pageSize min = 1, max = {@link #MAX_PAGE_SIZE} In case of incorrect values the size
	 *                 will be set in between min and max.
	 * @param pageNum  min = 1, max = {@link #MAX_PAGE_NUM}.
	 *                 In case of incorrect values the page will be set in between min and max
	 * @param orderBy  If "default" or empty - a List will be ordered by CreationDate
	 * @param order    ENUM from Sort.Direction with "ASC" or "DESC" values
	 * @return List of Entities or throws EntityNotFoundException if either nothing was found or a PersistenceException occurred.
	 * @throws EntityNotFoundException If nothing was found or {@link #entityClass} doesn't have 'orderBy' property.
	 */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, readOnly = true)
	public List<T> findAllEntities(int pageSize, int pageNum, @Nullable String orderBy, Sort.Direction order)
		throws EntityNotFoundException {
		pageSize = pageSize <= 0 || pageSize > MAX_PAGE_SIZE ? DEFAULT_PAGE_SIZE : pageSize;
		pageNum = pageNum < 0 || pageNum > MAX_PAGE_NUM ? 0 : pageNum;
		orderBy = orderBy == null || orderBy.isEmpty() ? DEFAULT_ORDER_BY : orderBy;
		order = order.isAscending() ? Sort.Direction.ASC : Sort.Direction.DESC;
		try {
			Optional<List<T>> entities = workshopEntitiesDaoAbstract.findAllEntities(pageSize, pageNum, orderBy, order);
			log.debug("An empty={} collection of {}s will be returned", entities.isPresent(), entityClass.getSimpleName());
			return entities.orElseThrow(() -> new EntityNotFoundException(
				"No " + entityClass.getSimpleName() + "s was found!",
				HttpStatus.NOT_FOUND,
				messageSource.getMessage("httpStatus.notFound(1)", new Object[]{entityClass.getSimpleName() + "s"},
					LocaleContextHolder.getLocale())));
		} catch (PersistenceException e) {
			throw new EntityNotFoundException(e.getMessage(), HttpStatus.NOT_FOUND, messageSource.getMessage(
				"error.notFoundByProperty(2)", new Object[]{entityClass.getSimpleName(), orderBy},
				LocaleContextHolder.getLocale()), e);
		}
	}
	
	/**
	 * Refresh the state of the instance from the database, overwriting changes made to the entity, if any.
	 */
	public void refreshEntity(T entity) {
		verifyEntityForNull(entity);
		try {
			workshopEntitiesDaoAbstract.refreshEntity(entity);
		} catch (EntityNotFoundException e) {
			throw new PersistenceFailureException(e.getMessage(), "httpStatus.notFound", HttpStatus.NOT_FOUND, e);
		}
	}
	
	@Transactional(propagation = Propagation.SUPPORTS, isolation = Isolation.READ_COMMITTED)
	public boolean isExist(long id) {
		verifyIdForNullZeroBelowZero(id);
		return workshopEntitiesDaoAbstract.isExist(id);
	}
	
	
	/**
	 * 1) Checks if a given Optional.List of Entities is present. If not - throws EntityNotFoundException,
	 * with the corresponds HttpStatus and localized message for the end-users.
	 * 2) Returns the fully prepared Page containing info about amount of Pages, total entities, current page num etc.
	 * to be extracted and user by WorkshopEntitiedResourceAssemblers.
	 *
	 * @param pageable PageRequest with verified parameters to prepare Page from.
	 * @param entities Found WorkshopEntities collection from WorkshopEntitiesDao
	 * @return Page with number of pages, all the included pages parameters, total elements etc.
	 * @throws EntityNotFoundException If no Entities were found.
	 */
	protected Page<T> getVerifiedEntitiesPage(Pageable pageable, Optional<List<T>> entities) throws EntityNotFoundException {
		
		long totalEntities = workshopEntitiesDaoAbstract.countAllEntities();
		
		Page<T> entitiesPage = new PageImpl<T>(entities.orElseThrow(() ->
			new EntityNotFoundException("No " + entityClass.getSimpleName() + "s were found!",
				HttpStatus.NOT_FOUND,
				messageSource.getMessage("httpStatus.notFound(1)",
					new Object[]{entityClass.getSimpleName() + "s"},
					LocaleContextHolder.getLocale()))),
			pageable, totalEntities);
		log.debug("A Page with the collection of {}s is found", entityClass.getSimpleName());
		
		return entitiesPage;
	}
	
	/**
	 * The convenient method to obtain a fully prepared EntityNotFoundException.
	 *
	 * @param entityClassName A SimpleClassName to be inserted into the localized message.
	 * @throws EntityNotFoundException With 404 HttpStatus and fully localized message for the end users If no Entities
	 *                                 were found.
	 */
	protected EntityNotFoundException getEntityNotFoundException(String entityClassName) {
		throw new EntityNotFoundException("No " + entityClassName + " found!",
			HttpStatus.NOT_FOUND,
			messageSource.getMessage("httpStatus.notFound(1)",
				new Object[]{entityClassName},
				LocaleContextHolder.getLocale()));
	}
	
	/**
	 * Verifies Pageable to be consistent.
	 *
	 * @param pageable The Pageable to be checked and renewed if it is non-compatible with standards.
	 * @return Fully verified and renewed Pageable with corrected values.
	 * @throws InternalServerErrorException If Pageable to be verified is null;
	 */
	protected Pageable getVerifiedAndCorrectedPageable(Pageable pageable) throws InternalServerErrorException {
		if (pageable == null) {
			throw new InternalServerErrorException("Pageable cannot by null!");
		}
		int pageSize = pageable.getPageSize() <= 0 || pageable.getPageSize() > MAX_PAGE_SIZE ? DEFAULT_PAGE_SIZE
			: pageable.getPageSize();
		int pageNum = pageable.getPageNumber() < 0 ? 0 : pageable.getPageNumber();
		Sort sort = pageable.getSortOr(new Sort(Sort.Direction.DESC, DEFAULT_ORDER_BY));
		
		return PageRequest.of(pageNum, pageSize, sort);
	}
	
	protected WorkshopEntity getVerifiedWorkshopEntity(Optional<? extends WorkshopEntity> optionalEntity)
		throws InternalServerErrorException, EntityNotFoundException {
		if (optionalEntity == null) {
			throw new InternalServerErrorException("Optional Entity cannot be null!");
		}
		return optionalEntity.orElseThrow(() -> getEntityNotFoundException(entityClassSimpleName));
	}
	
	protected T getVerifiedEntity(Optional<T> optionalEntity)
		throws InternalServerErrorException, EntityNotFoundException {
		if (optionalEntity == null) {
			throw new InternalServerErrorException("Optional Entity cannot be null!");
		}
		return optionalEntity.orElseThrow(() -> getEntityNotFoundException(entityClassSimpleName));
	}
	
	/**
	 * @param idToVerify ID to be verified
	 * @throws IllegalArgumentsException With appropriate HttpStatus and fully localized error message for the end
	 *                                   users if the given parameter id is null or below zero.
	 */
	protected void verifyIdForNullBelowZero(Long idToVerify) throws IllegalArgumentsException {
		if (idToVerify == null) {
			throw new IllegalArgumentsException("Identifier cannot be null!",
				HttpStatus.NOT_ACCEPTABLE, messageSource.getMessage("httpStatus.notAcceptable.identifier(1)",
				new Object[]{"null"}, LocaleContextHolder.getLocale()));
		} else if (idToVerify < 0) {
			throw new IllegalArgumentsException("Identifier cannot be below zero!",
				HttpStatus.NOT_ACCEPTABLE, messageSource.getMessage("httpStatus.notAcceptable.identifier(1)",
				new Object[]{idToVerify}, LocaleContextHolder.getLocale()));
		}
	}
	
	/**
	 * @param idToVerify ID to be verified
	 * @throws IllegalArgumentsException With appropriate HttpStatus and fully localized error message for the end
	 *                                   users if the given parameter id is null either zero or below zero.
	 */
	protected void verifyIdForNullZeroBelowZero(Long... idToVerify) throws IllegalArgumentsException {
		if (idToVerify == null) {
			throw new IllegalArgumentsException(
				"Identifier cannot be null!",
				HttpStatus.NOT_ACCEPTABLE, messageSource.getMessage("httpStatus.notAcceptable.identifier(1)",
				new Object[]{"null"}, LocaleContextHolder.getLocale()));
		} else {
			for (Long id : idToVerify) {
				if (id <= 0) {
					throw new IllegalArgumentsException(
						"Identifier cannot be zero or below!",
						HttpStatus.NOT_ACCEPTABLE, messageSource.getMessage(
						"httpStatus.notAcceptable.identifier(1)",
						new Object[]{idToVerify}, LocaleContextHolder.getLocale()));
				}
			}
		}
	}
	
	/**
	 * @param entity Entity to be checked for nullability.
	 * @throws IllegalArgumentsException With the HttpStatus.UNPROCESSABLE_ENTITY and localized message for end users.
	 */
	protected void verifyEntityForNull(T entity) throws IllegalArgumentsException {
		if (entity == null) {
			log.error("The given " + entityClassSimpleName + " cannot be null!");
			throw new IllegalArgumentsException(
				"The given " + entityClassSimpleName + " cannot be null!",
				"httpStatus.unprocessableEntity.null",
				HttpStatus.UNPROCESSABLE_ENTITY);
		}
	}
}
