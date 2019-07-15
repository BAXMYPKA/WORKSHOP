package internal.entities;

import com.fasterxml.jackson.annotation.*;
import internal.entities.hibernateValidation.PersistenceCheck;
import internal.entities.hibernateValidation.UpdationCheck;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.*;
import javax.validation.groups.Default;
import java.io.Serializable;
import java.util.Collection;

/**
 * Deleting a Department will lead to deleting all the related Positions
 */
@Slf4j
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = {"id", "name"})
@ToString(of = {"id", "name"})
@JsonIgnoreProperties(value = {"positions"})
@Entity
@Table(name = "Departments", schema = "INTERNAL")
public class Department implements WorkshopEntity, Serializable {
	
	@Transient
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "departments_sequence")
	@SequenceGenerator(name = "departments_sequence", schema = "INTERNAL", initialValue = 100, allocationSize = 1)
//	@Max(groups = PersistenceCheck.class, value = 0, message = "{validation.max}")
//	@Min(groups = UpdationCheck.class, value = 1, message = "{validation.minimumDigitalValue}")
	@PositiveOrZero(message = "{validation.positiveOrZero}")
	private long id;
	
	@Column(unique = true, nullable = false)
	@NotBlank(groups = {Default.class, PersistenceCheck.class, UpdationCheck.class}, message = "{validation.notBlank}")
	private String name;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@OneToMany(fetch = FetchType.EAGER, orphanRemoval = true, mappedBy = "department", cascade = {
		CascadeType.REMOVE, CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	private Collection<@Valid Position> positions;
	
	public Department(@NotBlank(
		groups = {Default.class, PersistenceCheck.class, UpdationCheck.class},
		message = "{validation.notBlank}") String name) {
		this.name = name;
	}
}
