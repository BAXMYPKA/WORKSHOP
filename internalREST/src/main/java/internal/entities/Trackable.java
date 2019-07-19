package internal.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import internal.entities.hibernateValidation.PersistenceCheck;
import internal.entities.hibernateValidation.UpdationCheck;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.*;
import javax.validation.groups.Default;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Class to be only extended by Entities in the 'INTERNAL' schema
 * Requires using the constructor with Employee as a single argument
 * "created" and "modified" fields are updated automatically
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
@JsonIgnoreProperties(value = {"createdBy", "modifiedBy"}, allowGetters = true)
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@MappedSuperclass
public abstract class Trackable implements WorkshopEntity, Serializable {
	
	@Transient
	private static final long serialVersionUID = 3L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "trackable_sequence")
	@SequenceGenerator(name = "trackable_sequence", schema = "INTERNAL", initialValue = 100, allocationSize = 1)
//	@Max(groups = PersistenceCheck.class, value = 0, message = "{validation.max}")
//	@Min(groups = UpdationCheck.class, value = 1, message = "{validation.minimumDigitalValue}")
	@PositiveOrZero(groups = Default.class, message = "{validation.positiveOrZero}")
	private long id;
	
	@Column(nullable = false, updatable = false)
	@PastOrPresent(groups = {PersistenceCheck.class}, message = "{validation.pastOrPresent}")
	private LocalDateTime created;
	
	@Column
	@Null(groups = {PersistenceCheck.class}, message = "{validation.null}")
	private LocalDateTime modified;
	
	@Column
	@PastOrPresent(message = "{validation.pastOrPresent}")
	private LocalDateTime finished;
	
	/**
	 * Sets automatically in the DaoAbstract.persistEntity() if an Employee is presented in the SecurityContext.
	 * Also may be set manually.
	 */
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE},
		optional = true)
	@JoinColumn(name = "created_by", referencedColumnName = "id", nullable = true, updatable = true)
	@Valid
	private Employee createdBy;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE})
	@JoinColumn(name = "modified_by", referencedColumnName = "id")
	@Valid
	private Employee modifiedBy;
	
	public Trackable(Employee createdBy) {
		this.createdBy = createdBy;
	}
	
	/**
	 * Only 'Employee' entities may not contain 'createdBy' field as they can create each other
	 *
	 * @throws IllegalArgumentException
	 */
	@PrePersist
	public void prePersist() throws IllegalArgumentException {
		this.created = LocalDateTime.now();
	}
	
	@PreUpdate
	public void preUpdate() throws IllegalArgumentException {
		this.modified = LocalDateTime.now();
	}
	
	@Override
	public String toString() {
		return "id=" + id;
	}
}
