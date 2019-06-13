package internal.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Requires using the constructor with Employee
 * "created" and "modified" fields are updated automatically
 */
@Getter
@Setter
@NoArgsConstructor
@Embeddable
public class TrackingInfo implements Serializable {
	
	@Transient
	private static final long serialVersionUID = 1L;
	
	@Column(nullable = false, updatable = false)
	private LocalDateTime created;
	
	@Column
	private LocalDateTime modified;
	
	@ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE},
		optional = false)
	@JoinColumn(name = "created_by", referencedColumnName = "id", nullable = false, updatable = false)
	private Employee createdBy;
	
	@ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE})
	@JoinColumn(name = "modified_by", referencedColumnName = "id")
	private Employee modifiedBy;
	
	public TrackingInfo(Employee createdBy) {
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
	public void preUpdate() {
		this.modified = LocalDateTime.now();
		if (this.modifiedBy == null) {
			throw new IllegalArgumentException(
				"An Employee in 'modifiedBy' field must be presented! Please add that one who's applied for modifying!");
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof TrackingInfo)) return false;
		TrackingInfo that = (TrackingInfo) o;
		return created.equals(that.created) &&
			createdBy.equals(that.createdBy);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(created, createdBy);
	}
}
