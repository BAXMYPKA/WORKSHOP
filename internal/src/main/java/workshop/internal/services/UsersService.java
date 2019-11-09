package workshop.internal.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import workshop.internal.dao.UsersDao;
import workshop.internal.entities.User;
import workshop.internal.entities.WorkshopEntity;
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
import workshop.internal.exceptions.IllegalArgumentsException;
import workshop.internal.exceptions.PersistenceFailureException;

import javax.persistence.EntityExistsException;
import javax.validation.Valid;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Service
public class UsersService extends WorkshopEntitiesServiceAbstract<User> {
	
	@Autowired
	private UsersDao usersDao;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Value("${default.languageTag}")
	private String defaultLanguageTag;
	
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
	 * Partly overridden method so that {@link User#getLanguageTag()} will be set.
	 * {@link User#getLanguageTag()} will be set if it is null. If it is wrong - {@link #defaultLanguageTag} will be set
	 *
	 * @see WorkshopEntitiesServiceAbstract#persistEntity(WorkshopEntity)
	 */
	@Override
	public User persistEntity(User entity)
		throws IllegalArgumentException, IllegalArgumentsException, EntityExistsException, PersistenceFailureException {
		if (entity.getLanguageTag() == null) {
			entity.setLanguageTag(LocaleContextHolder.getLocale().toLanguageTag());
		} else if (Locale.forLanguageTag(entity.getLanguageTag()).toLanguageTag() == null) {
			entity.setLanguageTag(defaultLanguageTag);
		}
		return super.persistEntity(entity);
	}
	
	/**
	 * @param emailOrPhone User can by logged by email or phone that's why this method will sequentially look for
	 *                     the User by one of those fields.
	 * @return Optional.ofNullable
	 */
	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	public User findByLogin(String emailOrPhone) {
		return usersDao.findByEmail(emailOrPhone).orElseThrow(() -> getEntityNotFoundException("User"));
	}
	
	/**
	 * @param pageable    Info with desired pageNum, pageSize, orderBy and order to op
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
	
	/**
	 * @param userDto {@link User} with a raw (non-encoded) password.
	 * @return Persisted {@link User} from the DataBase.
	 */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
	public User createNewUser(@Valid User userDto) {
		userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
		userDto.setLanguageTag(userDto.getLanguageTag() == null ? defaultLanguageTag : userDto.getLanguageTag());
		userDto.setIsEnabled(false);
		if (userDto.getExternalAuthorities() == null || userDto.getExternalAuthorities().isEmpty()) {
			//
		}
		return persistEntity(userDto);
	}
}
