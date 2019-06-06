package internal.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

/**
 * Class also plays a role for granting access to the inner App resources by its name
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "Positions", schema = "INTERNAL")
public class Position implements GrantedAuthority, Serializable {
	
	@Transient
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	
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
	
	@OneToMany(cascade = CascadeType.REFRESH, mappedBy = "position", orphanRemoval = true)
	private Set<Employee> employees;
	
	@ManyToOne(optional = false, cascade = CascadeType.REFRESH)
	@JoinTable(name = "Departments_to_Positions",
		schema = "INTERNAL",
		joinColumns = {
			@JoinColumn(name = "position_id", referencedColumnName = "id", nullable = false, table = "Positions")},
		inverseJoinColumns = {
			@JoinColumn(name = "department_id", referencedColumnName = "id", nullable = false, table = "Departments")})
	private Department department;
	
	@Override
	public String getAuthority() {
		return getName();
	}
}
