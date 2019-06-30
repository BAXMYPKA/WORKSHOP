package internal.dao;

import internal.entities.WorkshopEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import javax.persistence.criteria.*;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The following parameters are obligatory to be set.
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
@Repository
public abstract class DaoAbstract<T extends Serializable, K> implements DaoInterface {
	
	@PersistenceContext
	public EntityManager entityManager;
	
	private Class<T> entityClass;
	private Class<K> keyClass;
	
	/**
	 * Automatically set from application.properties but getter and setter are for the testing purposes
	 */
	@Getter
	@Setter
	@Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
	int batchSize;
	
	public Optional<T> findById(K key) throws PersistenceException, IllegalArgumentException {
		if (key == null) {
			throw new IllegalArgumentException("Key parameter is invalid!");
		}
		Optional<T> t = Optional.ofNullable(entityManager.find(entityClass, key));
//		T t = entityManager.find(entityClass, key);
//		if (t == null) {
//			throw new EntityNotFoundException("No results found for " + entityClass.getSimpleName());
//		}
		return t;
	}
	
	/**
	 * Page formula is: (pageNum -1)*pageSize
	 *
	 * @param pageSize Limits the number of results given at once. Min = 1, Max = 15000. Default = 15000
	 * @param pageNum  Offset (page number). When pageSize=10 and pageNum=3 the result will return from 30 to 40 entities
	 * @param orderBy  The name of the field the ascDesc will be happened by.
	 *                 When empty, if the Entity is instance of WorkshopEntity.class the list will be ordered by
	 *                 'created' field, otherwise no ordering will happened.
	 * @param order  "ASC" or "DESC" types from Sort.Order ENUM
	 * @return
	 */
	public Optional<List<T>> findAll(int pageSize, int pageNum, String orderBy, Sort.Direction order) {
		//TODO: to realize estimating the whole quantity with max pageNum
		pageSize = (pageSize <= 0 || pageSize > 15000) ? 15000 : pageSize;
		pageNum = pageNum <= 0 ? 1 : pageNum;
		
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
		
//		List<T> entities = select.getResultList();
		Optional<List<T>> entities = Optional.ofNullable(select.getResultList());
		
		return entities;
	}
	
	/**
	 * @param stringToMatch The part of the text to be presented in the 'name' field of the Entity
	 * @throws PersistenceException     When nothing found or in case of some DB problems
	 * @throws IllegalArgumentException if 'stringToMatch' is null or empty
	 */
	public T findByNameCoincidence(String stringToMatch) throws PersistenceException, IllegalArgumentException {
		if (stringToMatch == null || stringToMatch.isEmpty()) {
			throw new IllegalArgumentException("Name cannot be null or empty!");
		}
		TypedQuery<T> typedQuery = entityManager.createQuery(
			"SELECT e FROM " + entityClass + " e WHERE e.getName LIKE %:name%",
			entityClass);
		typedQuery.setParameter("name", stringToMatch);
		return typedQuery.getSingleResult();
	}
	
	/**
	 * @param entity
	 * @return Returns a managed copy of Entity with 'id' set
	 * @throws PersistenceException
	 * @throws IllegalArgumentException
	 */
	public T persistEntity(T entity) throws PersistenceException, IllegalArgumentException {
		if (entity == null) {
			throw new IllegalArgumentException("Entity cannot be null!");
		}
		entityManager.persist(entity);
		return entity;
	}
	
	/**
	 * Batch insertion into the DataBase.
	 * Persists all the given Entities from Collection parameter from 0 to 'batchSize';
	 * every Entity above 'batchSize' in the given Collection have to be returned in the managed state manually.
	 * Also clears Hibernate cache (to save memory) so after the batch inserting
	 * you have to refresh those entities you need from the batch further.
	 *
	 * @param entities Collection of entities to be batch inserted
	 */
	public void persistEntities(Collection<T> entities) {
		if (entities == null) {
			throw new IllegalArgumentException("Entities Collection cannot be null!");
		}
//		Iterator<T> iterator = entities.iterator();
		AtomicInteger counter = new AtomicInteger();
		
//		while (iterator.hasNext()) {
//			persistEntity(iterator.next());
//			counter.getAndIncrement();
//			if (counter.get() % batchSize == 0) {
//				entityManager.flush();
//				entityManager.clear();
//			}
//		}
		
		entities.stream().forEachOrdered(entity -> {
			counter.getAndIncrement();
			entity = persistEntity(entity);
			if (counter.get() % batchSize == 0){
				entityManager.flush();
				entityManager.clear();
			}
		});
	}
	
	/**
	 * Pull any database changes into the managed Entity
	 */
	public void refreshEntity(T entity) throws IllegalArgumentException {
		if (entity == null) {
			throw new IllegalArgumentException("Entity cannot be null!");
		}
		entityManager.refresh(entity);
	}
	
	/**
	 * Pull any database changes into the managed Entities
	 */
	public void refreshEntities(Collection<T> entities) throws IllegalArgumentException {
		if (entities == null) {
			throw new IllegalArgumentException("Entities Collection cannot be null!");
		}
		entities.iterator().forEachRemaining(this::refreshEntity);
	}
	
	/**
	 * Flush all the changed properties of the detached Entity to the DataBase and returns a managed one.
	 *
	 * @param entity Entity to be merge with existing one
	 * @return A managed copy of the Entity
	 */
	public T mergeEntity(T entity) throws IllegalArgumentException {
		if (entity == null) {
			throw new IllegalArgumentException("Entity cannot be null!");
		}
		return entityManager.merge(entity);
	}
	
	public Collection<T> mergeEntities(Collection<T> entities) throws IllegalArgumentException {
		if (entities == null) {
			throw new IllegalArgumentException("Entities collection cannot be null!");
		}
		entities.iterator().forEachRemaining(this::mergeEntity);
		return entities;
	}
	
	public void removeEntity(T entity) throws IllegalArgumentException, TransactionRequiredException {
		if (entity == null) {
			throw new IllegalArgumentException("Entity cannot be null!");
		}
		entityManager.remove(entity);
	}
	
	public void removeEntities(Collection<T> entities) {
		if (entities == null) {
			throw new IllegalArgumentException("Entities collection cannot be null!");
		}
		entities.forEach(this::removeEntity);
	}
	
	public long countAllEntities() {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		cq.select(cb.count(cq.from(entityClass)));
		Long count = entityManager.createQuery(cq).getSingleResult();
		return count;
	}
}
