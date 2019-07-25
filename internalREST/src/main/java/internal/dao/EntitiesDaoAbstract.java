package internal.dao;

import internal.entities.Employee;
import internal.entities.Trackable;
import internal.entities.WorkshopEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The following parameters are obligatory to be set in the all subclasses:
 *
 * @param <T> Entity class
 * @param <K> Key class for the Entity class
 *            <p>
 *            So every subclass must have the explicit NoArgsConstructor as:
 *            public EntityDao() {
 *            super.setEntityClass(Entity.class);
 *            super.setKeyClass(Key.class);
 *            }
 *            <p>
 *            Subclasses are throw PersistenceContext exceptions
 */
@Getter
@Setter
@Slf4j
@Repository
public abstract class EntitiesDaoAbstract<T extends Serializable, K> implements EntitiesDaoInterface {
	
	@Value("${default.page.size}")
	private int DEFAULT_PAGE_SIZE;
	@Value("${default.page.max_num}")
	private int MAX_PAGE_NUM;
	
	@PersistenceContext
	public EntityManager entityManager;
	
	private Class<T> entityClass;
	private Class<K> keyClass;
	
	/**
	 * Automatically set from application.properties 'spring.jpa.properties.hibernate.jdbc.batch_size=2000'
	 * Getter and setter are for the testing purposes.
	 */
	@Getter
	@Setter
	@Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
	int batchSize;
	
	/**
	 * @param key
	 * @return The found entity instance or Optional.empty() if the entity does not exist
	 * @throws IllegalArgumentException if key is null
	 */
	public Optional<T> findById(K key) throws IllegalArgumentException {
		if (key == null) {
			throw new IllegalArgumentException("Key parameter is null!");
		}
		Optional<T> entity = Optional.ofNullable(entityManager.find(entityClass, key));
		log.debug("{} with id={} is found? = {}", entityClass.getSimpleName(), key, entity.isPresent());
		return entity;
	}
	
	/**
	 * As a quite common method it is placed in the superclass
	 *
	 * @param email For instance Employee.email or User.email can be found
	 * @throws IllegalArgumentException If an email.isEmpty or null
	 */
	public Optional<T> findByEmail(String email) throws IllegalArgumentException {
		if (email == null || email.isEmpty()) {
			throw new IllegalArgumentException("Email cannot be null or empty!");
		}
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(entityClass);
		Root<T> root = cq.from(entityClass);
		cq.select(root.get("email").get(email));
		TypedQuery<T> typedQuery = entityManager.createQuery(cq);
		try {
			Optional<T> entity = Optional.ofNullable(typedQuery.getSingleResult());
			log.debug("{} with email={} is found? = {}", entityClass.getSimpleName(), email, entity.isPresent());
			return entity;
		} catch (NoResultException e) {
			log.debug("No {} with email={} was found. (NoResultException is omitted)", entityClass.getSimpleName(), email);
			return Optional.empty();
		} catch (PersistenceException e) {
			log.info("No {} with email={} was found with the PersistenceException", entityClass.getSimpleName(), email, e);
			return Optional.empty();
		}
	}
	
