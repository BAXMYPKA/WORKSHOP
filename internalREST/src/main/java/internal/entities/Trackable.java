package internal.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import internal.entities.hibernateValidation.PersistEmployeeCheck;
import internal.entities.hibernateValidation.PersistenceCheck;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.hateoas.ResourceSupport;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.*;
import javax.validation.groups.Default;
import java.io.Serializable;
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
@EqualsAndHashCode(of = {"identifier"})
@JsonIgnoreProperties(value = {"createdBy", "modifiedBy"}, allowGetters = true)
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@MappedSuperclass
public abstract class Trackable extends ResourceSupport implements WorkshopEntity, Serializable {
	
	@Transient
	private static final long serialVersionUID = 4L;
	
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
	@PositiveOrZero(groups = Default.class, message = "{validation.positiveOrZero}")
	private Long identifier;
	
	/**
	 * @AttributeOverrided as 'employed' for Employee.
	 * Also for this entity can be set manually with PersistEmployeeCheck.class validation group to be set.
	 */
	@Column(nullable = false, updatable = false)
	@PastOrPresent(groups = {PersistEmployeeCheck.class}, message = "{validation.pastOrPresent}")
	@Null(groups = {PersistenceCheck.class}, message = "{validation.null}")
	private ZonedDateTime created;
	
	@Column
	@Null(groups = {PersistenceCheck.class}, message = "{validation.null}")
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
	
	public int compareTo(Trackable o) {
		Trackable obj = (Trackable) o;
		if (this.identifier > obj.getIdentifier()) {
			return 1;
		} else if (this.identifier < obj.getIdentifier()) {
			return -1;
		} else {
			return 0;
		}
	}
	
//	@Override
//	public int compareTo(WorkshopEntity o) {
//		return 0;
//	}
	
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
