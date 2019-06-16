package internal.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

/**
 * Deleting a Department will lead to deleting all the related Positions
 */
@Slf4j
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "Departments", schema = "INTERNAL")
public class Department implements Serializable {
	
	@Transient
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	
	@Column(unique = true, nullable = false)
	private String name;
	
	@OneToMany(fetch = FetchType.EAGER, orphanRemoval = true, mappedBy = "department", cascade = {
		CascadeType.REMOVE, CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH
	})
	private Set<Position> positions;
	
	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof Department)) return false;
		Department dep = (Department) o;
		return id == dep.id && name.equals(dep.name);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(id, name);
	}
}
