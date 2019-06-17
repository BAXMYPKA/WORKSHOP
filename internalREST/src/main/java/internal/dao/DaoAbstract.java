package internal.dao;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
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
	
	public List<T> findAll() {
		TypedQuery<T> selectAll = entityManager.createQuery("SELECT t FROM " + entityClass.getSimpleName() + " t", entityClass);
		List<T> entities = selectAll.getResultList();
		return entities;
	}
	
	/**
	 * @param stringToMatch The part of the text to be presented in the 'name' field of the Entity
	 * @throws PersistenceException When nothing found or in case of some DB problems
	 * @throws IllegalArgumentException if 'stringToMatch' is null or empty
	 */
	public T findByNameCoincidence(String stringToMatch) throws PersistenceException, IllegalArgumentException{
		if (stringToMatch == null || stringToMatch.isEmpty()) {
			throw new IllegalArgumentException("Name cannot be null or empty!");
		}
		TypedQuery<T> typedQuery = entityManager.createQuery(
			"SELECT e FROM "+ entityClass +" e WHERE e.getName LIKE %:name%",
			entityClass);
		typedQuery.setParameter("name", stringToMatch);
		return typedQuery.getSingleResult();
	}
}
