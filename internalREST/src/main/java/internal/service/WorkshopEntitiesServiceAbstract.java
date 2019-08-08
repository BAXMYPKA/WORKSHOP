package internal.service;

import internal.dao.WorkshopEntitiesDaoAbstract;
import internal.entities.WorkshopEntity;
import internal.exceptions.IllegalArguments;
import internal.exceptions.EntityNotFound;
import internal.exceptions.PersistenceFailure;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityExistsException;
import javax.persistence.PersistenceException;
import java.util.*;
import java.util.function.Supplier;

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
public abstract class WorkshopEntitiesServiceAbstract <T extends WorkshopEntity> {
	
	/**
	 * Default size of results on one page
	 */
	@Value("${page.size.default}")
	private int DEFAULT_PAGE_SIZE;
	/**
	 * Maximum available page number
	 */
	@Value("${page.max_num}")
	private int MAX_PAGE_NUM;
	@Autowired
	private MessageSource messageSource;
	private WorkshopEntitiesDaoAbstract<T, Long> workshopEntitiesDaoAbstract;
	private Class<T> entityClass;
	
	/**
	 * @param workshopEntitiesDaoAbstract A concrete implementation of the EntitiesDaoAbstract<T,K> for the concrete
	 *                            implementation of this EntitiesServiceAbstract<T>.
	 *                            To be injected to all the superclasses.
	 *                            For instance, 'public OrdersService(OrdersDao ordersDao)'
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
	 * @throws EntityNotFound           If nothing were found
	 */
	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	public T findById(long id) throws IllegalArgumentException, EntityNotFound {
		if (id <= 0) {
			throw new IllegalArguments("The ID to be found cannot be 0 or even lower!", HttpStatus.NOT_ACCEPTABLE,
				messageSource.getMessage("error.propertyHasToBe(2)",
					new Object[]{"ID", " > 0"}, LocaleContextHolder.getLocale()));
		}
		return workshopEntitiesDaoAbstract.findById(id).orElseThrow(() ->
			new EntityNotFound("No " + entityClass.getSimpleName() + " with identifier=" + id + " was found!",
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
			throw new EntityNotFound(
				"Couldn't neither save nor update the given " + entityClass.getSimpleName() + "! Check its properties",
				HttpStatus.GONE,
				messageSource.getMessage("error.saveOrUpdate(1)", new Object[]{entityClass.getSimpleName()},
					LocaleContextHolder.getLocale()),
				ie);
		}
		return persistedEntity.orElseThrow(() -> new PersistenceFailure(
			"Couldn't neither save nor update the given " + entityClass.getSimpleName() + "! Check its properties."));
	}
	
	public T persistEntity(T entity) throws IllegalArgumentException, EntityExistsException {
		if (entity == null) {
			throw new IllegalArgumentException("Entity cannot be null!");
		}
		return workshopEntitiesDaoAbstract.persistEntity(entity).orElseThrow(() -> new PersistenceFailure(
			"Couldn't save" + entityClass.getSimpleName() + "! Check its properties.",
			HttpStatus.CONFLICT,
			messageSource.getMessage("error.saveFailure(1)", new Object[]{entityClass.getSimpleName()},
				LocaleContextHolder.getLocale())));
	}
	
	/**
	 * @param entity Entity to be merged (updated) in the DataBase
	 * @return An updated managed copy of the entity.
	 * @throws IllegalArgumentException If the given entity = null.
	 * @throws PersistenceFailure       If the given entity is in the removed state (not found in the DataBase).
	 */
	public T mergeEntity(T entity) throws IllegalArgumentException, PersistenceFailure {
		if (entity == null) {
			throw new IllegalArgumentException("Entity cannot be null!");
		}
		return workshopEntitiesDaoAbstract.mergeEntity(entity).orElseThrow(() -> new PersistenceFailure(
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
			throw new EntityNotFound(
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
		T foundBiId = workshopEntitiesDaoAbstract.findById(id).orElseThrow(() -> new EntityNotFound(
			"No " + entityClass.getSimpleName() + " for identifier=" + id + " was found to be deleted!",
			HttpStatus.NOT_FOUND,
			messageSource.getMessage("error.removingFailure(2)",
				new Object[]{entityClass.getSimpleName(), id}, LocaleContextHolder.getLocale())));
		workshopEntitiesDaoAbstract.removeEntity(foundBiId);
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
		return workshopEntitiesDaoAbstract.mergeEntities(entities).orElseThrow(() -> new EntityNotFound(
			"Internal service failure!", HttpStatus.INTERNAL_SERVER_ERROR, messageSource.getMessage(
			"error.unknownError", null, LocaleContextHolder.getLocale())));
	}
	
	
	/**
	 * @param pageable Must contain Sort.by(Sort.Direction, orderBy) or Sort.of(Sort.Direction, "created") property!
	 * @param orderBy  The property for ordering the result list by.
	 * @return A Page<Entity> with a collection of Entities or with an Collection.empty() if nothing found or
	 * something went wrong during the search.
	 */
	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	public Page<T> findAllEntities(Pageable pageable, String orderBy) throws IllegalArgumentException {
		int pageSize = pageable.getPageSize() < 0 || pageable.getPageSize() > DEFAULT_PAGE_SIZE ? DEFAULT_PAGE_SIZE : pageable.getPageSize();
		int pageNum = pageable.getPageNumber() <= 0 || pageable.getPageNumber() > MAX_PAGE_NUM ? 1 :
			pageable.getPageNumber();
		try {
			Optional<List<T>> entities = workshopEntitiesDaoAbstract.findAllPaged(
				pageSize,
				pageNum,
				orderBy == null ? "" : orderBy,
				pageable.getSort().getOrderFor(orderBy == null || orderBy.isEmpty() ? "created" : orderBy).getDirection());
			long total = workshopEntitiesDaoAbstract.countAllEntities();
			
			Page<T> page = new PageImpl<T>(entities.orElse(Collections.<T>emptyList()), pageable, 0);
			log.debug("A Page with the collection of {}s is found? = {}", entityClass.getSimpleName(), page.isEmpty());
			return page;
		} catch (PersistenceException e) {
			log.info(e.getMessage(), e);
			return new PageImpl<T>(Collections.<T>emptyList());
		}
	}
	
	/**
	 * @param pageSize min = 1, max = {@link this#getDEFAULT_PAGE_SIZE()} In case of incorrect values the size
	 *                 will be set in between min and max.
	 * @param pageNum  min = 1, max = {@link WorkshopEntitiesServiceAbstract#getMAX_PAGE_NUM()}.
	 *                 In case of incorrect values the page will be set in between min and max
	 * @param orderBy  If "default" or empty - a List will be ordered by CreationDate
	 * @param order    ENUM from Sort.Direction with "ASC" or "DESC" values
	 * @return List of Entities or Collections.emptyList() if either nothing was found or a PersistenceException occurred.
	 */
	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	public List<T> findAllEntities(int pageSize, int pageNum, String orderBy, Sort.Direction order)
		throws IllegalArgumentException {
		pageSize = pageSize <= 0 || pageSize > DEFAULT_PAGE_SIZE ? DEFAULT_PAGE_SIZE : pageSize;
		pageNum = pageNum <= 0 || pageNum > MAX_PAGE_NUM ? 1 : pageNum;
		try {
			Optional<List<T>> entities = workshopEntitiesDaoAbstract.findAllPaged(
				pageSize,
				pageNum,
				orderBy == null ? "" : orderBy,
				order);
			log.debug("An empty={} collection of {}s will be returned", entities.isPresent(), entityClass.getSimpleName());
			return entities.orElse(Collections.<T>emptyList());
		} catch (PersistenceException e) {
			log.info(e.getMessage(), e);
			return Collections.<T>emptyList();
		}
	}
}
