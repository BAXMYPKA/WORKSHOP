package internal.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

/**
 * The Classifier for Tasks. Loads and updated directly from DB
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "Classifiers", schema = "INTERNAL")
public class Classifier implements Serializable {
	
	@Transient
	private static	final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "classifiers_sequence")
	@SequenceGenerator(name = "classifiers_sequence", schema = "INTERNAL", initialValue = 100, allocationSize = 1)
	private long id;
	
	@Column(nullable = false, unique = true)
	private String name;
}