	/**
	 * Page formula is: (pageNum -1)*pageSize
	 *
	 * @param pageSize Limits the number of results given at once. Min = 1, Max = ${@link EntitiesDaoAbstract#DEFAULT_PAGE_SIZE}
	 *                 If 0 - will be set to default (max).
	 * @param pageNum  Offset (page number). When pageSize=10 and pageNum=3 the result will return from 30 to 40 entities
	 *                 If 0 - will be set to default. Max amount of given pages is ${@link EntitiesDaoAbstract#MAX_PAGE_NUM}
	 * @param orderBy  The name of the field the ascDesc will be happened by.
	 *                 When empty, if the Entity is instance of WorkshopEntity.class the list will be ordered by
	 *                 'created' field, otherwise no ordering will happened.
	 * @param order    "ASC" or "DESC" types from Sort.Order ENUM
	 * @return If nothing found an Optional.empty() will be returned.
	 * @throws PersistenceException In case of timeout, non-transactional operation, lock-failure etc.
	 */
	public Optional<List<T>> findAll(
		int pageSize,
		int pageNum,
		@Nullable String orderBy,
		@Nullable Sort.Direction order) throws PersistenceException {
		//TODO: to realize estimating the whole quantity with max pageNum
		pageSize = (pageSize <= 0 || pageSize > DEFAULT_PAGE_SIZE) ? DEFAULT_PAGE_SIZE : pageSize;
		pageNum = pageNum <= 0 ? 1 : pageNum;
		order = order == null ? Sort.Direction.DESC : order;
		
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> query = cb.createQuery(entityClass);
		Root<T> root = query.from(entityClass);
		
		TypedQuery<T> select = entityManager.createQuery(query);
		select.setFirstResult((pageNum - 1) * pageSize); //Page formula
		select.setMaxResults(pageSize);
		
		//If 'orderBy' is used we use it in conjunction with order
		if (orderBy != null && !orderBy.isEmpty()) {
			if (order.isAscending()) {
				query.orderBy(cb.asc(root.get(orderBy)));
			} else {
				query.orderBy(cb.desc(root.get(orderBy)));
			}
			//Otherwise we try to use 'created' field
		} else if (entityClass.isInstance(WorkshopEntity.class)) {
			query.orderBy(cb.desc(root.get("created")));
		}
		
		Optional<List<T>> entities = Optional.ofNullable(select.getResultList());
		log.debug("{}s with pageSize={}, pageNum={}, orderBy={}, order={} is found? = {}",
			entityClass.getSimpleName(), pageSize, pageNum, orderBy, order, entities.isPresent());
		return entities;
	}
	
	/**
	 * The search by a value of the particular property of ${@link this#getClass()} (if the Entity has such a property).
	 *
	 * @param propertyName  The name of the ${@link this#getClass()} property.
	 * @param propertyValue The value to be found.
	 * @throws PersistenceException     When nothing found or in case of some DB problems
	 * @throws IllegalArgumentException If 'propertyName' or 'propertyValue' is null or empty.
	 *                                  Or ${@link this#getClass()} doesn't have such a property!
	 */
	public Optional<T> findByProperty(String propertyName, String propertyValue) throws PersistenceException, IllegalArgumentException {
		if (propertyValue == null || propertyName == null || propertyName.isEmpty() || propertyValue.isEmpty()) {
			throw new IllegalArgumentException("Name or value is null or empty!");
		}
		try {
			Field property = entityClass.getField(propertyName);
			log.debug("{} has the {} property to find a value='{}' from",
				entityClass.getSimpleName(), property.getName(), propertyValue);
		} catch (NoSuchFieldException e) {
			throw new IllegalArgumentException(
				entityClass.getSimpleName() + " doesn't have such a " + propertyName + " property!", e);
		}
		TypedQuery<T> typedQuery = entityManager.createQuery(
			"SELECT e FROM " + entityClass + " e WHERE e.getName LIKE %:name%",
			entityClass);
		typedQuery.setParameter("name", propertyValue);
		try {
			Optional<T> entity = Optional.ofNullable(typedQuery.getSingleResult());
			log.debug("{} with property={} and propertyValue={} is found? = {}",
				entityClass.getSimpleName(), propertyName, propertyValue, entity.isPresent());
			return entity;
		} catch (NoResultException e) {
			log.info("No {} with property={} and propertyValue={} was found.",
				entityClass.getSimpleName(), propertyName, propertyValue);
			return Optional.empty();
		} catch (PersistenceException ep) {
			log.warn(ep.getMessage(), ep);
			return Optional.empty();
		}
	}
	
