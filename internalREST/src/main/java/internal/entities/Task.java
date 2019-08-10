package internal.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import internal.entities.hibernateValidation.PersistenceCheck;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.Future;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Can be appointed to an Employee in the creation time or can be self-appointed that's why 'appointedTo' field can be null
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true, of = {"price", "appointedTo"})
@EqualsAndHashCode(callSuper = true, of = {"order", "name", "deadline"})
@JsonIgnoreProperties(value = {"order"}, allowGetters = true)
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "Tasks", schema = "INTERNAL")
public class Task extends Trackable {
	
	@Transient
	private static final long serialVersionUID = WorkshopEntity.serialVersionUID;
	
	@Column
	private String name;
	
	@Column
	@Future(groups = {PersistenceCheck.class}, message = "{validation.future}")
	private ZonedDateTime deadline;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE})
	@JoinColumn(name = "appointed_to")
	private Employee appointedTo;
	
	/**
	 * One Task can have a number of Classifiers
	 * Validation note: this field is validated by setters
	 */
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@ManyToMany(fetch = FetchType.EAGER, cascade = {
		CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE})
	@JoinTable(name = "Tasks_to_Classifiers", schema = "INTERNAL",
		joinColumns = {@JoinColumn(name = "task_id")},
		inverseJoinColumns = {@JoinColumn(name = "classifier_id")})
	private Set<@Valid Classifier> classifiers;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@ManyToOne(optional = false, cascade = {
		CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE})
	@JoinColumn(name = "order_id", referencedColumnName = "id")
	private Order order;
	
	/**
	 * The sum of the all included Classifiers prices.
	 * Will be set automatically as the sum of the all included Classifiers prices.
	 * Use Task.addClassifier() of Task.deleteClassifier() to correctly set Task.price.
	 * Of course can be corrected manually.
	 */
	@Column(scale = 2, nullable = false)
	@PositiveOrZero(message = "{validation.positiveOrZero}")
	private BigDecimal price = BigDecimal.ZERO;
	
	@Builder
	public Task(String name,
				@Future(groups = {PersistenceCheck.class}, message = "{validation.future}") ZonedDateTime deadline,
				Employee appointedTo,
				Set<@Valid Classifier> classifiers, Order order,
				@PositiveOrZero(message = "{validation.positiveOrZero}") BigDecimal price) {
		this.name = name;
		this.deadline = deadline;
		this.appointedTo = appointedTo;
		this.classifiers = classifiers;
		this.order = order;
		this.price = price;
	}
	
	public void setAppointedTo(@Valid Employee appointedTo) {
		this.appointedTo = appointedTo;
	}
	
	public void setOrder(@Valid Order order) {
		this.order = order;
	}
	
	/**
	 * ALso adds a Classifier.price to this Task.price
	 *
	 * @param classifier
	 * @throws IllegalArgumentException
	 */
	public void addClassifier(@Valid Classifier classifier) throws IllegalArgumentException {
		if (classifier == null) {
			throw new IllegalArgumentException("Classifier cannot be null!");
		}
		if (classifiers == null) {
			classifiers = new HashSet<>(3);
		}
		classifiers.add(classifier);
		setClassifiers(new HashSet<Classifier>(classifiers));
		setPrice(price == null ? classifier.getPrice() : price.add(classifier.getPrice()));
	}
	
	/**
	 * ALso adds an every Classifier.price to this Task.price
	 *
	 * @param classifier
	 * @throws IllegalArgumentException
	 */
	public void addClassifiers(@Valid Classifier... classifier) throws IllegalArgumentException {
		if (classifier == null) {
			throw new IllegalArgumentException("Classifier cannot be null!");
		}
		if (classifiers == null) {
			classifiers = new HashSet<>(3);
		}
		if (price == null) price = new BigDecimal(0);
		
		Stream.of(classifier).forEach(classifierToAdd -> {
			classifiers.add(classifierToAdd);
			price = price.add(classifierToAdd.getPrice());
		});
		setClassifiers(new HashSet<>(classifiers));
		setPrice(new BigDecimal(price.toString()));
	}
	
	/**
	 * Also recalculates the overall price of the Task! Use this with careful.
	 * For deleting or addition the particular Tasks use Task.addClassifier() or Task.addClassifiers()
	 */
	public void setClassifiers(Set<@Valid Classifier> classifiers) {
		if (classifiers == null || classifiers.isEmpty()) {
			return;
		}
		this.classifiers = classifiers;
		
		BigDecimal classifiersPrices = classifiers.stream().map(Classifier::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
		setPrice(classifiersPrices);
		
	}
	
	/**
	 * Before being updated...
	 */
	@PreUpdate
	@Override
	public void preUpdate() {
		super.preUpdate();
		getOrder().preUpdate();
	}
	
	@Override
	public Long getIdentifier() {
		return super.getIdentifier();
	}
}
