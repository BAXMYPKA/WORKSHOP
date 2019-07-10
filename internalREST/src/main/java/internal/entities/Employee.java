package internal.entities;

import com.fasterxml.jackson.annotation.*;
import internal.entities.hibernateValidation.PersistenceCheck;
import internal.entities.hibernateValidation.UpdationCheck;
import lombok.*;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.Collection;

/**
 * Resigned employees are not deleted. They have to be moved to the Archived vesion of DataBase
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true, of = {"email"})
@JsonIgnoreProperties(value = {"appointedTasks", "ordersModifiedBy", "ordersCreatedBy", "tasksModifiedBy", "tasksCreatedBy"})
@Entity
@Table(name = "Employees", schema = "INTERNAL")
@AttributeOverrides({
	  @AttributeOverride(name = "finished", column = @Column(name = "gotFired")),
	  @AttributeOverride(name = "createdBy", column = @Column(name = "createdBy", nullable = true))
})
public class Employee extends Trackable {
	
	@Column(name = "first_name", nullable = false, length = 100)
	@NotBlank(groups = {PersistenceCheck.class, UpdationCheck.class}, message = "{validation.notBlank}")
	private String firstName;
	
	@Column(name = "last_name", nullable = false, length = 100)
	@NotBlank(groups = {PersistenceCheck.class, UpdationCheck.class}, message = "{validation.notBlank}")
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
	@NotBlank(groups = {PersistenceCheck.class, UpdationCheck.class}, message = "{validation.notBlank}")
	@Email(groups = {PersistenceCheck.class, UpdationCheck.class}, message = "{validation.email}")
	private String email;
	
	@Column(nullable = false)
	@NotNull(groups = {PersistenceCheck.class}, message = "{validation.notNull}")
	@Past(groups = {PersistenceCheck.class}, message = "{validation.past}")
	private LocalDate birthday;
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@OneToMany(mappedBy = "employee", fetch = FetchType.EAGER, orphanRemoval = true, cascade = {
		  CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
	private Collection<@Valid Phone> phones;
	
	@JsonIgnore
	@Lob
	@Column(length = 5242880) //5Mb
	@Size(groups = {PersistenceCheck.class, UpdationCheck.class}, max = 5242880, message = "{validation.photoSize}")
	private byte[] photo;
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@ManyToOne(optional = true, cascade = {
		  CascadeType.REMOVE, CascadeType.MERGE, CascadeType.REFRESH})
	@JoinTable(name = "Employees_to_Positions", schema = "INTERNAL",
		  joinColumns = @JoinColumn(table = "Employees", name = "employee_id", referencedColumnName = "id"),
		  inverseJoinColumns = @JoinColumn(table = "Positions", name = "position_id", referencedColumnName = "id"))
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
}
