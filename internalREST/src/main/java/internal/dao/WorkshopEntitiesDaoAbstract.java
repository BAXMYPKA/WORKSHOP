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
import javax.persistence.criteria.*;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The following parameters are obligatory to be set in the all subclasses:
 *
 * @param <T> Entity class
 * @param <K> Key class for the Entity class
 *            <p>
 *            So every subclass must have the explicit NoArgsConstructor as:
 *            public EntityDao() {
 *            super.setWorkshopEntityClass(Entity.class);
 *            super.setKeyClass(Key.class);
 *            }
 *            <p>
 *            Subclasses are throw PersistenceContext exceptions
 */
@Getter
@Setter
@Slf4j
@Repository
public abstract class WorkshopEntitiesDaoAbstract <T extends Serializable, K> implements WorkshopEntitiesDaoInterface {
	
	@Value("${page.size.default}")
	private int PAGE_SIZE_DEFAULT;
	@Value("${page.size.max}")
	private int PAGE_SIZE_MAX;
	@Value("${page.max_num}")
	private int PAGE_NUM_MAX;
	
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
		log.debug("{} with identifier={} is found? = {}", entityClass.getSimpleName(), key, entity.isPresent());
		return entity;
	}
	
	/**
	 * As a quite common method it is placed in the superclass
	 *
	 * @param email For instance Employee.email or User.email can be found
	 * @return Optional<Entity> or Optional.empty() if nothing was found.
	 * @throws IllegalArgumentException If an email.isEmpty or null
	 */
	public Optional<T> findByEmail(String email) throws IllegalArgumentException {
		if (email == null || email.isEmpty()) {
			throw new IllegalArgumentException("Email cannot be null or empty!");
		}
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(entityClass);
		Root<T> root = cq.from(entityClass);
		
		Predicate emailPredicate = cb.equal(root.get("email"), email);
		cq.where(emailPredicate);
//		cq.select(root.get("email").get(email));
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
	
	/*
	 */
/**
 * Page formula is: (pageNum -1)*pageSize
 *
 * @param pageSize Limits the number of results given at once. Min = 1, Max = ${@link WorkshopEntitiesDaoAbstract#PAGE_SIZE_DEFAULT}
 *                 If 0 - will be set to default (max).
 * @param pageNum  Offset (page number). When pageSize=10 and pageNum=3 the result will return from 30 to 40 entities
 *                 If 0 - will be set to default. Max amount of given pages is ${@link WorkshopEntitiesDaoAbstract#PAGE_NUM_MAX}
 * @param orderBy  The name of the field the ascDesc will be happened by.
 *                 When empty, if the Entity is instance of WorkshopEntity.class the list will be ordered by
 *                 'created' field, otherwise no ordering will happened.
 * @param order    "ASC" or "DESC" types from Sort.Order ENUM
 * @return If nothing found an Optional.empty() will be returned.
 * @throws PersistenceException In case of timeout, non-transactional operation, lock-failure etc.
 *//*

	public Optional<List<T>> findAll(
		int pageSize,
		int pageNum,
		@Nullable String orderBy,
		@Nullable Sort.Direction order) throws PersistenceException {
		pageSize = (pageSize <= 0 || pageSize > PAGE_SIZE_DEFAULT) ? PAGE_SIZE_DEFAULT : pageSize;
		pageNum = pageNum <= 0 ? 1 : pageNum;
		order = order == null ? Sort.Direction.DESC : order;
		//TODO: to realize estimating the whole quantity with max pageNum
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> query = cb.createQuery(workshopEntityClass);
		Root<T> root = query.from(workshopEntityClass);
		
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
		} else if (workshopEntityClass.isInstance(WorkshopEntity.class)) {
			query.orderBy(cb.desc(root.get("created")));
		}
		
		Optional<List<T>> entities = Optional.ofNullable(select.getResultList());
		log.debug("{}s with pageSize={}, pageNum={}, orderBy={}, order={} is found? = {}",
			workshopEntityClass.getSimpleName(), pageSize, pageNum, orderBy, order, entities.isPresent());
		return entities;
	}
*/
	
	/**
	 * Page formula is: (pageNum -1)*pageSize
	 *
	 * @param pageSize The maximum amount of entities at once (on one page).
	 *                 0 = default ({@link WorkshopEntitiesDaoAbstract#PAGE_SIZE_DEFAULT}),
	 *                 Max = {@link WorkshopEntitiesDaoAbstract#PAGE_SIZE_MAX}
	 * @param pageNum  Number of desired page to be given.
	 *                 0 = default (1),
	 *                 Max = {@link WorkshopEntitiesDaoAbstract#PAGE_NUM_MAX}.
	 * @param orderBy  The name of the field (property) to order by. If null, 'created' field will be used by default.
	 * @param order    {@link Sort.Direction} Ascending or Descending order. Default is Descending.
	 * @return Optional<List < T>>. If nothing was foung, Optional.empty() will be returned.
	 * @throws IllegalArgumentException If pageSize or pageNum are greater or less than their Min and Max values.
	 * @throws PersistenceException     If an Entity doesn't have 'orderBy' field name.
	 */
	public Optional<List<T>> findAllPaged(
		int pageSize,
		int pageNum,
		@Nullable String orderBy,
		@Nullable Sort.Direction order
	) throws IllegalArgumentException, PersistenceException {
		if (pageSize < 0 || pageNum < 0) {
			throw new IllegalArgumentException("Page size or page number cannot be below zero!");
		} else if (pageSize > PAGE_SIZE_MAX || pageNum > PAGE_NUM_MAX) {
			throw new IllegalArgumentException("Your page size=" + pageSize + " or page number=" + pageNum +
				" exceeds the max page size=" + PAGE_SIZE_MAX + " or max page num=" + PAGE_NUM_MAX);
		}
		pageSize = pageSize == 0 ? PAGE_SIZE_DEFAULT : pageSize;
		pageNum = pageNum == 0 ? 1 : pageNum;
		orderBy = orderBy != null && !orderBy.isEmpty() ? orderBy : "created"; //Set presented or default
		order = order != null ? order : Sort.Direction.DESC; //Set presented of default
		
		log.debug("Paged query with pageSize={}, pageNum={}, orderBy={}, order={} will be performed",
			pageSize, pageNum, orderBy, order);
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(entityClass);
		Root<T> root = cq.from(entityClass);
		cq.select(root);
		
		if (order.isDescending()) {
			cq.orderBy(cb.desc(root.get(orderBy)));
		} else {
			cq.orderBy(cb.asc(root.get(orderBy)));
		}
		TypedQuery<T> query = entityManager.createQuery(cq);
		query.setFirstResult((pageNum - 1) * pageSize); //Offset (page number)
		query.setMaxResults(pageSize); //Limit (page size)
		
		Optional<List<T>> resultList = Optional.ofNullable(query.getResultList());
		log.debug("Was the result found? = {}", resultList.isPresent());
		return resultList;
	}
	
	/**
	 * The search by manually entered Entity.property name and its value.
	 * Also accepts ZonedDateTime, LocalDateTime and LocalDate to be parsed if Entity.property instanceof Temporal.class.
	 *
	 * @param propertyName  The name of the ${@link #getClass()} and its superclass property.
	 *                      If the propertyName class is matching any instance of ZonedDateTime, LocalDateTime or LocalDate
	 *                      so its 'propertyValue' will be parsed accordingly and may throw the parsing exception.
	 * @param propertyValue The value to be found. It it is the ZonedDateTime, LocalDate value,
	 *                      it is will be parsed accordingly.
	 * @return Optional<List<T>> with the found entities or Optional.ofNullable() if nothing was found.
	 * @throws PersistenceException     When nothing found or in case of some DB problems
	 * @throws IllegalArgumentException If 'propertyName' or 'propertyValue' is either null, or empty,
	 *                                  or neither ${@link #getClass()} or nor of its superclasses don't have such a property,
	 *                                  or parsing the 'propertyName' to the instanceof Temporal is failed.
	 */
	public Optional<List<T>> findByProperty(String propertyName, String propertyValue) throws PersistenceException, IllegalArgumentException {
		if (propertyValue == null || propertyName == null || propertyName.isEmpty() || propertyValue.isEmpty()) {
			throw new IllegalArgumentException("Name or value is null or empty!");
		}
		//Try to find such a property by name in this class and its superclasses; otherwise an IllegalArgsException will be thrown
		Field propertyFound = findPropertyOfThisEntityClass(propertyName);
		//propertyValue can be both String.class and Temporal.class to be used as argument for CriteriaQuery
		Object parsedPropertyValue = propertyValue;
		//If workshopEntityClass.property instance of Temporal.class so its value has to be the instance of the corresponding class
		if (Temporal.class.isAssignableFrom(propertyFound.getType())) {
			parsedPropertyValue = parseTemporal(propertyFound, propertyValue);
		}
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(entityClass);
		Root<T> root = cq.from(entityClass);
		Path<Object> property = root.get(propertyName);
		Predicate predicate = cb.equal(property, parsedPropertyValue);
		
		cq.select(root).where(predicate);
		
		TypedQuery<T> query = entityManager.createQuery(cq);
		List<T> resultList = query.getResultList();
		log.debug("The result by property={} with value={} is found? = {}", propertyName, parsedPropertyValue, resultList != null);
		return (resultList != null && !resultList.isEmpty()) ? Optional.of(resultList) : Optional.empty();
	}
	
	/**
	 * Only if an Entity.identifier == 0 or null it will be persisted.
	 * If an Entity argument extends Trackable and persisting is performing on behalf of an Employee,
	 * Trackable.setCreatedBy() is filling in with an Employee from current SecurityContext.
	 * If the persisting is performing on behalf of an User and the Entity argument is instance of Order,
	 * so the Order.setCreatedFor() will be filled in by Spring Auditable interface (see SecurityConfiguration).
	 * From the SecurityContext we estimate the current Authentication - is this an Employee or User.
	 * Employee will be found by email, User by email or phone (both fields can be used as the unique IDs).
	 *
	 * @param entity
	 * @return Returns a managed copy of Entity with 'identifier' set
	 * @throws EntityExistsException        Throws by the EntityManager itself if such an Entity extsts.
	 * @throws TransactionRequiredException Throws by the EntityManager itself if it is performing not in a transaction
	 * @throws IllegalArgumentException     If a given Entity is not an entity either is null or its identifier != 0.
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
		return Optional.ofNullable(entityManager.find(entityClass, ((WorkshopEntity) entity).getIdentifier()));
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
	
	/**
	 * @param entities Entities to be merged into the DataBase.
	 * @return A collection of only managed entities which were merged successfully. If the collection to be returned
	 * exceeds the {@link WorkshopEntitiesDaoAbstract#getBatchSize()} this collection will contain only detached entities.
	 * @throws IllegalArgumentException If a given collection == null or isEmpty().
	 */
	public Optional<Collection<T>> mergeEntities(Collection<T> entities) throws IllegalArgumentException {
		if (entities == null || entities.size() == 0) {
			throw new IllegalArgumentException("Entities collection cannot be null or be zero size!");
		}
		Collection<T> mergedEntities = new ArrayList<>(entities.size());
		
		entities.iterator().forEachRemaining(entityToBeMerged -> {
			Optional<T> mergedEntity = mergeEntity(entityToBeMerged);
			mergedEntity.ifPresent(mergedEntities::add);
		});
		if (mergedEntities.size() > batchSize) {
			entityManager.flush();
			entityManager.clear();
		}
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
			log.debug("Entity.identifier={} is found.", id);
			return true;
		} catch (NoResultException nre) {
			log.debug("Entity.identifier={} is not found!", id);
			return false;
		}
	}
	
	/**
	 * @return The whole amount of {@link #entityClass} available in DataBase.
	 */
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
	
	private Field findPropertyOfThisEntityClass(String fieldName) throws IllegalArgumentException {
		//Try to find such a field in this class...
		List<Field> allFields = new ArrayList<>(Arrays.asList(entityClass.getDeclaredFields()));
		// ...and in its all the superclasses
		Class superClass = entityClass.getSuperclass();
		while (superClass != null) {
			allFields.addAll(Arrays.asList(superClass.getDeclaredFields()));
			superClass = superClass.getSuperclass();
		}
		
		Optional<Field> fieldFound = allFields.stream().filter(field -> fieldName.equals(field.getName())).findFirst();
		
		log.debug("Is the property(field)={} presented in {} and its superclasses? = {}",
			fieldName, entityClass.getSimpleName(), fieldFound.isPresent());
		
		return fieldFound.orElseThrow(() -> new IllegalArgumentException(
			entityClass.getSimpleName() + " doesn't have such a '" + fieldName + "' property!"));
		
	}
	
	private Temporal parseTemporal(Field temporalClass, String temporalValue) {
		Temporal temporalParsed;
		try {
			if (ZonedDateTime.class.isAssignableFrom(temporalClass.getType())) {
				temporalParsed = ZonedDateTime.parse(temporalValue);
				log.debug("{} parsed as ZonedDateTime", temporalValue);
			} else if (LocalDate.class.isAssignableFrom(temporalClass.getType())) {
				temporalParsed = LocalDate.parse(temporalValue);
				log.debug("{} parsed as LocalDate", temporalValue);
			} else if (LocalDateTime.class.isAssignableFrom(temporalClass.getType())) {
				temporalParsed = LocalDateTime.parse(temporalValue);
				log.debug("{} parsed as LocalDateTime", temporalValue);
			} else {
				throw new DateTimeParseException(
					"The property temporal class must be the instance of either ZonedDateTime or LocalDateTime or LocalDate!",
					temporalValue, 0);
			}
		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException("Impossible to parse the given '" + temporalValue +
				"' value as ZonedDateTime, LocalDateTime or LocalDate!" +
				"The proper template must correspond: yyyy-MM-ddT00:00:00+00:00" +
				"e.g.: 2020-10-5T13:50:45:00+00.01, 2017-11-20T09:35:45+03:00[Europe/Moscow] ect", e);
		}
		return temporalParsed;
	}
}