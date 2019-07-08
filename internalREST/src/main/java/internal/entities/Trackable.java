package internal.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import internal.entities.hibernateValidation.CreationCheck;
import internal.entities.hibernateValidation.UpdationCheck;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.*;
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
@MappedSuperclass
public abstract class Trackable implements WorkshopEntity, Serializable {
	
	@Transient
	private static final long serialVersionUID = 3L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "trackable_sequence")
	@SequenceGenerator(name = "trackable_sequence", schema = "INTERNAL", initialValue = 100, allocationSize = 1)
	//TODO: message
	@Null(groups = {CreationCheck.class}, message = "{validation.mustBeNull}")
	@NotNull(groups = {UpdationCheck.class}, message = "{validation.mustBeNotNull}")
	@Min(groups = {UpdationCheck.class}, value = 1)
	private long id;
	
	@Column(nullable = false, updatable = false)
	//TODO: validation message
	@PastOrPresent(groups = {UpdationCheck.class}, message = "{validation.PastOrPresent}")
	private LocalDateTime created;
	
	@Column
	private LocalDateTime modified;
	
	@Column
	//TODO: validation international message
	@FutureOrPresent(groups = {UpdationCheck.class}, message = "{validation.futureOrPresent}")
	private LocalDateTime finished;
	
	/**
	 * Sets automatically in the DaoAbstract.persistEntity() if an Employee is presented in the SecurityContext.
	 */
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	//TODO: message
	@NotNull(groups = {CreationCheck.class}, message = "{validation.mustBeNotNull}")
	@ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE},
		optional = true)
	@JoinColumn(name = "created_by", referencedColumnName = "id", nullable = true, updatable = true)
	private Employee createdBy;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	//TODO: message
	@NotNull(groups = {UpdationCheck.class}, message = "{validation.mustBeNotNull}")
	@ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE})
	@JoinColumn(name = "modified_by", referencedColumnName = "id")
	private Employee modifiedBy;
	
	public Trackable(Employee createdBy) {
		this.createdBy = createdBy;
	}
	
	/**
	 * Only 'Employee' entities can't contain 'createdBy' field as they can create each other
	 *
	 * @throws IllegalArgumentException
	 */
	@PrePersist
	public void prePersist() throws IllegalArgumentException {
		this.created = LocalDateTime.now();
/*
		if (!"Employee".equals(this.getClass().getSimpleName()) && this.createdBy == null) {
			throw new IllegalArgumentException(
				"An Employee in 'createdBy' field must be presented! Use proper constructor with that argument!");
		}
*/
	}
	
	@PreUpdate
	public void preUpdate() throws IllegalArgumentException {
		this.modified = LocalDateTime.now();
		//TODO: to realize createdBy check and insertion implementation
/*
		if (!"Employee".equals(this.getClass().getSimpleName()) && this.modifiedBy == null) {
			throw new IllegalArgumentException(
				"An Employee in 'modifiedBy' field must be presented! Please add that one who's applied for modifying!");
		}
*/
	}
	
	@Override
	public String toString() {
		return "id=" + id;
	}
}
