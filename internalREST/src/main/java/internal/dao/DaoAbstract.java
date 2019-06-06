package internal.dao;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.io.Serializable;
import java.util.List;

/**
 * The following parameters are obligatory to be set.
 *
 * @param <T> Entity class
 * @param <K> Key class for the Entity class
 *
 * Subclasses are throw PersistenceContext exceptions
 */
@Getter
@Setter
@Repository
public abstract class DaoAbstract<T extends Serializable, K> implements DaoInterface {
	
	@PersistenceContext
	public EntityManager entityManager;
	
	private Class<T> entityClass;
	private Class<K> keyClass;
	
	public T findOne(K key) throws EntityNotFoundException, IllegalArgumentException {
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
		TypedQuery<T> selectAll = entityManager.createQuery("SELECT t FROM "+entityClass.getSimpleName()+" t", entityClass);
		List<T> entities = selectAll.getResultList();
		return entities;
	}
}
