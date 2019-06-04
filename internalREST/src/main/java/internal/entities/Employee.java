package internal.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "EMPLOYEES", schema = "INTERNAL")
public class Employee implements Serializable {
	
	@Transient
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	
	@Column(name = "first_name", nullable = false, length = 100)
	private String firstName;
	
	@Column(name = "last_name", nullable = false, length = 100)
	private String lastName;
	
	@Column(nullable = false)
	private String password;
	
	@Column(nullable = false, length = 100)
	private String email;
	
	@Column(nullable = false, length = 100)
	private String phone;
	
	@Lob
	@Column(length = 5242880) //5Mb
	private byte[] photo;
	
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinTable(name = "Employees_to_Positions",
		schema = "INTERNAL",
		joinColumns = @JoinColumn(table = "Employees", name = "employee_id", referencedColumnName = "id"),
		inverseJoinColumns = @JoinColumn(table = "Positions", name = "position_id", referencedColumnName = "id"))
	private Position position;
}
