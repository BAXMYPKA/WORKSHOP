package internal.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.context.annotation.Description;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

/**
 * Resigned employees are not deleted. They have to be moved to the Archived vesion of DataBase
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, of = {"email"})
@Entity
@Table(name = "Employees", schema = "INTERNAL")
@AttributeOverrides({
	@AttributeOverride(name = "finished", column = @Column(name = "gotFired")),
	@AttributeOverride(name = "createdBy", column = @Column(name = "createdBy", nullable = true))
})
public class Employee extends Trackable {
	
	@Column(name = "first_name", nullable = false, length = 100)
	private String firstName;
	
	@Column(name = "last_name", nullable = false, length = 100)
	private String lastName;
	
	/**
	 * To exclude original password from DB to be included in JSON
	 * Setter, in the contrary, is intended to add a raw password input from User to be compared
	 */
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Column(nullable = false, length = 255) //Uses for storing BCrypt encoded passwords with the min length = 60
	private String password;
	
	@Column(nullable = false, length = 100)
	private String email;
	
	@Column(nullable = false)
	private LocalDate birthday;
	
	@OneToMany(mappedBy = "employee", fetch = FetchType.EAGER, orphanRemoval = true, cascade = {
		CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
	private Set<Phone> phones;
	
	@JsonIgnore
	@Lob
	@Column(length = 5242880) //5Mb
	private byte[] photo;
	
	@ManyToOne(optional = true, cascade = {
		CascadeType.REMOVE, CascadeType.MERGE, CascadeType.REFRESH})
	@JoinTable(name = "Employees_to_Positions", schema = "INTERNAL",
		joinColumns = @JoinColumn(table = "Employees", name = "employee_id", referencedColumnName = "id"),
		inverseJoinColumns = @JoinColumn(table = "Positions", name = "position_id", referencedColumnName = "id"))
	private Position position;
	
	@OneToMany(mappedBy = "appointedTo", cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE})
	private Set<Task> appointedTasks;
	
	@OneToMany(mappedBy = "modifiedBy", cascade = {CascadeType.REMOVE}, targetEntity = Order.class)
	private Set<Trackable> ordersModifiedBy;
	
	@OneToMany(mappedBy = "createdBy", cascade = {CascadeType.REMOVE}, targetEntity = Order.class)
	private Set<Trackable> ordersCreatedBy;
	
	@OneToMany(mappedBy = "modifiedBy", cascade = {CascadeType.REMOVE}, targetEntity = Task.class)
	private Set<Trackable> tasksModifiedBy;
	
	@OneToMany(mappedBy = "createdBy", cascade = {CascadeType.REMOVE}, targetEntity = Task.class)
	private Set<Trackable> tasksCreatedBy;
}
