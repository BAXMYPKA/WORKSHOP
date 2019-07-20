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
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.groups.Default;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Set;

/**
 * The Classifier is the atomic description of one of the possible service to be performed for the customers.
 * May contain the name (or not), the description (or not), and the price (or may be free).
 * Official Classifiers are presented in the official List of possible services (price list).
 * Non-official are those not presented in the official price list and can be created on the fly to be included in the Task.
 * 	They wont be presented in the official price list and will be stored within their Tasks. But it is possible to
 * 	edit them and set isOfficial = true.
 * Classifier(s) have to be included in one Task.
 * Task(s) have to be included in one Order. The Order summarizes the prices of the all Classifiers as the final price.
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true, of = {"name"})
@JsonIgnoreProperties(value = {"tasks"})
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
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
	@NotBlank(groups = {UpdationCheck.class, PersistenceCheck.class}, message = "{validation.notBlank}")
	private String name;
	
	@Column
	private String description;
	
	/**
	 * Official Classifiers are presented in the official list of services.
	 * Non-official are made up on the spot to present a kind of service out of the official price list.
	 */
	@Column(nullable = false)
	private boolean isOfficial = false;
	
	/**
	 * The current price for the particular kind of service. It will be stored into Order at the moment of the Order
	 * creation to fix it, because this current price can be changed further.
	 * May be = 0 (some work for free, for instance).
	 * Default = 0;
	 */
	@Column(nullable = false, scale = 2)
	@PositiveOrZero(message = "{validation.positiveOrZero}")
	private BigDecimal price = BigDecimal.ZERO;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@ManyToMany(mappedBy = "classifiers", cascade = {
		CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE})
	private Set<@Valid Task> tasks;
}
