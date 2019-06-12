package internal.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Creation DateTime is equal to the corresponding Order.
 * Can be appointed in creation time or self-appointed so that 'appointedTo' field can by null
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "Tasks", schema = "INTERNAL")
public class Task implements Serializable {
	
	@Transient
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tasks_sequence")
	@SequenceGenerator(name = "tasks_sequence", schema = "INTERNAL", initialValue = 100, allocationSize = 1)
	private long id;
	
	@Column
	private String name;
	
	@Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP()")
	private LocalDateTime created;
	
	@Column
	//TODO: check the date has to be before the linked Order "finished"
	private LocalDateTime finished;
	
	@Column
	//TODO: check the date has to be before the linked Order "deadline"
	private LocalDateTime deadline;
	
	@Column
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
}
