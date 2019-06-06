package internal.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

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
	
	@OneToMany(fetch = FetchType.EAGER, orphanRemoval = false, mappedBy = "department")
	private Set<Position> positions;
}
