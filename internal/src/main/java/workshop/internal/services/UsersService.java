package workshop.internal.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import workshop.internal.dao.ExternalAuthoritiesDao;
import workshop.internal.dao.UsersDao;
import workshop.internal.dao.UuidsDao;
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
	private UuidsDao uuidsDao;
	
	@Autowired
	private ExternalAuthoritiesDao externalAuthoritiesDao;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
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
	 * Creates an absolutely new User with only "READ-PROFILE" {@link ExternalAuthority}, {@link User#getIsEnabled()}
	 * = false.
	 * Also creates a new {@link workshop.internal.entities.Uuid} for that User.
	 *
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
		setNewUserExternalAuthorities(userDto);
		Uuid uuid = new Uuid(userDto);
		return persistEntity(userDto);
	}
	
	/**
	 * 1. Search {@link Uuid} by the given String.
	 * <p>
	 * 2. Set the derived {@link User#setIsEnabled(Boolean)} to 'true'
	 * <p>
	 * 3. Set all the default {@link User#setExternalAuthorities(Set)} for enabled Users.
	 * <p>
	 * 4. Deleted the found {@link Uuid} linked with the enabled {@link User#getUuid()}
	 *
	 * @param uuid String with {@link UUID} to be found in the DataBase
	 * @return Confirmed
	 */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
	public User confirmNewUserByUuid(String uuid) {
		Uuid uuidEntity = uuidsDao.findByProperty("uuid", uuid)
			.orElseThrow(() -> getEntityNotFoundException("Uuid")).get(0);
		
		User confirmedUser = uuidEntity.getUser();
		
		uuidsDao.removeEntity(uuidEntity);
		
		confirmedUser.setIsEnabled(true);
		confirmedUser.setUuid(null);
		setDefaultExternalAuthorities(confirmedUser);
		return confirmedUser;
	}
	
	private void setNewUserExternalAuthorities(User newUser) {
		ExternalAuthority readProfileAuthority =
			externalAuthoritiesDao.findByProperty("name", "READ-PROFILE")
				.orElseThrow(() -> new InternalServerErrorException(
					"The default ExternalAuthority 'READ-PROFILE' doesn't exist!",
					"httpStatus.internalServerError.common",
					HttpStatus.INTERNAL_SERVER_ERROR))
				.get(0);
		newUser.setExternalAuthorities(Collections.singleton(readProfileAuthority));
		
	}
	
	private void setDefaultExternalAuthorities(User user) {
		ExternalAuthority readProfileAuthority =
			externalAuthoritiesDao.findByProperty("name", "READ-PROFILE")
				.orElseThrow(() -> new InternalServerErrorException(
					"The default ExternalAuthority 'READ-PROFILE' doesn't exist!",
					"httpStatus.internalServerError.common",
					HttpStatus.INTERNAL_SERVER_ERROR))
				.get(0);
		ExternalAuthority writeProfileAuthority =
			externalAuthoritiesDao.findByProperty("name", "WRITE-PROFILE")
				.orElseThrow(() -> new InternalServerErrorException(
					"The default ExternalAuthority 'WRITE-PROFILE' doesn't exist!",
					"httpStatus.internalServerError.common",
					HttpStatus.INTERNAL_SERVER_ERROR))
				.get(0);
		ExternalAuthority readOrderAuthority =
			externalAuthoritiesDao.findByProperty("name", "READ-ORDER")
				.orElseThrow(() -> new InternalServerErrorException(
					"The default ExternalAuthority 'READ-ORDER' doesn't exist!",
					"httpStatus.internalServerError.common",
					HttpStatus.INTERNAL_SERVER_ERROR))
				.get(0);
		ExternalAuthority writeOrderAuthority =
			externalAuthoritiesDao.findByProperty("name", "WRITE-ORDER")
				.orElseThrow(() -> new InternalServerErrorException(
					"The default ExternalAuthority 'WRITE-ORDER' doesn't exist!",
					"httpStatus.internalServerError.common",
					HttpStatus.INTERNAL_SERVER_ERROR))
				.get(0);
		Set<ExternalAuthority> authorities = new HashSet<>(Arrays.asList(
			readProfileAuthority, writeProfileAuthority, readOrderAuthority, writeOrderAuthority));
		if (user.getExternalAuthorities() == null) {
			user.setExternalAuthorities(authorities);
		} else {
			user.getExternalAuthorities().addAll(authorities);
		}
	}
}