	/**
	 * Only if an Entity.id == 0 or null it will be persisted.
	 * If an Entity argument extends Trackable and persisting is performing on behalf of an Employee,
	 * Trackable.setCreatedBy() is filling in with an Employee from current SecurityContext.
	 * If the persisting is performing on behalf of an User and the Entity argument is instance of Order,
	 * so the Order.setCreatedFor() will be filled in by Spring Auditable interface (see SecurityConfiguration).
	 * From the SecurityContext we estimate the current Authentication - is this an Employee or User.
	 * Employee will be found by email, User by email or phone (both fields can be used as the unique IDs).
	 *
	 * @param entity
	 * @return Returns a managed copy of Entity with 'id' set
	 * @throws EntityExistsException        Throws by the EntityManager itself if such an Entity extsts.
	 * @throws TransactionRequiredException Throws by the EntityManager itself if it is performing not in a transaction
	 * @throws IllegalArgumentException     If a given Entity is not an entity either is null or its id != 0.
	 */
	public Optional<T> persistEntity(T entity)
		throws EntityExistsException, TransactionRequiredException, IllegalArgumentException, ClassCastException {
		if (entity == null) {
			throw new IllegalArgumentException("Entity cannot be null!");
		}
		//Set Trackable.createdBy(Employee employee)
		if (entity instanceof Trackable) {
			Authentication currentAuthentication = getCurrentAuthentication();
			if ("Employee".equals(currentAuthentication.getPrincipal().getClass().getSimpleName())) {
				((Trackable) entity).setCreatedBy((Employee) currentAuthentication.getPrincipal());
				log.debug("{}.createdBy set to {}", entityClass.getSimpleName(), currentAuthentication.getName());
			}
		}
		entityManager.persist(entity);
		log.info("{} has been persisted.", entity.getClass().getSimpleName());
		return Optional.ofNullable(entityManager.find(entityClass, ((WorkshopEntity) entity).getId()));
	}
	
	/**
	 * Batch insertion into the DataBase.
	 * Persists all the given Entities from Collection parameter from 0 to 'batchSize';
	 * every Entity above 'batchSize' in the given Collection have to be returned in the managed state manually.
	 * Also clears Hibernate cache (to save memory) so after the batch inserting
	 * you have to refresh those entities you need from the batch further.
	 *
	 * @param entities Collection of entities to be batch inserted. It is preferable not to exceed {@link #batchSize}
	 *                 to be able to get a Collection of persisted and managed Entities.
	 * @return If the Collection.size doesn't exceed the batchSize {@link #batchSize} a collection of copied and managed
	 * Entities will be returned.
	 * Otherwise the Optional.empty() will be returned (not to overload JPA first-level cache and RAM memory)
	 * and you will have to get managed copies of Entities by another way.
	 */
	public Optional<Collection<T>> persistEntities(Collection<T> entities) {
		if (entities == null || entities.size() == 0) {
			throw new IllegalArgumentException("Entities Collection cannot be null or zero size!");
		}
		//Context wont be cleared and this method will return a managed copy of Entities collection if entities.size <= batchSize
		Collection<T> persistedEntities = entities.size() <= batchSize ? new ArrayList<>(entities.size()) : null;
		log.debug("Will a new collection of the managed {}s be returned? = {}",
			entityClass.getSimpleName(), entities.size() <= batchSize);
		AtomicInteger counter = new AtomicInteger();
		
		entities.forEach(entity -> {
			counter.getAndIncrement();
			Optional<T> persistedEntity = persistEntity(entity);
			if (persistedEntities != null && persistedEntity.isPresent()) {
				persistedEntities.add(persistedEntity.get());
			}
			if (counter.get() % batchSize == 0) {
				entityManager.flush();
				entityManager.clear();
			}
		});
		log.info("{}s collection is persisted.", entityClass.getSimpleName());
		return persistedEntities != null ? Optional.of(persistedEntities) : Optional.empty();
	}
	
	/**
	 * Pull any database changes into the managed Entity
	 */
	public void refreshEntity(T entity) throws IllegalArgumentException {
		if (entity == null) {
			throw new IllegalArgumentException("Entity cannot be null!");
		}
		entityManager.refresh(entity);
		log.debug("{} properties have been refreshed in the DataBase.", entity.getClass().getSimpleName());
	}
	
	/**
	 * Pull any database changes into the managed Entities
	 */
	public void refreshEntities(Collection<T> entities) throws IllegalArgumentException {
		if (entities == null) {
			throw new IllegalArgumentException("Entities Collection cannot be null!");
		}
		entities.iterator().forEachRemaining(this::refreshEntity);
		log.debug("{}s all the properties have been refreshed in the DataBase.", entityClass.getSimpleName());
	}
	
