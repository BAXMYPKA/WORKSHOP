package internal.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true, of = {"createdFor"})
@Entity
@Table(name = "Orders", schema = "INTERNAL")
public class Order extends Trackable {
	
	@Column
	private LocalDateTime deadline;
	
	@Column
	private String description;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
	@ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	private User createdFor;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
	@OneToMany(orphanRemoval = true, mappedBy = "order", fetch = FetchType.EAGER, cascade = {
		CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	private Set<Task> tasks;
	
	@Column(scale = 2)
	private BigDecimal overallPrice;
	
	@Builder
	public Order(Employee createdBy, LocalDateTime deadline, String description, User createdFor, Set<Task> tasks) {
		super(createdBy);
		this.deadline = deadline;
		this.description = description;
		this.createdFor = createdFor;
		this.tasks = tasks;
	}
	
	/**
	 * Before being persisted the Order recalculates overall price that depends on the price of every Task
	 */
	@PrePersist
	@Override
	public void prePersist() throws IllegalArgumentException {
		super.prePersist();
		if (tasks != null && !tasks.isEmpty()){
			setOverallPrice(getTasks().stream().map(Task::getPrice).reduce((a, b) -> a.add(b)).orElseThrow(
				() -> new IllegalArgumentException("Task prices cannot be null!")));
		}
	}
	
	/**
	 * Before being updated the Order recalculates overall price that depends on the price of every Task
	 */
	@PreUpdate
	@Override
	public void preUpdate() throws IllegalArgumentException {
		super.preUpdate();
		prePersist();
	}
}
