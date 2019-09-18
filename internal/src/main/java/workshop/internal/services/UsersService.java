package workshop.internal.services;

import workshop.internal.dao.UsersDao;
import workshop.internal.entities.User;
import workshop.internal.exceptions.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UsersService extends WorkshopEntitiesServiceAbstract<User> {
	
	@Autowired
	private UsersDao usersDao;
	
	/**
	 * @param usersDao A concrete implementation of the EntitiesDaoAbstract<T,K> for the concrete
	 *                 implementation of this EntitiesServiceAbstract<T>.
	 *                 To be injected to all the superclasses.
	 *                 For instance, 'public OrdersService(OrdersDao ordersDao)'
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
	
	/**
	 * @param pageable Info with desired pageNum, pageSize, orderBy and order to op
	 * @param authorityId
	 * @return
	 * @throws EntityNotFoundException
	 */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, readOnly = true)
	public Page<User> findUsersByExternalAuthority(Pageable pageable, Long authorityId) throws EntityNotFoundException {
		super.verifyIdForNullZeroBelowZero(authorityId);
		Pageable verifiedPageable = super.getVerifiedAndCorrectedPageable(pageable);
		String orderBy = pageable.getSort().iterator().next().getProperty();
		Sort.Direction order = pageable.getSort().getOrderFor(orderBy).getDirection();
		
		List<User> usersByExternalAuthority = ((UsersDao) getWorkshopEntitiesDaoAbstract()).findUsersByExternalAuthority(
			verifiedPageable.getPageSize(),
			verifiedPageable.getPageNumber(),
			orderBy,
			order,
			authorityId)
			.orElseThrow(() -> getEntityNotFoundException(getEntityClassSimpleName()));
		long totalUsers = getWorkshopEntitiesDaoAbstract().countAllEntities();
		
		return new PageImpl<>(usersByExternalAuthority, verifiedPageable, totalUsers);
	}
}
