package workshop.security;

import lombok.extern.slf4j.Slf4j;
import workshop.internal.dao.UsersDao;
import workshop.internal.entities.User;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.persistence.PersistenceException;
import java.util.Optional;

@Slf4j
@Setter
public class UsersDetailsService implements UserDetailsService {
	
	@Autowired
	private UsersDao usersDao;
	
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
	
/*
	public UserDetailsUser loadUserByEmail(String email) throws UsernameNotFoundException  {
		return new UserDetailsUser(byEmail.get());
	}
*/
}
