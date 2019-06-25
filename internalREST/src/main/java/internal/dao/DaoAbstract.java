package internal.dao;

import internal.entities.WorkshopEntity;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.boot.model.naming.IllegalIdentifierException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import javax.persistence.criteria.*;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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
	
	public T findById(K key) throws PersistenceException, IllegalArgumentException {
		if (key == null) {
			throw new IllegalArgumentException("Key parameter is invalid!");
		}
		T t = entityManager.find(entityClass, key);
		if (t == null) {
			throw new EntityNotFoundException("No results found for " + entityClass.getSimpleName());
		}
		return t;
	}
	
	/**
	 * @param limit   Limits the number of results given at once. Max = 50. Default = 50
	 * @param offset  Offset (page number). When limit=10 and offset=3 the result will return from 30 to 40 entities
	 * @param orderBy The name of the field the ascDesc will be happened by.
	 *                When empty, if the Entity is instance of WorkshopEntity.class the list will be ordered by
	 *                'created' field, otherwise no ordering will happened.
	 * @param ascDesc "ASC" or "DESC" type
	 * @return
	 */
	public List<T> findAll(int limit, int offset, String orderBy, String ascDesc) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> query = cb.createQuery(entityClass);
		Root<T> root = query.from(entityClass);
		
		TypedQuery<T> select = entityManager.createQuery(query);
		
		if (limit > 0 && limit <= 50) { //If the 'limit' set
			select.setMaxResults(limit);
		} else {
			select.setMaxResults(50);
		}
		
		if (offset > 0) {
			select.setFirstResult(limit * offset);
		}
		//If 'orderBy' is used we use it in conjunction with ascDesc
		if (orderBy != null && !orderBy.isEmpty()) {
			if ("asc".equalsIgnoreCase(ascDesc)) {
				query.orderBy(cb.asc(root.get(orderBy)));
			} else {
				query.orderBy(cb.desc(root.get(orderBy)));
			}
			//Otherwise we try to use 'created' field
		} else if (entityClass.isInstance(WorkshopEntity.class)) {
			query.orderBy(cb.desc(root.get("created")));
		}
		
		List<T> entities = select.getResultList();

/*
		List<T> resultList = select.getResultList();
		TypedQuery<T> selectAll = entityManager.createQuery("SELECT t FROM " + entityClass.getSimpleName() + " t", entityClass);
		List<T> entities = selectAll.getResultList();
*/
		
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
	 * Batch insertion into the DataBase
	 * @param entities Collection of entities to be batch inserted
	 */
	public void persistEntities(Collection<T> entities){
		if (entities == null){
			throw new IllegalArgumentException("Entities Collection cannot be null!");
		}
		Iterator<T> iterator = entities.iterator();
		int counter = 0;
		while (iterator.hasNext()){
			persistEntity(iterator.next());
			counter++;
			if (counter % batchSize == 0){
				entityManager.flush();
				entityManager.clear();
			}
		}
	}
	/**
	 * Flush all the changed properties of the detached Entity to the DataBase and returns a managed one.
	 *
	 * @param entity Entity to be merge with existing one
	 * @return A managed copy of the Entity
	 */
	public T mergeEntity(T entity) {
		if (entity == null) {
			throw new IllegalArgumentException("Entity cannot be null!");
		}
		return entityManager.merge(entity);
		
	}
}
