package internal.entities;

import java.time.LocalDateTime;

/**
 * Creation DateTime is equal to the corresponding Order.
 * Can be appointed in creation time or self-appointed so that 'appointedTo' field can by null
 */
public class Task {
	
	private long id;
	private String name;
	private LocalDateTime finished;
	private LocalDateTime deadline;
	private Employee appointedTo;
	private Classifier classifier;
	private Order order;
}
