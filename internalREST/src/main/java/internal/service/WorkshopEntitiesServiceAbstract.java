package internal.service;

import internal.dao.WorkshopEntitiesDaoAbstract;
import internal.entities.WorkshopEntity;
import internal.exceptions.EntityNotFoundException;
import internal.exceptions.IllegalArgumentsException;
import internal.exceptions.InternalServerErrorException;
import internal.exceptions.PersistenceFailureException;
import lombok.Getter;
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
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityExistsException;
import javax.persistence.PersistenceException;
import java.util.*;

/**
 * This class also intended to throw WorkshopException with fully localized messages with appropriate HttpStatus codes
 * to be shown to the end Users.
 *
 * @param <T> The class type of the Entity its instance will be serving to.
 */
@Getter
@Setter
@Slf4j
@Transactional(propagation = Propagation.REQUIRED)
@Repository
public abstract class WorkshopEntitiesServiceAbstract<T extends WorkshopEntity> {
	
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
	private MessageSource messageSource;
	private WorkshopEntitiesDaoAbstract<T, Long> workshopEntitiesDaoAbstract;
	private Class<T> entityClass;
	
	/**
	 * @param workshopEntitiesDaoAbstract A concrete implementation of the EntitiesDaoAbstract<T,K> for the concrete
	 *                                    implementation of this EntitiesServiceAbstract<T>.
	 *                                    To be injected to all the superclasses.
	 *                                    For instance, 'public OrdersService(OrdersDao ordersDao)'
	 */
	public WorkshopEntitiesServiceAbstract(WorkshopEntitiesDaoAbstract<T, Long> workshopEntitiesDaoAbstract) {
		this.workshopEntitiesDaoAbstract = workshopEntitiesDaoAbstract;
		setEntityClass(workshopEntitiesDaoAbstract.getEntityClass());
		log.trace("{} initialized successfully", entityClass.getSimpleName());
	}
	
