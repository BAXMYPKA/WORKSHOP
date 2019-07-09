package internal.entities;


import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import internal.entities.hibernateValidation.PersistenceCheck;
import internal.entities.hibernateValidation.UpdationCheck;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Set;

/**
 * The Classifier for the Tasks. Each Classifier contains the price for its kind of work.
 * Loads and updated directly from DB
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true, of = {"name"})
@JsonIgnoreProperties(value = {"tasks"})
@Entity
@Table(name = "Classifiers", schema = "INTERNAL")
@AttributeOverride(name = "finished", column = @Column(name = "deleted"))
public class Classifier extends Trackable implements Serializable {
	
/*
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "classifiers_sequence")
	@SequenceGenerator(name = "classifiers_sequence", schema = "INTERNAL", initialValue = 100, allocationSize = 1)
	private long id;
*/
	
	@Column(nullable = false, unique = true)
	@NotBlank(groups = {PersistenceCheck.class, UpdationCheck.class}, message = "{validation.notBlank}")
	private String name;
	
	@Column
	private String description;
	
	/**
	 * The current price for the particular kind of service. It will be stored into Order at the moment of the Order
	 * creation to fix it, because this current price can be changed further.
	 * May be = 0 (some work for free, for instance).
	 * Default = 0;
	 */
	@Column(nullable = false, scale = 2)
	private BigDecimal price = new BigDecimal(0);
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@ManyToMany(mappedBy = "classifiers", cascade = {
		CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE})
	private Set<Task> tasks;
}
