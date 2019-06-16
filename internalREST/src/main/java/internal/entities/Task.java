package internal.entities;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

/**
 * Can be appointed to an Employee in the creation time or can be self-appointed that's why 'appointedTo' field can be null
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "Tasks", schema = "INTERNAL")
public class Task extends Trackable {
	
	@Column
	private String name;
	
	@Column
	//TODO: check the date has to be before the linked Order "deadline"
	private LocalDateTime deadline;
	
	@ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE})
	@JoinColumn(name = "appointed_to")
	private Employee appointedTo;
	
	/**
	 * One Task can have a number of Classifiers
	 */
	@ManyToMany(fetch = FetchType.EAGER, cascade = {
		CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE})
	@JoinTable(name = "Tasks_to_Classifiers", schema = "INTERNAL",
		joinColumns = {@JoinColumn(name = "task_id")},
		inverseJoinColumns = {@JoinColumn(name = "classifier_id")})
	private Set<Classifier> classifiers;
	
	@ManyToOne(optional = false, cascade = {
		CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE})
	@JoinColumn(name = "order_id", referencedColumnName = "id")
	private Order order;
	
	//TODO: to do!
	private BigDecimal price;
}
