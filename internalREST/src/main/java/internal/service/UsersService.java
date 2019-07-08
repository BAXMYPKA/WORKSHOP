package internal.service;

import internal.dao.UsersDao;
import internal.entities.User;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Setter
@Getter
@Service
public class UsersService {
	
	@Autowired
	private UsersDao usersDao;
	
	/**
	 * @param emailOrPhone User can by logged by email or phone that's why this method will sequentially look for
	 *                     the User by one of those fields.
	 * @return Optional.ofNullable
	 */
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public Optional<User> findByLogin(String emailOrPhone) {
		Optional user = usersDao.findByEmail(emailOrPhone).isPresent() ? usersDao.findByEmail(emailOrPhone) :
			usersDao.findByPhone(emailOrPhone);
		return user;
	}
}
