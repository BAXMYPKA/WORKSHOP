package workshop.internal.entities;

import com.fasterxml.jackson.annotation.*;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import workshop.internal.entities.hibernateValidation.Merge;
import workshop.internal.entities.hibernateValidation.Persist;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.*;
import javax.validation.groups.Default;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Employee's personal data should not be exposed outside for Users
 * Resigned employees are not deleted. They have to be moved to the Archived version of DataBase
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true, of = {"email"})
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@JsonIgnoreProperties(value = {
	"appointedTasks", "ordersModifiedBy", "ordersCreatedBy", "tasksModifiedBy", "tasksCreatedBy", "workshopEntityName"})
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Entity
@Table(name = "Employees", schema = "INTERNAL")
@AttributeOverrides({
						@AttributeOverride(name = "finished", column = @Column(name = "gotFired")),
						@AttributeOverride(name = "createdBy", column = @Column(name = "createdBy", nullable = true)),
						@AttributeOverride(name = "created", column = @Column(name = "employed"))})
public class Employee extends WorkshopAudibleEntityAbstract {
	
	@Transient
	private static final long serialVersionUID = WorkshopEntity.serialVersionUID;
	
	@Column(name = "first_name", nullable = false, length = 100)
	@NotBlank(groups = {Default.class, Persist.class, Merge.class}, message = "{validation.notBlank}")
	@Pattern(regexp = "^([\\p{LD}-]){3,50}\\s?([\\p{LD}-]){0,50}\\s?([\\p{LD}-]){0,50}", message = "{validation.pattern.name}")
	@EqualsAndHashCode.Include
	private String firstName;
	
	@Column(name = "last_name", nullable = false, length = 100)
	@NotBlank(groups = {Default.class, Persist.class, Merge.class}, message = "{validation.notBlank}")
	@Pattern(regexp = "^([\\p{LD}-]){3,50}\\s?([\\p{LD}-]){0,50}\\s?([\\p{LD}-]){0,50}", message = "{validation.pattern.name}")
	@EqualsAndHashCode.Include
	private String lastName;
	
	/**
	 * May be presented or not as Employee might have been created by another one.
	 * To exclude original password from DB to be included in JSON
	 * Setter, in the contrary, is intended to add a raw password input from User to be compared
	 */
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Column(nullable = false, length = 255) //Uses for storing BCrypt encoded passwords with the min length = 60
	@Pattern(regexp = "^[\\p{LD}-_+=()*&%$#@!<>\\[\\{\\]\\}\\'\\\"\\;\\:\\?\\/]{5,36}$", message = "{validation.passwordStrength}")
	private String password;
	
	@Column(nullable = false, length = 100)
	@NotBlank(groups = {Default.class, Persist.class, Merge.class}, message = "{validation.notBlank}")
	@Email(groups = {Default.class, Persist.class, Merge.class}, message = "{validation.email}")
	@EqualsAndHashCode.Include
	private String email;
	
	@Column(nullable = false)
	@NotNull(groups = {Default.class, Persist.class, Merge.class}, message = "{validation.notNull}")
	@Past(groups = {Default.class, Persist.class, Merge.class}, message = "{validation.past}")
	private LocalDate birthday;
	
	/**
	 * To disable Employee across a domain.
	 * Default = true
	 */
	@Column
	private Boolean isEnabled = true;
	
	/**
	 * Is set by {@link workshop.internal.services.UsersService#persistEntity(User)} when it is possible to obtain the
	 * {@link Locale} and its LanguageTag from creator's context.
	 * It is accepted when no special "languageTag" was set during creation then it will be set from the creator's
	 * context.
	 */
	@Column
	private String languageTag;
	
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@OneToMany(mappedBy = "employee", fetch = FetchType.EAGER, orphanRemoval = true,
			   cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
	private Set<@Valid Phone> phones;
	
	@JsonIgnore
	@Lob
	@Column(length = 5242880) //5Mb
	@Size(max = 5242880, message = "{validation.photoSize}")
	private byte[] photo;
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
	@JoinColumn(name = "position_id", referencedColumnName = "id", nullable = false)
	@NotNull(groups = {Default.class, Persist.class, Merge.class}, message = "{validation.notNull}")
	@Valid
	private Position position;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@OneToMany(mappedBy = "appointedTo", fetch = FetchType.LAZY,
			   cascade = {CascadeType.MERGE, CascadeType.REFRESH})
	private Set<@Valid Task> appointedTasks;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@OneToMany(mappedBy = "modifiedBy", fetch = FetchType.LAZY, targetEntity = Task.class,
			   cascade = {CascadeType.MERGE, CascadeType.REFRESH})
	private Set<WorkshopAudibleEntityAbstract> tasksModifiedBy;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY, targetEntity = Task.class,
			   cascade = {CascadeType.MERGE, CascadeType.REFRESH})
	private Set<WorkshopAudibleEntityAbstract> tasksCreatedBy;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@OneToMany(mappedBy = "modifiedBy", fetch = FetchType.LAZY, targetEntity = Order.class,
			   cascade = {CascadeType.MERGE, CascadeType.REFRESH})
	private Set<WorkshopAudibleEntityAbstract> ordersModifiedBy;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY, targetEntity = Order.class,
			   cascade = {CascadeType.MERGE, CascadeType.REFRESH})
	private Set<WorkshopAudibleEntityAbstract> ordersCreatedBy;
	
	/**
	 * All the arguments of this constructor are obligatory to be set!
	 * Also you can use Builder to construct.
	 */
	@Builder
	public Employee(String firstName, String lastName, String password, String email, LocalDate birthday, Position position) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.password = password;
		this.email = email;
		this.birthday = birthday;
		this.position = position;
	}
	
	/**
	 * Can be performed only under a Transaction.
	 */
	public void addPhone(Phone... phones) {
		for (Phone phone : phones) {
			phone.setEmployee(this);
		}
		if (this.phones != null) {
			this.phones.addAll(Arrays.asList(phones));
		} else {
			this.phones = new HashSet<>(Arrays.asList(phones));
		}
	}
}
