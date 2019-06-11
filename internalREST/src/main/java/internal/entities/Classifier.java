package internal.entities;


import javax.persistence.*;

/**
 * The Classifier for Tasks. Loads and updated directly from DB
 */
@Entity
@Table(name = "Classifiers", schema = "INTERNAL")
public class Classifier {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "classifier_sequence")
	@SequenceGenerator(name = "classifier_sequence", schema = "INTERNAL", initialValue = 100)
	private long id;
	
	@Column(nullable = false, unique = true)
	private String name;
}
