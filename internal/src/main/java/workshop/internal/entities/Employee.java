package workshop.internal.entities;

import com.fasterxml.jackson.annotation.*;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import workshop.internal.entities.hibernateValidation.MergingValidation;
import workshop.internal.entities.hibernateValidation.PersistenceValidation;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.*;
import javax.validation.groups.Default;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
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
public class Employee extends Trackable {
	
	@Transient
	private static final long serialVersionUID = WorkshopEntity.serialVersionUID;
	
	@Column(name = "first_name", nullable = false, length = 100)
	@NotBlank(groups = {Default.class, PersistenceValidation.class, MergingValidation.class}, message = "{validation.notBlank}")
	@EqualsAndHashCode.Include
	private String firstName;
	
	@Column(name = "last_name", nullable = false, length = 100)
	@NotBlank(groups = {Default.class, PersistenceValidation.class, MergingValidation.class}, message = "{validation.notBlank}")
	@EqualsAndHashCode.Include
	private String lastName;
	
	/**
	 * May be presented or not as Employee might have been created by another one.
	 * To exclude original password from DB to be included in JSON
	 * Setter, in the contrary, is intended to add a raw password input from User to be compared
	 */
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Column(nullable = false, length = 255) //Uses for storing BCrypt encoded passwords with the min length = 60
	@Pattern(groups = {PersistenceValidation.class}, regexp = "\\w{5,}", message = "{validation.passwordStrength}")
	private String password;
	
	@Column(nullable = false, length = 100)
	@NotBlank(groups = {Default.class, PersistenceValidation.class, MergingValidation.class}, message = "{validation.notBlank}")
	@Email(groups = {Default.class, PersistenceValidation.class, MergingValidation.class}, message = "{validation.email}")
	@EqualsAndHashCode.Include
	private String email;
	
	@Column(nullable = false)
	@NotNull(groups = {Default.class, PersistenceValidation.class, MergingValidation.class}, message = "{validation.notNull}")
	@Past(groups = {Default.class, PersistenceValidation.class, MergingValidation.class}, message = "{validation.past}")
	private LocalDate birthday;
	
	/**
	 * To disable Employee across a domain.
	 * Default = true
	 */
	@Column
	private Boolean isEnabled = true;
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@OneToMany(mappedBy = "employee", fetch = FetchType.EAGER, orphanRemoval = true,
			   cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE})
	private Set<@Valid Phone> phones;
	
	//TODO: to implement a photo loader Controller method
	
	@JsonIgnore
	@Lob
	@Column(length = 5242880) //5Mb
	@Size(max = 5242880, message = "{validation.photoSize}")
	private byte[] photo;
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
	@JoinColumn(name = "position_id", referencedColumnName = "id", nullable = false)
//	@JoinTable(name = "Employees_to_Positions", schema = "INTERNAL",
//			   joinColumns = @JoinColumn(table = "Employees", name = "employee_id", referencedColumnName = "id"),
//			   inverseJoinColumns = @JoinColumn(table = "Positions", name = "position_id", referencedColumnName = "id"))
	@NotNull(groups = {Default.class, PersistenceValidation.class, MergingValidation.class}, message = "{validation.notNull}")
	@Valid
	private Position position;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@OneToMany(mappedBy = "appointedTo", fetch = FetchType.LAZY,
			   cascade = {CascadeType.MERGE, CascadeType.REFRESH})
	private Set<@Valid Task> appointedTasks;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@OneToMany(mappedBy = "modifiedBy", fetch = FetchType.LAZY, targetEntity = Task.class,
			   cascade = {CascadeType.MERGE, CascadeType.REFRESH})
	private Set<Trackable> tasksModifiedBy;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY, targetEntity = Task.class,
			   cascade = {CascadeType.MERGE, CascadeType.REFRESH})
	private Set<Trackable> tasksCreatedBy;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@OneToMany(mappedBy = "modifiedBy", fetch = FetchType.LAZY, targetEntity = Order.class,
			   cascade = {CascadeType.MERGE, CascadeType.REFRESH})
	private Set<Trackable> ordersModifiedBy;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY, targetEntity = Order.class,
			   cascade = {CascadeType.MERGE, CascadeType.REFRESH})
	private Set<Trackable> ordersCreatedBy;
	
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
