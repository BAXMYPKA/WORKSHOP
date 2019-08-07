package internal.entities;

import com.fasterxml.jackson.annotation.*;
import internal.entities.hibernateValidation.PersistenceCheck;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.*;
import javax.validation.groups.Default;
import java.time.LocalDate;
import java.util.Collection;

/**
 * Employee's personal data should not be exposed outside for Users
 * Resigned employees are not deleted. They have to be moved to the Archived version of DataBase
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true, of = {"email"})
@JsonIgnoreProperties(value = {"appointedTasks", "ordersModifiedBy", "ordersCreatedBy", "tasksModifiedBy", "tasksCreatedBy"})
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Entity
@Table(name = "Employees", schema = "INTERNAL")
@AttributeOverrides({
	@AttributeOverride(name = "finished", column = @Column(name = "gotFired")),
	@AttributeOverride(name = "createdBy", column = @Column(name = "createdBy", nullable = true)),
	@AttributeOverride(name = "created", column = @Column(name = "employed"))})
public class Employee extends Trackable {
	
	@Column(name = "first_name", nullable = false, length = 100)
	@NotBlank(groups = {Default.class, PersistenceCheck.class}, message = "{validation.notBlank}")
	private String firstName;
	
	@Column(name = "last_name", nullable = false, length = 100)
	@NotBlank(groups = {Default.class, PersistenceCheck.class}, message = "{validation.notBlank}")
	private String lastName;
	
	/**
	 * To exclude original password from DB to be included in JSON
	 * Setter, in the contrary, is intended to add a raw password input from User to be compared
	 */
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Column(nullable = false, length = 255) //Uses for storing BCrypt encoded passwords with the min length = 60
	@Pattern(groups = {PersistenceCheck.class}, regexp = "\\w{5,}", message = "{validation.passwordStrength}")
	private String password;
	
	@Column(nullable = false, length = 100)
	@NotBlank(message = "{validation.notBlank}")
	@Email(message = "{validation.email}")
	private String email;
	
	@Column(nullable = false)
	@NotNull(message = "{validation.notNull}")
	@Past(message = "{validation.past}")
	private LocalDate birthday;
	
	@Column
	private Boolean isEnabled = true;
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@OneToMany(mappedBy = "employee", fetch = FetchType.EAGER, orphanRemoval = true, cascade = {
		CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE})
	private Collection<@Valid Phone> phones;
	
	//TODO: to implement a photo loader Controller method
	@JsonIgnore
	@Lob
	@Column(length = 5242880) //5Mb
	@Size(max = 5242880, message = "{validation.photoSize}")
	private byte[] photo;
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@ManyToOne(optional = false, cascade = {
		CascadeType.REMOVE, CascadeType.MERGE, CascadeType.REFRESH})
	@JoinTable(name = "Employees_to_Positions", schema = "INTERNAL",
		joinColumns = @JoinColumn(table = "Employees", name = "employee_id", referencedColumnName = "id"),
		inverseJoinColumns = @JoinColumn(table = "Positions", name = "position_id", referencedColumnName = "id"))
	@NotNull(message = "{validation.notNull}")
	@Valid
	private Position position;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@OneToMany(mappedBy = "appointedTo", cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE})
	private Collection<@Valid Task> appointedTasks;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@OneToMany(mappedBy = "modifiedBy", cascade = {CascadeType.REMOVE}, targetEntity = Order.class)
	private Collection<Trackable> ordersModifiedBy;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@OneToMany(mappedBy = "createdBy", cascade = {CascadeType.REMOVE}, targetEntity = Order.class)
	private Collection<Trackable> ordersCreatedBy;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@OneToMany(mappedBy = "modifiedBy", cascade = {CascadeType.REMOVE}, targetEntity = Task.class)
	private Collection<Trackable> tasksModifiedBy;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@OneToMany(mappedBy = "createdBy", cascade = {CascadeType.REMOVE}, targetEntity = Task.class)
	private Collection<Trackable> tasksCreatedBy;
	
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
	
	public void setIsEnabled(Boolean enabled) {
		isEnabled = enabled != null ? enabled : true;
	}
}
