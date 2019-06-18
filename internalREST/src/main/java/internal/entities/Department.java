package internal.entities;

import lombok.*;
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
@EqualsAndHashCode(of = {"id", "name"})
@ToString(of = {"id", "name"})
@Entity
@Table(name = "Departments", schema = "INTERNAL")
public class Department implements WorkshopEntity, Serializable {
	
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
}
