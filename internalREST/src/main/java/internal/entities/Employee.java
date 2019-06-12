package internal.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.context.annotation.Description;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "Employees", schema = "INTERNAL")
public class Employee implements Serializable {
	
	@Transient
	private static final long serialVersionUID = 1L;
	
	//TODO: to implement sequence generator that starts from 20L otherwise there will be a collision!
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	
	@Column(name = "first_name", nullable = false, length = 100)
	private String firstName;
	
	@Column(name = "last_name", nullable = false, length = 100)
	private String lastName;
	
	@Column(nullable = false, length = 255) //Uses for storing BCrypt encoded passwords with the min length = 60
	private String password;
	
	@Column(nullable = false, length = 100)
	private String email;
	
	@Column(nullable = false, columnDefinition = "")
	private LocalDate birthday;
	
//	@Column(nullable = false, length = 100)
	@OneToMany(mappedBy = "employee", fetch = FetchType.EAGER, orphanRemoval = true, cascade = {
		CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH	})
	private Set<Phone> phones;
	
	@Lob
	@Column(length = 5242880) //5Mb
	private byte[] photo;
	
	@ManyToOne(optional = true, cascade = {
		CascadeType.REMOVE, CascadeType.MERGE, CascadeType.REFRESH
	})
	@JoinTable(name = "Employees_to_Positions", schema = "INTERNAL",
		joinColumns = @JoinColumn(table = "Employees", name = "employee_id", referencedColumnName = "id"),
		inverseJoinColumns = @JoinColumn(table = "Positions", name = "position_id", referencedColumnName = "id"))
	private Position position;
	
	@Override
	public String toString() {
		return new StringJoiner(", ", Employee.class.getSimpleName() + "[", "]")
			.add("id=" + id)
			.add("firstName='" + firstName + "'")
			.add("lastName='" + lastName + "'")
			.add("email='" + email + "'")
			.add("birthday=" + birthday)
			.add("phone='" + phones.iterator().next() + "'")
			.add("position=" + position)
			.toString();
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Employee)) return false;
		Employee employee = (Employee) o;
		return id == employee.id &&
			email.equals(employee.email);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(id, email);
	}
}
