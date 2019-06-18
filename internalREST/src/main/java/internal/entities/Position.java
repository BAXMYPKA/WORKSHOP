package internal.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

/**
 * Class also plays a role for granting access to the inner App resources by its name
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, of = {"name", "department"})
@Entity
@Table(name = "Positions", schema = "INTERNAL")
@AttributeOverride(name = "finished", column = @Column(name = "deleted"))
public class Position extends Trackable implements GrantedAuthority {
	
	/**
	 * Also uses as the GrantedAuthority name
	 */
	@Column(unique = true, nullable = false)
	private String name;
	
	/**
	 * Contains Russian translation with a little description of the Position
	 */
	@Column(length = 255)
	private String description;
	
	@OneToMany(mappedBy = "position", orphanRemoval = false, cascade = {
		CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH
	})
	private Set<Employee> employees;
	
	@ManyToOne(optional = false, cascade = {
		CascadeType.REMOVE, CascadeType.REFRESH, CascadeType.MERGE
	})
	@JoinTable(name = "Departments_to_Positions", schema = "INTERNAL",
		joinColumns =
		@JoinColumn(name = "position_id", referencedColumnName = "id", nullable = false, table = "Positions"),
		inverseJoinColumns =
		@JoinColumn(name = "department_id", referencedColumnName = "id", nullable = false, table = "Departments"))
	private Department department;
	
	@JsonIgnore
	@Override
	public String getAuthority() {
		return getName();
	}
}
