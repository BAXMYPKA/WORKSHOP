package workshop.internal.entities;

import lombok.*;
import org.springframework.http.HttpStatus;
import workshop.applicationEvents.WorkshopEntitiesEventPublisher;
import workshop.internal.exceptions.IllegalArgumentsException;

import javax.annotation.PostConstruct;
import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.UUID;

/**
 * Can be used as the temporary UUID-to-any-WorkshopEntity holder for the newly-created Entities which require the
 * following confirmation for being permanently persisted.
 * <p>
 * E.G., new {@link User}s after their registration are demanded to confirm it by sending this {@link #uuid} to their
 * emails. If this uuid is not confirmed during 24 hours, the corresponding {@link Uuid} and its {@link #user} will be
 * removed from the DataBase.
 * <p>
 * The new instance generates an {@link UUID} for this {@link #uuid} automatically.
 * <p>
 * After being persisted generated an {@link org.springframework.context.ApplicationEvent} for the corresponding
 * {@link #user} or any other possible properties.
 */
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@Getter
@Setter
@Entity
@Table(name = "Uuids", schema = "INTERNAL")
public class Uuid extends WorkshopAudibleEntityAbstract {
	
	@Transient
	private static final long serialVersionUID = WorkshopEntity.serialVersionUID;
	
	@NotEmpty(message = "{validation.notBlank}")
	@EqualsAndHashCode.Include
	@ToString.Include
	@Column(nullable = false, unique = true, updatable = true)
	private String uuid;
	
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "user_id", unique = true, updatable = false, referencedColumnName = "id")
	@Valid
	private User user;
	
	/**
	 * 1. {@link #uuid} property generates automatically.
	 * <p>
	 * 2. this {@link Uuid} automatically sets to the {@link User#setUuid(Uuid)}
	 *
	 * @param newUser A newly created {@link User} that demands a confirmation by generating an {@link UUID} for email
	 *                confirmation.
	 */
	public Uuid(@Valid User newUser) {
		this.user = newUser;
		generateUuid();
		newUser.setUuid(this);
	}
	
	@PostConstruct
	void generateUuid() {
		if (uuid == null || uuid.isEmpty()) {
			uuid = UUID.randomUUID().toString();
		}
	}
	
	@PostPersist
	void publishWorkshopEvent() {
		if (user != null) {
			WorkshopEntitiesEventPublisher.publishUserRegisteredEvent(this);
		}
	}
	
	/**
	 * This instance of {@link Uuid} will be automatically inserted into the given {@link User#setUuid(Uuid)}
	 *
	 * @param user A newly-created {@link User}
	 */
	public void setUser(User user) {
		if (user == null) {
			this.user = user;
			return;
		}
		if (user.getIsEnabled()) {
			throw new IllegalArgumentsException("UUID cannot be associated with enabled User!",
				"httpStatus.notAcceptable.uuidForEnabledUser",
				HttpStatus.NOT_ACCEPTABLE);
		}
		user.setUuid(this);
		this.user = user;
	}
}
