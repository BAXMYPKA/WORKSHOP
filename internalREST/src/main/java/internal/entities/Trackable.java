package internal.entities;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Class to be only extended by Entities in the 'INTERNAL' schema
 * Requires using the constructor with Employee as a single argument
 * "created" and "modified" fields are updated automatically
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = {"id", "created"})
@MappedSuperclass
public abstract class Trackable implements Serializable {
	
	@Transient
	private static final long serialVersionUID = 3L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "trackable_sequence")
	@SequenceGenerator(name = "trackable_sequence", schema = "INTERNAL", initialValue = 100, allocationSize = 1)
	private long id;
	
	@Column(nullable = false, updatable = false)
	private LocalDateTime created;
	
	@Column
	private LocalDateTime modified;
	
	@Column
	private LocalDateTime finished;
	
	
	@ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE},
		optional = true)
	@JoinColumn(name = "created_by", referencedColumnName = "id", nullable = true, updatable = false)
	private Employee createdBy;
	
	@ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE})
	@JoinColumn(name = "modified_by", referencedColumnName = "id")
	private Employee modifiedBy;
	
	public Trackable(Employee createdBy) {
		this.createdBy = createdBy;
	}
	
	@PrePersist
	public void prePersist() throws IllegalArgumentException {
		this.created = LocalDateTime.now();
		if (this.createdBy == null) {
			throw new IllegalArgumentException(
				"An Employee in 'createdBy' field must be presented! Use proper constructor with that argument!");
		}
	}
	
	@PreUpdate
	public void preUpdate() throws IllegalArgumentException {
		this.modified = LocalDateTime.now();
		if (this.modifiedBy == null) {
			throw new IllegalArgumentException(
				"An Employee in 'modifiedBy' field must be presented! Please add that one who's applied for modifying!");
		}
	}
}
