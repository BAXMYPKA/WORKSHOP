package internal.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "Users", schema = "EXTERNAL")
public class User implements Serializable {
	
	@Transient
	private static	final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_sequence")
	@SequenceGenerator(name = "users_sequence", schema = "EXTERNAL", initialValue = 100, allocationSize = 1)
	private long id;
	
	@OneToMany(mappedBy = "user", orphanRemoval = true, fetch = FetchType.EAGER, cascade = {
		CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	private Set<Phone> phones;
}
