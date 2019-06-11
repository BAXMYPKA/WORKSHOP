package internal.entities;

import java.time.LocalDateTime;
import java.util.Set;

public class Order {
	
	private long id;
	private LocalDateTime created;
	private LocalDateTime modified;
	private String description;
	private Employee createdBy;
	private User createdFor;
	private Set<Task> tasks;
}
