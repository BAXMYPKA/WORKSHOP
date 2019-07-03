package internal.dao;

import internal.entities.User;
import org.springframework.stereotype.Repository;

@Repository
public class UsersDao extends DaoAbstract<User, Long> {
	
	public UsersDao() {
		setEntityClass(User.class);
		setKeyClass(Long.class);
	}
}
