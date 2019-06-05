package internal.dao;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.List;

/**
 * The following parameters are obligatory to be set.
 * @param <T> Entity class
 * @param <K> Key class for the Entity class
 */
@Getter
@Setter
@Repository
public abstract class DaoAbstract <T extends Serializable, K> implements DaoInterface {
	
	@PersistenceContext
	public EntityManager entityManager;
	
	private Class <T> entityClass;
	private Class <K> keyClass;
	
	public T findEntity(K key){
		return entityManager.find(entityClass, key);
	}
	
	public List<T> findEntities(){
		return null;
	}
}
