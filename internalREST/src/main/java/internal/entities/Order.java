package internal.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
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
	
	@Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP()")
	private LocalDateTime created;
	
	@Column
	//TODO: not less than this.created
	private LocalDateTime modified;
	
	@Column
	//TODO: if has to be set, check whether all the linked Tasks don't have more prolonged Date
	private LocalDateTime deadline;
	
	@Column
	//TODO: to check future date
	private LocalDateTime finished;
	
	@Column
	private String description;
	
	@Column(nullable = false)
	private Employee createdBy;
	
	@Column(nullable = false)
	private User createdFor;
	
	@OneToMany(orphanRemoval = true, mappedBy = "order", fetch = FetchType.EAGER, cascade = {
		CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH	})
	private Set<Task> tasks;
	
	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private User user;
}
