package workshop.applicationEvents;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import workshop.internal.entities.User;

/**
 * Special class only for particular type of {@link ApplicationEvent} which is invoked in
 * {@link workshop.internal.entities.Uuid} {@literal @PostPersist} method to force the WorkshopApplication to send an
 * email for the new {@link User} with the confirmation link.
 */
@Getter
@Slf4j
public class UserRegisteredEvent extends ApplicationEvent {
	
	private User user;
	
	/**
	 * Create a new ApplicationEvent.
	 *
	 * @param user the object on which the event initially occurred (never {@code null})
	 */
	public UserRegisteredEvent(User user) {
		super(user);
		this.user = user;
	}
}
