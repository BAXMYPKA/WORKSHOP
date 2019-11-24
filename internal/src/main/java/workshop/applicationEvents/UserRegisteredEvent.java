package workshop.applicationEvents;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import workshop.internal.entities.User;
import workshop.internal.entities.Uuid;

/**
 * Special class only for particular type of {@link ApplicationEvent} which is invoked in
 * {@link workshop.internal.entities.Uuid} {@literal @PostPersist} method to force the WorkshopApplication to send an
 * email for the new {@link User} with the confirmation link.
 */
@Getter
@Slf4j
public class UserRegisteredEvent extends ApplicationEvent {
	
	private Uuid uuid;
	
	/**
	 * Create a new ApplicationEvent.
	 *
	 * @param uuid the object on which the event initially occurred (never {@code null})
	 */
	public UserRegisteredEvent(Uuid uuid) {
		super(uuid);
		this.uuid = uuid;
	}
}
