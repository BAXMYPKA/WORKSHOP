package workshop.internal.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import workshop.internal.entities.hibernateValidation.Persist;
import workshop.internal.entities.hibernateValidation.Merge;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.*;
import javax.validation.groups.Default;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * Deleting a Department will lead to deleting all the related Positions
 */
@Slf4j
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(of = {"identifier", "name"})
@JsonIgnoreProperties(value = {"positions", "workshopEntityName"})
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Entity
@Table(name = "Departments", schema = "INTERNAL")
public class Department extends WorkshopEntityAbstract {
	
	@Transient
	private static long serialVersionUID = WorkshopEntity.serialVersionUID;
	
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "departments_sequence")
	@SequenceGenerator(name = "departments_sequence", schema = "INTERNAL", initialValue = 300, allocationSize = 1)
	@NotNull(groups = {Merge.class, Default.class}, message = "{validation.notNull}")
	@Positive(groups = {Merge.class, Default.class}, message = "{validation.positive}")
	@Null(groups = {Persist.class}, message = "{validation.null}")
	@EqualsAndHashCode.Include
	private Long identifier;
	
	@Column(unique = true, nullable = false)
	@NotBlank(groups = {Default.class, Persist.class, Merge.class}, message = "{validation.notBlank}")
	@EqualsAndHashCode.Include
	private String name;
	
	@Column(updatable = false)
	@PastOrPresent(groups = {Persist.class, Default.class}, message = "{validation.pastOrPresent}")
	@EqualsAndHashCode.Include
	private ZonedDateTime created;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@OneToMany(fetch = FetchType.EAGER, orphanRemoval = true, mappedBy = "department",
			   cascade = {CascadeType.REMOVE, CascadeType.MERGE, CascadeType.REFRESH})
	private Set<@Valid Position> positions;
	
	public Department(String name) {
		this.name = name;
	}
	
	public void addPosition(@Valid Position... positions) {
		if (positions == null) {
			throw new IllegalArgumentException("Method argument Position cannot be null!");
		}
		if (this.positions == null) {
			this.positions = new HashSet<>(Arrays.asList(positions));
		} else {
			this.positions.addAll(Arrays.asList(positions));
		}
	}
	
	@PrePersist
	public void prePersist() {
		if (created == null) {
			created = ZonedDateTime.now();
		}
	}
}
