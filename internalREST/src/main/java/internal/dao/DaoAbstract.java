package internal.dao;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.List;

/**
 * @param <T> Entity class
 * @param <K> Key class for the Entity class
 */
@Getter
@Setter
@NoArgsConstructor
@Repository
public abstract class DaoAbstract <T extends Serializable, K> implements DaoInterface {
	
	private Class <T> entity;
	private Class <K> key;
	
//	public DaoAbstract(Class<T> entity, Class<K> key){
//		this.entity = entity;
//		this.key = key;
//	}

	@PersistenceContext
	public EntityManager entityManager;
	
//	public T find(K key){
//		T entity = entityManager.find();
//		return entity;
//	}
	
	public List<T> findAll(){
		return null;
	}
}
