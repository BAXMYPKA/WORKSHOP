package internal.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

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
	private static	final long serialVersionUID = 1L;
	
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
	
	@Column
	private Classifier classifier;
	
/*
	@Column(nullable = false)
	private Order order;
*/
}