	/**
	 * Flush all the changed properties of the detached Entity to the DataBase and returns a managed one.
	 *
	 * @param entity Entity to be merge with existing one
	 * @return A managed copy of the Optional<Entity> or Optional.empty() if the given entity is the removed one.
	 * @throws IllegalArgumentException If Entity is null or if instance is a removed entity
	 */
	public Optional<T> mergeEntity(T entity) throws IllegalArgumentException {
		if (entity == null) {
			throw new IllegalArgumentException("Entity cannot be null!");
		}
//		Set 'modifiedBy' field if an Entity instance of Trackable and the SecurityContext contains an Employee who is merging the changes
		if (entity instanceof Trackable && getCurrentAuthentication() != null) {
			Authentication authentication = getCurrentAuthentication();
			if ("Employee".equals(authentication.getPrincipal().getClass().getSimpleName())) {
				((Trackable) entity).setModifiedBy((Employee) authentication.getPrincipal());
				log.debug("{}.createdBy set to '{}'", entityClass.getSimpleName(), authentication.getName());
			}
		}
		Optional<T> mergedEntity;
		try {
			mergedEntity = Optional.of(entityManager.merge(entity));
		} catch (IllegalArgumentException ex) { //Could be thrown by EntityManager if it is a removed entity
			log.info(ex.getMessage(), ex);
			return Optional.empty();
		}
		log.debug("{} is merged", entity.getClass().getSimpleName());
		return mergedEntity;
	}
	
	public Optional<Collection<T>> mergeEntities(Collection<T> entities) throws IllegalArgumentException {
		if (entities == null || entities.size() == 0) {
			throw new IllegalArgumentException("Entities collection cannot be null or be zero size!");
		}
		Collection<T> mergedEntities = new ArrayList<>(entities.size());
		
		entities.iterator().forEachRemaining(entityToBeMerged -> {
			Optional<T> mergedEntity = mergeEntity(entityToBeMerged);
			mergedEntity.ifPresent(mergedEntities::add);
		});
		log.debug("Has the {}s collection been successfully merged? = {}", entityClass.getSimpleName(), mergedEntities.isEmpty());
		return Optional.of(mergedEntities);
	}
	
	/**
	 * @throws IllegalArgumentException     If a given entity is null either the instance is not an entity or is a detached
	 *                                      entity
	 * @throws TransactionRequiredException This method has to be performed within a Transaction.
	 */
	public void removeEntity(T entity) throws IllegalArgumentException, TransactionRequiredException {
		if (entity == null) {
			throw new IllegalArgumentException("Entity cannot be null!");
		}
		entityManager.remove(entity);
		log.debug("{} successfully removed", entityClass.getSimpleName());
	}
	
	public void removeEntities(Collection<T> entities) {
		if (entities == null) {
			throw new IllegalArgumentException("Entities collection cannot be null!");
		}
		entities.forEach(this::removeEntity);
		log.debug("All the {}s entities have been removed.", entityClass.getSimpleName());
	}
	
	public boolean isExist(long id) {
		if (id <= 0) {
			throw new IllegalArgumentException("ID cannot be zero or below!");
		}
		TypedQuery<Long> entityId = entityManager.createQuery(
			"SELECT id FROM " + entityClass + " id WHERE id.id= :id", Long.class);
		entityId.setParameter("id", id);
		Long idFound = null;
		try {
			idFound = entityId.getSingleResult();
			log.debug("Entity.id={} is found.", id);
			return true;
		} catch (NoResultException nre) {
			log.debug("Entity.id={} is not found!", id);
			return false;
		}
	}
	
	public long countAllEntities() {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		cq.select(cb.count(cq.from(entityClass)));
		Long count = entityManager.createQuery(cq).getSingleResult();
		log.debug("All the counted {} ={}", entityClass.getSimpleName(), count);
		return count;
	}
	
	private Authentication getCurrentAuthentication() {
		//Authentication.getPrincipal returns either Employee or User object
		Authentication authentication = null;
		try {
			authentication = SecurityContextHolder.getContext().getAuthentication();
			log.info("Authentication={} is found", authentication.getName());
		} catch (Exception e) {
			throw new AuthenticationCredentialsNotFoundException("Authentication not found in the SecurityContext!", e);
		}
		return authentication;
	}
}
