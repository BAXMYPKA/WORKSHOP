package workshop.internal.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import workshop.internal.dao.ExternalAuthoritiesDao;
import workshop.internal.dao.UsersDao;
import workshop.internal.entities.ExternalAuthority;
import workshop.internal.entities.User;
import workshop.internal.entities.Uuid;
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
import workshop.internal.exceptions.InternalServerErrorException;
import workshop.internal.exceptions.PersistenceFailureException;

import javax.persistence.EntityExistsException;
import javax.validation.Valid;
import java.util.*;

@Slf4j
@Service
public class UsersService extends WorkshopEntitiesServiceAbstract<User> {
	
	@Autowired
	private UsersDao usersDao;
	
	@Autowired
	private ExternalAuthoritiesDao externalAuthoritiesDao;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Value("${default.languageTag}")
	private String defaultLanguageTag;
	
	private Set<ExternalAuthority> unconfirmedUsersDefaultExternalAuthorities;
	
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
	 * Creates an absolutely new User with only "READ-PROFILE" {@link ExternalAuthority}, {@link User#getIsEnabled()}
	 * = false.
	 * Also creates a new {@link workshop.internal.entities.Uuid} for that User.
	 * @param userDto {@link User} with a raw (non-encoded) password.
	 * @return Persisted {@link User} from the DataBase.
	 */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
	public User createNewUser(@Valid User userDto) {
		if (userDto.getPassword() == null || userDto.getPassword().isEmpty()) {
			throw new IllegalArgumentsException("Password cannot be null or empty!", getMessageSource().getMessage(
				"httpStatus.notAcceptable.nullEmpty(1)",
					new Object[]{"password"},
					LocaleContextHolder.getLocale()),
				HttpStatus.NOT_ACCEPTABLE);
		}
		userDto.setPassword(bCryptPasswordEncoder.encode(userDto.getPassword()));
		userDto.setLanguageTag(userDto.getLanguageTag() == null ? defaultLanguageTag : userDto.getLanguageTag());
		userDto.setIsEnabled(false);
		
		if (userDto.getExternalAuthorities() == null || userDto.getExternalAuthorities().isEmpty()) {
			ExternalAuthority readProfileAuthority =
				externalAuthoritiesDao.findByProperty("name", "READ-PROFILE")
					.orElseThrow(() -> new InternalServerErrorException(
						"The default ExternalAuthority 'READ-PROFILE' doesn't exist!",
						"httpStatus.internalServerError.common",
						HttpStatus.INTERNAL_SERVER_ERROR))
					.get(0);
			userDto.setExternalAuthorities(Collections.singleton(readProfileAuthority));
		}
		Uuid uuid = new Uuid(userDto);
		return persistEntity(userDto);
	}
}
