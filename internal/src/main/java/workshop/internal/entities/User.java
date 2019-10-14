package workshop.internal.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.security.core.GrantedAuthority;
import workshop.internal.entities.hibernateValidation.PersistenceValidation;
import workshop.internal.entities.hibernateValidation.MergingValidation;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.*;
import javax.validation.groups.Default;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Stream;

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
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(of = {"identifier", "email", "firstName"})
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "Users", schema = "EXTERNAL")
public class User extends WorkshopEntityAbstract {
	
	//TODO: how to determine and fix the particular phone the User is using for login? And fix it in the JwtUtils getToken method!
	
	@Transient
	private static final long serialVersionUID = WorkshopEntity.serialVersionUID;
	
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_sequence")
	@SequenceGenerator(name = "users_sequence", schema = "EXTERNAL", initialValue = 100, allocationSize = 1)
	@NotNull(groups = {MergingValidation.class, Default.class}, message = "{validation.notNull}")
	@Positive(groups = {MergingValidation.class, Default.class}, message = "{validation.positive}")
	@Null(groups = {PersistenceValidation.class}, message = "{validation.null}")
	@EqualsAndHashCode.Include
	private Long identifier;
	
	@Column
	@EqualsAndHashCode.Include
	private String firstName;
	
	@Column
	@EqualsAndHashCode.Include
	private String lastName;
	
	/**
	 * Can be written from JSON to the User.class as a raw password to math with encoded one from DB
	 * But must not be serialized as an encoded password from DB while serializing!
	 */
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Column
	@Pattern(regexp = "^(\\w){5,36}$", message = "{validation.passwordStrength}")
	private String password;
	
	/**
	 * Can be used as a Login identity
	 */
	@Column(unique = true)
	@Email(message = "{validation.email}")
	@EqualsAndHashCode.Include
	private String email;
	
	@Column(nullable = false)
	@PastOrPresent(message = "{validation.pastOrPresent}")
	private ZonedDateTime created;
	
	@Column
	@Null(groups = PersistenceValidation.class, message = "{validation.null}")
	private ZonedDateTime modified;
	
	@Column
	@Past(message = "{validation.past}")
	@EqualsAndHashCode.Include
	private LocalDate birthday;
	
	@Column(name = "enabled")
	private Boolean isEnabled = true;
	
	/**
	 * Is set by {@link workshop.internal.services.UsersService#persistEntity(User)} when it is possible to obtain the
	 * {@link Locale} and its LanguageTag from creator's context.
	 * It is accepted when no special "languageTag" was set during creation then it will be set from the creator's
	 * context.
	 */
	@Column
	private String languageTag;
	
	/**
	 * One of the Phones can be used as a Login identity.
	 */
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@OneToMany(mappedBy = "user", orphanRemoval = true, fetch = FetchType.EAGER,
		cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE})
	private Set<@Valid Phone> phones = new HashSet<>(2);
	
	@JsonIgnore
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@OneToMany(mappedBy = "createdFor", orphanRemoval = false, fetch = FetchType.LAZY,
		cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	private Collection<@Valid Order> orders;
	
	/**
	 * {@link ExternalAuthority}
	 * Available individual permissions within Workshop security realm.
	 */
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@ManyToMany(targetEntity = ExternalAuthority.class, fetch = FetchType.EAGER,
		cascade = {CascadeType.REFRESH, CascadeType.MERGE})
	@JoinTable(name = "Users_To_External_Authorities", schema = "EXTERNAL",
		joinColumns = {@JoinColumn(name = "user_id", nullable = false)},
		inverseJoinColumns = {@JoinColumn(name = "external_authority_id", nullable = false)})
	private Set<@Valid ExternalAuthority> externalAuthorities;
	
	public User(@Email(message = "{validation.email}") String email) {
		this.email = email;
	}
	
	public User(Set<@Valid Phone> phones) {
		this.phones = phones;
	}
	
	public void setIsEnabled(Boolean enabled) {
		isEnabled = enabled != null ? enabled : true;
	}
	
	public void addPhone(Phone... phone) throws IllegalArgumentException {
		if (phone == null) {
			throw new IllegalArgumentException("Phone object cannot be null!");
		}
		if (phones == null) {
			phones = new HashSet<>(3);
		}
		phones.addAll(Arrays.asList(phone));
	}
	
	public void removePhone(Phone... phone) {
		if (phones == null || phones.isEmpty()) return;
		phones.removeAll(Arrays.asList(phone));
	}
	
	/**
	 * @param grantedAuthority {@link ExternalAuthority} instance.
	 * @throws ClassCastException If the given GrantedAuthority is not the ExternalAuthority instance.
	 */
	public void addGrantedAuthority(GrantedAuthority... grantedAuthority) throws ClassCastException {
		if (externalAuthorities == null) {
			externalAuthorities = new HashSet<>(5);
		}
		Stream.of(grantedAuthority).forEach(authority -> {
			externalAuthorities.add((ExternalAuthority) authority);
		});
	}
	
	/**
	 * @param grantedAuthority {@link ExternalAuthority}
	 */
	public void removeGrantedAuthority(GrantedAuthority... grantedAuthority) {
		if (externalAuthorities == null || externalAuthorities.isEmpty()) {
			return;
		}
		externalAuthorities.removeAll(Arrays.asList(grantedAuthority));
	}
	
	/**
	 * If User.email or User.Set<Phones> are not set - throws an exception. Email or Phone are required as an identity.
	 * If the creation date isn't preset, set it by now
	 */
	@PrePersist
	public void prePersist() throws PersistenceException {
		if ((email == null || email.isEmpty()) && (phones == null || phones.isEmpty())) {
			throw new PersistenceException(
				"User must have either email or phone as a login to be persisted! Enter one of these field");
		}
		if (created == null) {
			setCreated(ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC")));
		}
	}
	
	@PreUpdate
	public void updateModificationDateTime() {
		setModified(ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC")));
	}
}
