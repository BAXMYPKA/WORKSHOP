package workshop.applicationEvents;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import workshop.internal.entities.User;
import workshop.internal.entities.Uuid;

/**
 * Events for {@link User}s who requested the password reset by getting the {@link Uuid} by email.
 * This event initializes the process of creating and sending an email to that User.
 */
public class UserPasswordResetEvent extends ApplicationEvent {
	
	@Getter
	private Uuid uuid;
	
	/**
	 * Create a new ApplicationEvent.
	 *
	 * @param uuid the object on which the event initially occurred (never {@code null})
	 */
	public UserPasswordResetEvent(Uuid uuid) {
		super(uuid);
		this.uuid = uuid;
	}
}
