package internal.entities;

import com.fasterxml.jackson.annotation.*;
import internal.entities.hibernateValidation.PersistenceCheck;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Users of 2 types can be persisted from online and created by manager.
 * Every User must have either email or phone as a login and the way for the further communications.
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
@Entity
@Table(name = "Users", schema = "EXTERNAL")
public class User implements WorkshopEntity, Serializable {
	
	//TODO: how to determine and fix the particular phone the User is using for login? And fix it in the JwtUtils getToken method!
	
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
	
	@Column(name = "enabled")
	private boolean isEnabled = true;
	
	/**
	 * One of the Phones can be used as a Login identity.
	 */
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@OneToMany(mappedBy = "user", orphanRemoval = true, fetch = FetchType.EAGER, cascade = {
		CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	private Set<@Valid Phone> phones;
	
	@JsonIgnore
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@OneToMany(mappedBy = "createdFor", orphanRemoval = false, cascade = {
		CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE})
	private Collection<@Valid Order> orders;
	
	/**
	 * Available individual permissions within Workshop security realm.
	 */
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@ManyToMany(targetEntity = WorkshopGrantedAuthority.class, fetch = FetchType.EAGER, cascade = {
		CascadeType.REFRESH, CascadeType.REMOVE, CascadeType.MERGE})
	@JoinTable(name = "Users_To_GrantedAuthorities", schema = "EXTERNAL",
		joinColumns = {
			@JoinColumn(name = "user_id", nullable = false)},
		inverseJoinColumns = {
			@JoinColumn(name = "WorkshopGrantedAuthority_id", nullable = false)})
	private Set<GrantedAuthority> grantedAuthorities;
	
	public User(String email) {
		this.email = email;
	}
	
	public User(Set<@Valid Phone> phones) {
		this.phones = phones;
	}
	
	public void addPhone(Phone phone) throws IllegalArgumentException {
		if (phone == null) {
			throw new IllegalArgumentException("Phone object cannot be null!");
		}
		if (phones == null) {
			phones = new HashSet<>(3);
		}
		phones.add(phone);
	}
	
	public void deletePhone(Phone phone) {
		if (phones == null || phones.isEmpty()) return;
		phones.remove(phone);
	}
	
	/**
	 * If User.email or User.Set<Phones> are not set - throws an exception. Email or Phone are required as an identity.
	 * If the creation date isn't preset, set it by now
	 */
	@PrePersist
	public void prePersist() throws PersistenceException {
		if ((email == null || email.isEmpty()) && (phones == null || phones.isEmpty())){
			throw new PersistenceException(
				"User must have either email or phone as a login to be persisted! Enter one of these field");
		}
		if (created == null) {
			setCreated(LocalDateTime.now());
		}
	}
	
	@PreUpdate
	public void updateModificationDateTime() {
		setModified(LocalDateTime.now());
	}
}
