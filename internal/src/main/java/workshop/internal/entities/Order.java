package workshop.internal.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import workshop.internal.entities.hibernateValidation.PersistenceValidation;
import workshop.internal.entities.hibernateValidation.UpdateValidation;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.Future;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.groups.Default;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true, of = {"createdFor"})
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "Orders", schema = "INTERNAL")
public class Order extends Trackable {
	
	@Transient
	private static final long serialVersionUID = WorkshopEntity.serialVersionUID;
	
	@Column
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@Future(groups = {PersistenceValidation.class}, message = "{validation.future}")
	@EqualsAndHashCode.Include
	private ZonedDateTime deadline;
	
	@Column
	@EqualsAndHashCode.Include
	private String description;
	
	/**
	 * Enabled by @EnableJpaAudition
	 * If an Order is created by User himself - this field is filling in automatically in the DaoAbstract.persistEntity()
	 * (if an User is presented in the SecurityContext).
	 */
	@CreatedBy //Only in a case when an User is a creator of the Order. Otherwise is set by hand
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.EAGER)
	@Valid
	private User createdFor;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@OneToMany(orphanRemoval = true, mappedBy = "order", fetch = FetchType.EAGER,
		cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	private Set<@Valid Task> tasks;
	
	/**
	 * Sets automatically as the sum of the all included Tasks.
	 * Also can be set or corrected manually.
	 */
	@Column(scale = 2)
	@PositiveOrZero(groups = {Default.class, PersistenceValidation.class, UpdateValidation.class},
		message = "{validation.positiveOrZero}")
	@EqualsAndHashCode.Include
	private BigDecimal overallPrice = BigDecimal.ZERO;
	
	@Builder
	public Order(Employee createdBy, ZonedDateTime deadline, String description, User createdFor, Set<Task> tasks) {
		super(createdBy);
		this.deadline = deadline;
		this.description = description;
		this.createdFor = createdFor;
		this.tasks = tasks;
	}
	
	/**
	 * After creating an Order the overallPrice is fixed at that moment (as the prices may change in time)
	 * to present a bill with the fixed overallPrice.
	 * Inserting a new Set<Task> will recalculate an overallPrice field so that to renew it and change the previously
	 * fixed price.
	 * If you only want to add a new Task with an only addition its price to overallPrice, use Order.addTask() method.
	 * To remove a one Task with proper price subtraction use Order.deleteTask()
	 *
	 * @param tasks new Tasks to recalculate all
	 */
	public void setTasks(Set<@Valid Task> tasks) {
		this.tasks = tasks;
		recalculateOverallPrice();
	}
	
	/**
	 * Add a Task and adds its price to the Order.overallPrice.
	 * FetchType of {@link #tasks} MUST BY EAGER!
	 *
	 */
	public void addTask(@Valid Task task) {
		if (tasks == null) {
			tasks = new HashSet<>(3);
		}
		tasks.add(task);
		setOverallPrice(overallPrice.add(task.getPrice()));
	}
	
	/**
	 * Deletes the Task with the subtraction its price from Order.overallPrice
	 *
	 * @param task
	 */
	public void removeTask(@Valid Task task) {
		if (task == null || tasks.isEmpty()) return;
		tasks.remove(task);
		setOverallPrice(overallPrice.subtract(task.getPrice()));
	}
	
	/**
	 * Before being persisted the Order recalculates overall price that depends on the price of every Task
	 */
	@PrePersist
	@Override
	public void prePersist() {
		super.prePersist();
		recalculateOverallPrice();
	}
	
	/**
	 * Before being updated the Order recalculates overall price that depends on the price of every Task
	 */
	@PreUpdate
	@Override
	public void preUpdate() throws IllegalArgumentException {
		super.preUpdate();
	}
	
	private void recalculateOverallPrice() throws IllegalArgumentException {
		if (tasks != null && !tasks.isEmpty()) { //Sets the sum of all included Task's prices
			setOverallPrice(getTasks().stream().map(Task::getPrice).reduce((a, b) -> a.add(b)).orElseThrow(
				() -> new IllegalArgumentException("Task prices cannot be null!")));
		}
	}
}