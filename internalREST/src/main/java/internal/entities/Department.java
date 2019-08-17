package internal.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import internal.entities.hibernateValidation.PersistenceValidation;
import internal.entities.hibernateValidation.MergingValidation;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.*;
import javax.validation.groups.Default;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * Deleting a Department will lead to deleting all the related Positions
 */
@Slf4j
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = {"identifier", "name"})
@ToString(of = {"identifier", "name"})
@JsonIgnoreProperties(value = {"positions"})
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
	@SequenceGenerator(name = "departments_sequence", schema = "INTERNAL", initialValue = 100, allocationSize = 1)
	@NotNull(groups = {MergingValidation.class, Default.class}, message = "{validation.notNull}")
	@Positive(groups = {MergingValidation.class, Default.class}, message = "{validation.positive}")
	@Null(groups = {PersistenceValidation.class}, message = "{validation.null}")
	private Long identifier;
	
	@Column(unique = true, nullable = false)
	@NotBlank(groups = {Default.class, PersistenceValidation.class, MergingValidation.class}, message = "{validation.notBlank}")
	private String name;
	
	@Column(updatable = false)
	@PastOrPresent(groups = {PersistenceValidation.class, Default.class}, message = "{validation.pastOrPresent}")
	private ZonedDateTime created;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@OneToMany(fetch = FetchType.EAGER, orphanRemoval = true, mappedBy = "department", cascade = {
		CascadeType.REMOVE, CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	private Collection<@Valid Position> positions;
	
	public Department(String name) {
		this.name = name;
	}
	
	public void addPosition(@Valid Position... positions) {
		if (positions == null) {
			throw new IllegalArgumentException("Method argument Position cannot be null!");
		}
		if (getPositions() == null) {
			setPositions(new HashSet<>(Arrays.asList(positions)));
		} else {
			getPositions().addAll(Arrays.asList(positions));
		}
	}
	
	@PrePersist
	public void prePersist() {
		if (created == null) {
			created = ZonedDateTime.now();
		}
	}
}
