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
public class Order extends Trackable {
	
/*
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orders_sequence")
	@SequenceGenerator(name = "orders_sequence", schema = "INTERNAL", initialValue = 100, allocationSize = 1)
	private long id;
*/
	
	@Column
	private LocalDateTime deadline;
	
	@Column
	private String description;
	
	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private User createdFor;
	
	@OneToMany(orphanRemoval = true, mappedBy = "order", fetch = FetchType.EAGER, cascade = {
		CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH	})
	private Set<Task> tasks;
	
	@Builder
	public Order(Employee createdBy, LocalDateTime deadline, String description, User createdFor, Set<Task> tasks) {
		super(createdBy);
		this.deadline = deadline;
		this.description = description;
		this.createdFor = createdFor;
		this.tasks = tasks;
	}
	
//	@Override
//	public boolean equals(Object o) {
//		if (this == o) return true;
//		if (!(o instanceof Order)) return false;
//		Order order = (Order) o;
//		return getId() == order.getId() &&
//			getTrackable().equals(order.getTrackable());
//	}
//
//	@Override
//	public int hashCode() {
//		return Objects.hash(getId(), getTrackable());
//	}
}