	/**
	 * @param id
	 * @return A found Entity or throws javax.persistence.NoResultException
	 * @throws IllegalArgumentException If identifier <= 0 or not the key type for the Entity
	 * @throws EntityNotFoundException  If nothing were found
	 */
	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	public T findById(long id) throws IllegalArgumentException, EntityNotFoundException {
		if (id <= 0) {
			throw new IllegalArgumentsException("The ID to be found cannot be 0 or even lower!", HttpStatus.NOT_ACCEPTABLE,
				messageSource.getMessage("error.propertyHasToBe(2)",
					new Object[]{"ID", " > 0"}, LocaleContextHolder.getLocale()));
		}
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
	 * @throws IllegalArgumentException                   If an Entity == null
	 * @throws AuthenticationCredentialsNotFoundException If this method is trying to be performed without an appropriate
	 *                                                    Authentication within the Spring's SecurityContext.
	 */
	public T persistOrMergeEntity(T entity)
		throws IllegalArgumentException, AuthenticationCredentialsNotFoundException {
		if (entity == null) {
			throw new IllegalArgumentException("The entity argument cannot by null!");
		}
		Optional<T> persistedEntity;
		try {
			persistedEntity = workshopEntitiesDaoAbstract.persistEntity(entity);
		} catch (EntityExistsException ex) {
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
	public T persistEntity(T entity)
		throws IllegalArgumentException, IllegalArgumentsException, EntityExistsException, PersistenceFailureException {
		if (entity == null) {
			throw new IllegalArgumentsException("Entity cannot be null!", "httpStatus.notAcceptable.null",
				HttpStatus.UNPROCESSABLE_ENTITY);
		} else if (entity.getIdentifier() != null && entity.getIdentifier() > 0){
			throw new PersistenceFailureException("Id (identifier) must by null or zero!",
				HttpStatus.UNPROCESSABLE_ENTITY, messageSource.getMessage(
				"error.propertyHasToBe(2)", new Object[]{"id", "empty (null) or 0"}, LocaleContextHolder.getLocale()));
		}
		return workshopEntitiesDaoAbstract.persistEntity(entity).orElseThrow(() -> new PersistenceFailureException(
			"Couldn't save" + entityClass.getSimpleName() + "! Check its properties.",
			HttpStatus.CONFLICT,
			messageSource.getMessage("error.saveFailure(1)", new Object[]{entityClass.getSimpleName()},
				LocaleContextHolder.getLocale())));
	}
	
	/**
	 * @param entity Entity to be merged (updated) in the DataBase
	 * @return An updated managed copy of the entity.
	 * @throws IllegalArgumentException    If the given entity = null.
	 * @throws PersistenceFailureException If the given entity is in the removed state (not found in the DataBase).
	 */
	public T mergeEntity(T entity) throws IllegalArgumentException, PersistenceFailureException {
		if (entity == null) {
			throw new IllegalArgumentException("Entity cannot be null!");
		}
		return workshopEntitiesDaoAbstract.mergeEntity(entity).orElseThrow(() -> new PersistenceFailureException(
			"Updating the " + entityClass.getSimpleName() + " is failed! Such an object wasn't found to be updated!",
			HttpStatus.GONE));
	}
	
	public void removeEntity(T entity) {
		if (entity == null) {
			throw new IllegalArgumentException("Entity cannot be null!");
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
					"error.removingFailure(1)",
					new Object[]{entityClass.getSimpleName()},
					LocaleContextHolder.getLocale()),
				ex);
		}
	}
	
	/**
	 * @param id Accepts only above zero values.
	 * @throws IllegalArgumentException If the given identifier <= 0.
	 */
	public void removeEntity(long id) throws IllegalArgumentException {
		if (id <= 0) {
			throw new IllegalArgumentException("Id cannot be equal zero or below!");
		}
		T foundBiId = workshopEntitiesDaoAbstract.findById(id).orElseThrow(() -> new EntityNotFoundException(
			"No " + entityClass.getSimpleName() + " for identifier=" + id + " was found to be deleted!",
			HttpStatus.NOT_FOUND,
			messageSource.getMessage("error.removingFailure(2)",
				new Object[]{entityClass.getSimpleName(), id}, LocaleContextHolder.getLocale())));
		workshopEntitiesDaoAbstract.removeEntity(foundBiId);
	}
	
	/**
	 * @throws IllegalArgumentsException If a given Collection is null it will be thrown with HttpStatus.NOT_ACCEPTABLE
	 */
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
	public Collection<T> mergeEntities(Collection<T> entities) {
		if (entities == null || entities.size() == 0) {
			throw new IllegalArgumentException("Collection<Entity> cannot be null or have a zero size!");
		}
		return workshopEntitiesDaoAbstract.mergeEntities(entities).orElseThrow(() -> new EntityNotFoundException(
			"Internal service failure!", HttpStatus.INTERNAL_SERVER_ERROR, messageSource.getMessage(
			"error.unknownError", null, LocaleContextHolder.getLocale())));
	}
	
	
	/**
	 * @param pageable Should contain 'Sort.by(Sort.Direction, orderBy)'.
	 *                 Otherwise {@link #DEFAULT_ORDER_BY} with {@link #DEFAULT_ORDER} will be used
	 * @param orderBy  Nullable. The property for ordering the result list by. If null {@link #DEFAULT_ORDER_BY} will
	 *                 be used.
	 * @return A Page<WorkshopEntity> with a collection of Entities or {@link EntityNotFoundException} will be thrown if nothing found or
	 * something went wrong during the search.
	 * @throws EntityNotFoundException      If nothing was found or 'orderBy' property isn't presented among {@link #entityClass}
	 *                                      properties.
	 * @throws InternalServerErrorException If Pageable argument is null.
	 */
	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	public Page<T> findAllEntities(Pageable pageable, @Nullable String orderBy) throws InternalServerErrorException, EntityNotFoundException {
		if (pageable == null) {
			throw new InternalServerErrorException("Pageable cannot by null!");
		}
		int pageSize = pageable.getPageSize() <= 0 || pageable.getPageSize() > MAX_PAGE_SIZE ? DEFAULT_PAGE_SIZE
			: pageable.getPageSize();
		int pageNum = pageable.getPageNumber() < 0 ? 0 : pageable.getPageNumber();
		orderBy = orderBy == null || orderBy.isEmpty() ? DEFAULT_ORDER_BY : orderBy;
		Sort.Direction order =
			pageable.getSort().isUnsorted() || pageable.getSort().getOrderFor(orderBy).getDirection() == null ?
				Sort.Direction.DESC : pageable.getSort().getOrderFor(orderBy).getDirection();
		long totalEntities = workshopEntitiesDaoAbstract.countAllEntities();
		try {
			Optional<List<T>> entities = workshopEntitiesDaoAbstract.findAllPagedAndSorted(pageSize, pageNum, orderBy, order);
			
			Page<T> page = new PageImpl<T>(entities.orElseThrow(() ->
				new EntityNotFoundException("No " + entityClass.getSimpleName() + "s were found!",
					HttpStatus.NOT_FOUND,
					messageSource.getMessage("message.notFound(1)",
						new Object[]{entityClass.getSimpleName() + "s"},
						LocaleContextHolder.getLocale()))),
				pageable, totalEntities);
			
			log.debug("A Page with the collection of {}s is found", entityClass.getSimpleName());
			return page;
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
	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	public List<T> findAllEntities(int pageSize, int pageNum, @Nullable String orderBy, Sort.Direction order)
		throws EntityNotFoundException {
		pageSize = pageSize <= 0 || pageSize > MAX_PAGE_SIZE ? DEFAULT_PAGE_SIZE : pageSize;
		pageNum = pageNum < 0 || pageNum > MAX_PAGE_NUM ? 0 : pageNum;
		orderBy = orderBy == null || orderBy.isEmpty() ? DEFAULT_ORDER_BY : orderBy;
		order = order.isAscending() ? Sort.Direction.ASC : Sort.Direction.DESC;
		try {
			Optional<List<T>> entities = workshopEntitiesDaoAbstract.findAllPagedAndSorted(pageSize, pageNum, orderBy, order);
			log.debug("An empty={} collection of {}s will be returned", entities.isPresent(), entityClass.getSimpleName());
			return entities.orElseThrow(() -> new EntityNotFoundException(
				"No " + entityClass.getSimpleName() + "s was found!",
				HttpStatus.NOT_FOUND,
				messageSource.getMessage("message.notFound(1)", new Object[]{entityClass.getSimpleName() + "s"},
					LocaleContextHolder.getLocale())));
		} catch (PersistenceException e) {
			throw new EntityNotFoundException(e.getMessage(), HttpStatus.NOT_FOUND, messageSource.getMessage(
				"error.notFoundByProperty(2)", new Object[]{entityClass.getSimpleName(), orderBy},
				LocaleContextHolder.getLocale()), e);
		}
	}
}
