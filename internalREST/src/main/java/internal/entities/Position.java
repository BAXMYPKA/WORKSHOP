package internal.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

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
	 * Also uses as the GrantedAuthority
	 */
	@Column(unique = true, nullable = false)
	private String name;
	
	@OneToMany(cascade = CascadeType.REFRESH, mappedBy = "position", orphanRemoval = true)
	private Set<Employee> employees;
	
	@Override
	public String getAuthority() {
		return getName();
	}
}
