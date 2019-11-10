package workshop.internal.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import workshop.applicationEvents.OrderFinishedEvent;
import workshop.internal.entities.hibernateValidation.PersistEmployee;
import workshop.internal.entities.hibernateValidation.Persist;
import workshop.internal.entities.hibernateValidation.Merge;
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
@ToString(onlyExplicitlyIncluded = true)
@JsonIgnoreProperties(value = {"createdBy", "modifiedBy"}, allowGetters = true)
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@MappedSuperclass
public abstract class WorkshopAudibleEntityAbstract extends WorkshopEntityAbstract {
	
	@Transient
	private static final long serialVersionUID = WorkshopEntity.serialVersionUID;
	
	/**
	 * Every instance has to
	 * '@Override
	 * public long getIdentifier() {
	 * return super.getIdentifier();
	 * }'
	 * method so that not to clash ResourceSupport.getIdentifier() with WorkshopEntity.getIdentifier()
	 */
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "audible_sequence")
	@SequenceGenerator(name = "audible_sequence", schema = "INTERNAL", initialValue = 150, allocationSize = 1)
	@NotNull(groups = {Merge.class, Default.class}, message = "{validation.notNull}")
	@Positive(groups = {Merge.class, Default.class}, message = "{validation.positive}")
	@Null(groups = {Persist.class}, message = "{validation.null}")
	@EqualsAndHashCode.Include
	@ToString.Include
	private Long identifier;
	
	/**
	 * @AttributeOverrided as 'employed' for Employee.
	 * Also for this entity can be set manually with PersistEmployeeValidation.class validation group to be set.
	 */
	@Column(nullable = false, updatable = false)
	@PastOrPresent(groups = {PersistEmployee.class},
				   message = "{validation.pastOrPresent}")
	@Null(groups = {Persist.class}, message = "{validation.null}")
	private ZonedDateTime created;
	
	@Column
	@Null(groups = {Persist.class}, message = "{validation.null}")
	private ZonedDateTime modified;
	
	/**
	 * For {@link Order} and {@link Task} classes it is MUST BE null at creation time. But later after being set it is
	 * MAY BE an indicator of a finished work to send an {@link OrderFinishedEvent}
	 * to {@link User} if all the {@link Task}s in the particular {@link Order} have their property {@link Task#finished}
	 * set.
	 */
	@Column
	@PastOrPresent(message = "{validation.pastOrPresent}")
	private ZonedDateTime finished;
	
	/**
	 * Sets automatically in the DaoAbstract.persistEntity() if an Employee is presented in the SecurityContext.
	 * Also may be set manually.
	 */
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH}, optional = true, targetEntity = Employee.class)
	@JoinColumn(name = "created_by", referencedColumnName = "id")
	@Valid
	private Employee createdBy;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH}, targetEntity = Employee.class)
	@JoinColumn(name = "modified_by", referencedColumnName = "id")
	@Valid
	private Employee modifiedBy;
	
	public WorkshopAudibleEntityAbstract(Employee createdBy) {
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
