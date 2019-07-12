package internal.entities;

import com.fasterxml.jackson.annotation.*;
import internal.entities.hibernateValidation.PersistenceCheck;
import internal.entities.hibernateValidation.UpdationCheck;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;

/**
 * Users of 2 types can be persisted from online and created by manager.
 * All Users must provide either their email or phone as a login and the way for the further communications.
 * Other fields are optional.
 * Online Users must create a password during registration.
 * Offline created Users will have a possibility to register themselves by their previously provided email or phone
 * as a login.
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
@ToString(of = {"id", "email", "firstName"})
//@JsonIgnoreProperties(value = {"phones"}, allowGetters = true)
@Entity
@Table(name = "Users", schema = "EXTERNAL")
public class User implements WorkshopEntity, Serializable {
	
	@Transient
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_sequence")
	@SequenceGenerator(name = "users_sequence", schema = "EXTERNAL", initialValue = 100, allocationSize = 1)
//	@Max(groups = PersistenceCheck.class, value = 0, message = "{validation.max}")
//	@Min(groups = UpdationCheck.class, value = 1, message = "{validation.minimumDigitalValue}")
	@PositiveOrZero(message = "{validation.positiveOrZero}")
	private long id;
	
	@Column
	private String firstName;
	
	@Column
	private String lastName;
	
	/**
	 * Can be written from JSON to the User.class as a raw password to math with encoded one from DB
	 * But must not be serialized as an encoded password from DB while serializing!
	 */
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Column
	private String password;
	
	/**
	 * Can be used as a Login identity
	 */
	@Column(unique = true)
	private String email;
	
	@Column(nullable = false)
	@PastOrPresent(message = "{validation.pastOrPresent}")
	private LocalDateTime created;
	
	@Column
	@Null(groups = PersistenceCheck.class, message = "{validation.null}")
	private LocalDateTime modified;
	
	@Column
	private LocalDate birthday;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@OneToMany(mappedBy = "user", orphanRemoval = true, fetch = FetchType.EAGER, cascade = {
		CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	private Set<@Valid Phone> phones;
	
	@JsonIgnore
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@OneToMany(mappedBy = "createdFor", orphanRemoval = false, cascade = {
		CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE})
	private Collection<@Valid Order> orders;
	
	
	private Set<GrantedAuthority> grantedAuthorities;
	
	/**
	 * If the creation date isn't preset, set it by now
	 */
	@PrePersist
	public void setCreationDateTime() {
		if (created == null) {
			setCreated(LocalDateTime.now());
		}
	}
	
	@PreUpdate
	public void updateModificationDateTime() {
		setModified(LocalDateTime.now());
	}
}
