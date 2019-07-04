package internal.dao;

import internal.entities.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UsersDao extends DaoAbstract<User, Long> {
	
	public UsersDao() {
		setEntityClass(User.class);
		setKeyClass(Long.class);
	}
	
	public Optional<User> findByPhone(String phone) throws IllegalArgumentException {
		if (phone == null || phone.isEmpty()){
			throw new IllegalArgumentException("User phone cannot be null or empty!");
		}
		
		return null;
	}
}
