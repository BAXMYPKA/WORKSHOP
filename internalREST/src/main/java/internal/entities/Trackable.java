package internal.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import internal.entities.hibernateValidation.PersistEmployeeValidation;
import internal.entities.hibernateValidation.PersistenceValidation;
import internal.entities.hibernateValidation.UpdateValidation;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.*;
import javax.validation.groups.Default;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Class to be only extended by Entities in the 'INTERNAL' schema
 * Requires using the constructor with Employee as a single argument
 * "created" and "modified" fields are updated automatically
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@JsonIgnoreProperties(value = {"createdBy", "modifiedBy"}, allowGetters = true)
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@MappedSuperclass
public abstract class Trackable extends WorkshopEntityAbstract {
	
	@Transient
	private static final long serialVersionUID = WorkshopEntity.serialVersionUID;
	
	/**
	 * Every instance has to
	 * '@Override
	 * public long getIdentifier() {
	 * 		return super.getIdentifier();
	 * 	}'
	 * 	method so that not to clash ResourceSupport.getIdentifier() with WorkshopEntity.getIdentifier()
	 */
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "trackable_sequence")
	@SequenceGenerator(name = "trackable_sequence", schema = "INTERNAL", initialValue = 100, allocationSize = 1)
	@NotNull(groups = {UpdateValidation.class, Default.class}, message = "{validation.notNull}")
	@Positive(groups = {UpdateValidation.class, Default.class}, message = "{validation.positive}")
	@Null(groups = {PersistenceValidation.class}, message = "{validation.null}")
	@EqualsAndHashCode.Include
	private Long identifier;
	
	/**
	 * @AttributeOverrided as 'employed' for Employee.
	 * Also for this entity can be set manually with PersistEmployeeValidation.class validation group to be set.
	 */
	@Column(nullable = false, updatable = false)
	@PastOrPresent(groups = {PersistEmployeeValidation.class},
		message = "{validation.pastOrPresent}")
	@Null(groups = {PersistenceValidation.class}, message = "{validation.null}")
	@EqualsAndHashCode.Include
	private ZonedDateTime created;
	
	@Column
	@Null(groups = {PersistenceValidation.class}, message = "{validation.null}")
	private ZonedDateTime modified;
	
	@Column
	@PastOrPresent(message = "{validation.pastOrPresent}")
	private ZonedDateTime finished;
	
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
	 * 'Employee' entities persist this field as 'employed'. For them can be set manually.*
	 */
	@PrePersist
	public void prePersist() {
		if (this.created == null) {
			this.created = ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC"));
		}
	}
	
	@PreUpdate
	public void preUpdate() {
		this.modified = ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC"));
	}
	
	@Override
	public String toString() {
		return "identifier=" + identifier;
	}
	
	@Override
	public void setIdentifier(Long identifier) {
		this.identifier = identifier;
	}
	
	/**
	 * @param identifier Primitive parameter just to simplify setting without adding 'L' suffix
	 */
	public void setIdentifier(long identifier) {
		this.identifier = identifier;
	}
}
