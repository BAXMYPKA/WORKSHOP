package internal.dao;

import internal.entities.Task;
import internal.entities.User;
import internal.exceptions.InternalServerErrorException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;

@Repository
public class UsersDao extends WorkshopEntitiesDaoAbstract<User, Long> {
	
	public UsersDao() {
		setEntityClass(User.class);
		setKeyClass(Long.class);
	}
	
	public Optional<User> findByPhone(String phone) throws IllegalArgumentException {
		if (phone == null || phone.isEmpty()){
			throw new IllegalArgumentException("User phone cannot be null or empty!");
		}
		//TODO: to complete with Dao method find by property
		return null;
	}
}
