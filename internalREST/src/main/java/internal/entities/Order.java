package internal.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import internal.entities.hibernateValidation.CreationCheck;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.Future;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true, of = {"createdFor"})
//@JsonIgnoreProperties(value = {"tasks", "createdFor"}, allowGetters = true)
//@JsonIgnoreProperties(value = {"tasks"}, allowGetters = true)
@Entity
@Table(name = "Orders", schema = "INTERNAL")
public class Order extends Trackable {
	
	@Column
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	//TODO: to create a multilanguage validation messaging
	@Future(groups = {CreationCheck.class})
	private LocalDateTime deadline;
	
	@Column
	private String description;
	
//	@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = User.class)
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	private User createdFor;
	
//	@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Task.class)
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@OneToMany(orphanRemoval = true, mappedBy = "order", fetch = FetchType.EAGER, cascade = {
		CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	private Set<Task> tasks;
	
	/**
	 * Sets automatically as the sum of the all included Tasks.
	 * Also can be set or corrected manually.
	 */
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
