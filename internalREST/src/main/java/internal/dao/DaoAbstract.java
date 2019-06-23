package internal.dao;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import javax.persistence.criteria.*;
import java.io.Serializable;
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
	 * @param limit   Limits the number of results at once. Max = 50. Default = 50
	 * @param offset  Offset (page number). When limit=10 and offset=3 the result will return from 30 to 40 entities
	 * @param orderBy The name of the field the sorting will happened by
	 * @param sorting "ASC" or "DESC" type
	 * @return
	 */
	public List<T> findAll(int limit, int offset, String orderBy, String sorting) {
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
		
		if (orderBy != null && !orderBy.isEmpty()) {
			if ("asc".equalsIgnoreCase(sorting)){
				query.orderBy(cb.asc(root.get(orderBy)));
			} else if ("desc".equalsIgnoreCase(sorting)){
				query.orderBy(cb.desc(root.get(orderBy)));
			}
//			query./
		}
		
		List<T> resultList = select.getResultList();
		//
		TypedQuery<T> selectAll = entityManager.createQuery("SELECT t FROM " + entityClass.getSimpleName() + " t", entityClass);
		List<T> entities = selectAll.getResultList();
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
}
