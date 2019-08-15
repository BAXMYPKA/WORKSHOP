package internal.http.security;

import internal.dao.UsersDao;
import internal.entities.User;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

@Setter
public class UsersDetailsService implements UserDetailsService {
	
	@Autowired
	private UsersDao usersDao;
	
	/**
	 * @param emailOrPhone The argument can be either User.email or User.<Set>phone.
	 *                     Method will try to find a User entity sequentially by email and phone.
	 * @return
	 * @throws UsernameNotFoundException If a User entity wasn't found neither by email nor one of phones.
	 */
	@Override
	public UserDetails loadUserByUsername(String emailOrPhone) throws UsernameNotFoundException {
		Optional<User> byEmail = usersDao.findByEmail(emailOrPhone);
		//TODO: to complete the searching by phone
		if (!byEmail.isPresent()){
			Optional<User> byPhone = usersDao.findByEmail(emailOrPhone);
		}
		return null;
	}
}
