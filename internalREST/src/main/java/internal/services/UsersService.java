package internal.services;

import internal.dao.UsersDao;
import internal.entities.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
public class UsersService extends WorkshopEntitiesServiceAbstract<User> {
	
	@Autowired
	private UsersDao usersDao;
	
	/**
	 * @param usersDao A concrete implementation of the EntitiesDaoAbstract<T,K> for the concrete
	 *                            implementation of this EntitiesServiceAbstract<T>.
	 *                            To be injected to all the superclasses.
	 *                            For instance, 'public OrdersService(OrdersDao ordersDao)'
	 */
	public UsersService(UsersDao usersDao) {
		super(usersDao);
	}
	
	/**
	 * @param emailOrPhone User can by logged by email or phone that's why this method will sequentially look for
	 *                     the User by one of those fields.
	 * @return Optional.ofNullable
	 */
	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	public Optional<User> findByLogin(String emailOrPhone) {
		Optional user = usersDao.findByEmail(emailOrPhone).isPresent() ? usersDao.findByEmail(emailOrPhone) :
			usersDao.findByPhone(emailOrPhone);
		return user;
	}
}
