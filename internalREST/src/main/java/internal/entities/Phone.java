package internal.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "Phones", schema = "INTERNAL")
public class Phone implements Serializable {
	
	@Transient
	private static	final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "phones_sequence")
	@SequenceGenerator(name = "phones_sequence", schema = "INTERNAL", initialValue = 100, allocationSize = 1)
	private long id;
	
	@Column
	private String name;
	
	@Column
	private String phone;
	
	private Employee employee;
	
//	private User user;
}
