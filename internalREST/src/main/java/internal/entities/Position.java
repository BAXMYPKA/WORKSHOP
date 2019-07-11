package internal.entities;

import com.fasterxml.jackson.annotation.*;
import internal.entities.hibernateValidation.PersistenceCheck;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.groups.Default;
import java.util.Collection;

/**
 * Class also plays a role for granting access to the inner App resources by its name
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true, of = {"name", "department"})
@JsonIgnoreProperties(value = {"department"}, allowGetters = true)
@Entity
@Table(name = "Positions", schema = "INTERNAL")
@AttributeOverride(name = "finished", column = @Column(name = "deleted"))
public class Position extends Trackable implements GrantedAuthority {
	
	/**
	 * Also uses as the GrantedAuthority name
	 */
	@Column(unique = true, nullable = false)
	@NotBlank(groups = {Default.class, PersistenceCheck.class}, message = "{validation.notBlank}")
	private String name;
	
	/**
	 * Contains Russian translation with a little description of the Position
	 */
	@Column(length = 255)
	private String description;
	
	@JsonIgnore
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@OneToMany(mappedBy = "position", orphanRemoval = false, cascade = {
		  CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH})
	private Collection<@Valid Employee> employees;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@ManyToOne(optional = false, cascade = {
		  CascadeType.REMOVE, CascadeType.REFRESH, CascadeType.MERGE})
	@JoinTable(name = "Departments_to_Positions", schema = "INTERNAL",
		  joinColumns =
		  @JoinColumn(name = "position_id", referencedColumnName = "id", nullable = false, table = "Positions"),
		  inverseJoinColumns =
		  @JoinColumn(name = "department_id", referencedColumnName = "id", nullable = false, table = "Departments"))
	@Valid
	private Department department;
	
	@JsonIgnore
	@Override
	public String getAuthority() {
		return getName();
	}
}
