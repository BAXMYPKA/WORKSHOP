package workshop.security;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import workshop.exceptions.EntityNotFoundException;
import workshop.exceptions.UuidAuthenticationException;
import workshop.internal.dao.UsersDao;
import workshop.internal.dao.UuidsDao;
import workshop.internal.entities.User;
import workshop.internal.entities.Uuid;
import workshop.internal.services.UsersService;

import javax.persistence.PersistenceException;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Setter
public class UsersDetailsService implements UserDetailsService {
	
	@Autowired
	private UsersDao usersDao;
	
	@Autowired
	private UuidsDao uuidsDao;
	
	@Autowired
	private UsersService usersService;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Value("${default.languageTag}")
	private String defaultLanguageTag;
	
	/**
	 * @param emailOrPhone The argument can be either User.email or {@literal User.<Set>phone}.
	 *                     Method will try to find a User entity sequentially by email and phone.
	 * @return {@link UserDetails} containing {@link User}
	 * @throws UsernameNotFoundException If a User entity wasn't found neither by email nor one of phones.
	 */
	@Override
	public UserDetailsUser loadUserByUsername(String emailOrPhone) throws UsernameNotFoundException {
		
		try {
			User userByEmail = usersDao.findByEmail(emailOrPhone)
				.orElseThrow(() -> new UsernameNotFoundException("User for email=" + emailOrPhone + " not found!"));
			
			log.debug("User={} is found by email and passing to the AuthenticationProvider to check the password",
				userByEmail.getEmail());
			
			return new UserDetailsUser(userByEmail);
			
		} catch (PersistenceException e) {
			log.debug("In this message may be presented any PersistenceException causing by as an UserNotFound " +
				"as the JPA failure");
			throw new UsernameNotFoundException(
				"Such an email=(" + emailOrPhone + ") is not found. The message from DataBase=" + e.getMessage());
		}
		
	}
	
	/**
	 * 1. Searches {@link Uuid} by the given String.
	 * <p>
	 * 2. Sets the derived {@link Uuid#getUser()} {@link User#setIsEnabled(Boolean)} to 'true'
	 * <p>
	 * 3. Sets all the default {@link User#setExternalAuthorities(Set)} for enabled Users.
	 * <p>
	 * 4. And deletes this {@link Uuid} as no longer needed.
	 *
	 * @param uuidAuthenticationToken {@link UsernamePasswordUuidAuthenticationToken} with {@link UUID} to be found
	 * @return Confirmed, enabled and ready to use {@link User} in the authenticated
	 * {@link UsernamePasswordUuidAuthenticationToken}
	 * @throws AuthenticationException If {@link User} from {@link Uuid} and from
	 *                                 {@link UsernamePasswordUuidAuthenticationToken#getPrincipal()} don't match;
	 *                                 passwords don't match, {@link Uuid} is not valid or not found etc...
	 */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
	public UsernamePasswordUuidAuthenticationToken authenticateNewUserByUuid(
		UsernamePasswordUuidAuthenticationToken uuidAuthenticationToken) throws AuthenticationException {
		
		if (uuidAuthenticationToken.getUuid() == null || uuidAuthenticationToken.getUuid().isEmpty()) {
			throw new BadCredentialsException("Neither Authentication nor Principal nor Uuid cannot be null or empty!");
		}
		log.trace("Provide authentication with UUID...");
		
		Uuid uuid = uuidsDao.findByProperty("uuid", uuidAuthenticationToken.getUuid())
			.orElseThrow(() -> new EntityNotFoundException("The given UUID cannot be found!")).get(0);
		
		log.debug("Uuid={} is found.", uuid.getUuid());
		
		matchUuidUser(uuid, uuidAuthenticationToken);
		
		User user = uuid.getUser();
		
		matchPassword((String) uuidAuthenticationToken.getCredentials(), user.getPassword());
		
		uuidsDao.removeEntity(uuid);
		
		user.setIsEnabled(true);
		user.setUuid(null);
		usersService.setDefaultExternalAuthorities(user);
		
		return new UsernamePasswordUuidAuthenticationToken(
			new UserDetailsUser(user), "", user.getExternalAuthorities(),	uuidAuthenticationToken.getUuid());
	}
	
	/**
	 * Matches the User from the given UUID and the one from the given UsernamePasswordUuidAuthToken.
	 * Those Users must be the same.
	 *
	 * @throws UuidAuthenticationException If the derived Users are not equal. If the UUID is valid,
	 *                                     {@link UuidAuthenticationException#getValidUuid()} will be 'true'.
	 */
	private void matchUuidUser(Uuid uuid, UsernamePasswordUuidAuthenticationToken uuidAuthToken)
		throws UuidAuthenticationException {
		if (!uuid.getUser().getEmail().equals(uuidAuthToken.getPrincipal())) {
			//To inform the Users that only their credentials are not valid but the UUID.
			throw new UuidAuthenticationException(
				"The User=" + uuidAuthToken.getName() + " doesn't math the UUID=" + uuid.getUuid() + " !", uuid);
		}
	}
	
	/**
	 * The raw password must match the encoded one from the User or Employee
	 */
	private void matchPassword(String rawPassword, String encodedPassword) {
		if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
			throw new BadCredentialsException("Username or Password is incorrect!");
		}
	}
	
}
