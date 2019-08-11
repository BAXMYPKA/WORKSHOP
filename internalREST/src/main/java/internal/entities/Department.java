package internal.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import internal.entities.hibernateValidation.PersistenceCheck;
import internal.entities.hibernateValidation.UpdationCheck;
import internal.exceptions.WorkshopException;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.hateoas.Identifiable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Null;
import javax.validation.constraints.Positive;
import javax.validation.groups.Default;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

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
	@Positive(groups = Default.class, message = "{validation.positive}")
	@Null(groups = PersistenceCheck.class, message = "{validation.null}")
	private Long identifier;
	
	@Column(unique = true, nullable = false)
	@NotBlank(groups = {Default.class, PersistenceCheck.class, UpdationCheck.class}, message = "{validation.notBlank}")
	private String name;
	
	@Column(updatable = false)
	private ZonedDateTime created;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@OneToMany(fetch = FetchType.EAGER, orphanRemoval = true, mappedBy = "department", cascade = {
		CascadeType.REMOVE, CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	private Collection<@Valid Position> positions;
	
	public Department(String name) {
		this.name = name;
	}
	
	public void addPosition(@Valid Position position) {
		if (position == null) {
			throw new IllegalArgumentException("Method argument Position cannot be null!");
		}
		if (getPositions() == null) {
			setPositions(new HashSet<Position>(Collections.singletonList(position)));
		} else {
			getPositions().add(position);
		}
	}
	
	@PrePersist
	public void prePersist() {
		if (created == null) {
			created = ZonedDateTime.now();
		}
	}
}
