package internal.entities;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "Orders", schema = "INTERNAL")
public class Order implements Serializable {
	
	@Transient
	private static	final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orders_sequence")
	@SequenceGenerator(name = "orders_sequence", schema = "INTERNAL", initialValue = 100, allocationSize = 1)
	private long id;
	
	@Column
	private LocalDateTime deadline;
	
	@Column
	private LocalDateTime finished;
	
	@Column
	private String description;
	
	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private User createdFor;
	
	@OneToMany(orphanRemoval = true, mappedBy = "order", fetch = FetchType.EAGER, cascade = {
		CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH	})
	private Set<Task> tasks;
	
	@Embedded
	private TrackingInfo trackingInfo;
	
	/**
	 * Automatically creates a new TrackingInfo with the presented Employee
	 * @param createdBy
	 */
	public Order(Employee createdBy, LocalDateTime deadline, String description, User createdFor, Set<Task> tasks) {
		this.deadline = deadline;
		this.description = description;
		this.createdFor = createdFor;
		this.tasks = tasks;
		this.setTrackingInfo(new TrackingInfo(createdBy));
	}
	
	/**
	 * If a TrackingInfo with createdBy Employee had been pre created
	 * @param trackingInfo
	 */
	public Order(TrackingInfo trackingInfo, LocalDateTime deadline, String description, User createdFor, Set<Task> tasks) {
		this.deadline = deadline;
		this.description = description;
		this.createdFor = createdFor;
		this.tasks = tasks;
		this.trackingInfo = trackingInfo;
	}
	
	public TrackingInfo getTrackingInfo() {
		return this.trackingInfo;
	}
	
	public void setTrackingInfo(TrackingInfo trackingInfo) {
		this.trackingInfo = trackingInfo;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Order)) return false;
		Order order = (Order) o;
		return getId() == order.getId() &&
			getTrackingInfo().equals(order.getTrackingInfo());
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getId(), getTrackingInfo());
	}
}
