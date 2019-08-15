package internal.http.security;

import internal.entities.User;
import internal.service.UsersService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Class is configured as a bean in the SecurityConfiguration.class.
 * It is intended to provide a User for @CreatedBy in Order.setCreatedFor()
 * when the User is the online creator of the Order and can be derived from the SecurityContext.
 */
@Slf4j
@Getter
@Setter
public class UserAuditorAware implements AuditorAware<User> {
	
	@Autowired
	private UsersService usersService;
	
	@Override
	public Optional<User> getCurrentAuditor() {
		String login = SecurityContextHolder.getContext().getAuthentication().getName();
		if (login == null || login.isEmpty()){
			return Optional.empty();
		} else {
			return usersService.findByLogin(login);
		}
	}
}
