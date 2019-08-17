package internal.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import internal.entities.hibernateValidation.PersistenceValidation;
import internal.entities.hibernateValidation.MergingValidation;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.groups.Default;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Set;

/**
 * The Classifier is the atomic description of one of the possible services to be performed for the customers.
 * May contain the name (or not), the description (or not), and the price (or may be free).
 * Official Classifiers are presented in the official List of possible services (price list).
 * Non-official are those not presented in the official price list and can be created on the fly to be included in the Task.
 * They wont be presented in the official price list and will be stored within their Tasks. But it is possible to
 * edit them and set isOfficial = true.
 * Classifier(s) have to be included in one Task.
 * Task(s) have to be included in one Order. The Order summarizes the prices of the all Classifiers as the final price.
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@JsonIgnoreProperties(value = {"tasks"})
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Entity
@Table(name = "Classifiers", schema = "INTERNAL")
@AttributeOverride(name = "finished", column = @Column(name = "deleted"))
public class Classifier extends Trackable implements Serializable {
	
	@Transient
	private static final long serialVersionUID = WorkshopEntity.serialVersionUID;
	
	@Column(nullable = false, unique = true)
	@NotBlank(groups = {MergingValidation.class, PersistenceValidation.class, Default.class}, message = "{validation.notBlank}")
	@EqualsAndHashCode.Include
	private String name;
	
	@Column
	private String description;
	
	/**
	 * Official Classifiers are presented in the official list of services.
	 * Non-official are made up on the spot to present a kind of services out of the official price list.
	 * Default = false (as Java primitive fields initialized by default)
	 */
	@Column(nullable = false)
	private boolean isOfficial = false;
	
	/**
	 * The current price for the particular kind of services. It will be stored into Order at the moment of the Order
	 * creation to fix it, because this current price can be changed further.
	 * May be = 0 (some work for free, for instance).
	 * Default = 0.00;
	 */
	@Column(nullable = false, scale = 2)
	@PositiveOrZero(groups = {PersistenceValidation.class, MergingValidation.class, Default.class},
		message = "{validation.positiveOrZero}")
	private BigDecimal price = BigDecimal.ZERO;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@ManyToMany(mappedBy = "classifiers", cascade = {
		CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE})
	private Set<@Valid Task> tasks;
	
	@Builder
	public Classifier(@NotBlank(groups = {MergingValidation.class, PersistenceValidation.class}, message = "{validation.notBlank}")
						  String name,
					  String description,
					  boolean isOfficial,
					  @PositiveOrZero(message = "{validation.positiveOrZero}")
						  BigDecimal price) {
		this.name = name;
		this.description = description;
		this.isOfficial = isOfficial;
		this.price = price;
	}
	
	@Override
	public Long getIdentifier() {
		return super.getIdentifier();
	}
}
