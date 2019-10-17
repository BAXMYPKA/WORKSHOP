package workshop.internal.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import workshop.internal.entities.hibernateValidation.PersistenceValidation;
import workshop.internal.entities.hibernateValidation.MergingValidation;
import workshop.internal.exceptions.PersistenceFailureException;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.http.HttpStatus;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.groups.Default;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Belongs to a particular Order.
 * Cannot be deleted if that Order is already finished.
 * Can be appointed to an Employee in the creation time or can be self-appointed that's why 'appointedTo' field can be null
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true, of = {"price", "appointedTo"})
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@JsonIgnoreProperties(value = {"order", "workshopEntityName"}, allowGetters = true)
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "Tasks", schema = "INTERNAL")
public class Task extends WorkshopAudibleEntityAbstract {
	
	@Transient
	private static final long serialVersionUID = WorkshopEntity.serialVersionUID;
	
	/**
	 * If not explicitly given, all the included Classifiers names will be concatenated into this 'name' property.
	 */
	@Column
	@EqualsAndHashCode.Include
	private String name;
	
	@Column
	@Future(groups = {PersistenceValidation.class}, message = "{validation.future}")
	private ZonedDateTime deadline;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.EAGER)
	@JoinColumn(name = "appointed_to")
	private Employee appointedTo;
	
	/**
	 * One Task can have a number of Classifiers
	 * Validation note: this field is validated by setters
	 * To remove a Classifier with the Price recalculation use {@link #removeClassifier(Classifier...)} method!
	 */
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
	@JoinTable(name = "Tasks_to_Classifiers", schema = "INTERNAL",
			   joinColumns = {@JoinColumn(name = "task_id", nullable = false)},
			   inverseJoinColumns = {@JoinColumn(name = "classifier_id", nullable = false)})
	private Set<@Valid Classifier> classifiers;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
	@JoinColumn(name = "order_id", referencedColumnName = "id")
	@Valid
	@NotNull(groups = {PersistenceValidation.class, MergingValidation.class}, message = "{validation.notNull}")
	private Order order;
	
	/**
	 * The sum of the all included Classifiers prices.
	 * Will be set automatically as the sum of the all included Classifiers prices.
	 * Use Task.addClassifier() of Task.deleteClassifier() to correctly set Task.price.
	 * Of course can be corrected manually.
	 * Default = 0.00
	 */
	@Column(scale = 2, nullable = false)
	@PositiveOrZero(groups = {Default.class, PersistenceValidation.class, MergingValidation.class},
					message = "{validation.positiveOrZero}")
	private BigDecimal price = BigDecimal.ZERO;
	
	@Builder
	public Task(String name, Order order) {
		this.name = name;
		this.order = order;
	}
	
	public void setAppointedTo(@Valid Employee appointedTo) {
		this.appointedTo = appointedTo;
	}
	
	public void setOrder(@Valid Order order) {
		this.order = order;
	}
	
	/**
	 * ALso adds an every Classifier.price to this Task.price
	 *
	 * @param classifiers
	 * @throws IllegalArgumentException
	 */
	public void addClassifier(@Valid Classifier... classifiers) throws IllegalArgumentException {
		if (classifiers == null) {
			throw new IllegalArgumentException("Classifier cannot be null!");
		}
		if (this.classifiers == null) {
			this.classifiers = new HashSet<>(3);
		}
		Stream.of(classifiers).forEach(classifierToAdd -> {
			this.classifiers.add(classifierToAdd);
			price = price == null ? classifierToAdd.getPrice() : price.add(classifierToAdd.getPrice());
		});
	}
	
	/**
	 * This method also recalculates the Price of the Task.
	 */
	public void removeClassifier(@Valid Classifier... classifiers) {
		if (classifiers == null) {
			throw new IllegalArgumentException("Classifier cannot be null!");
		}
		if (this.classifiers == null) {
			return;
		}
		price = price.subtract(
			Arrays.stream(classifiers).map(Classifier::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add)
		);
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
		price = classifiers.stream().map(Classifier::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
	}
	
	/**
	 * Checks if the Order this Task belongs to is not finished (finished Orders cannot be modified).
	 *
	 * @throws PersistenceFailureException If the Order this Task belongs to is already finished and cannot be modified.
	 *                                     This RuntimeException is intended to break a Transaction with this removing.
	 */
	@PreRemove
	public void preRemove() throws PersistenceFailureException {
		if (order != null && order.getFinished() != null &&
			order.getFinished().withZoneSameLocal(ZoneId.of("UTC"))
				.isBefore(ZonedDateTime.now().withZoneSameLocal(ZoneId.of("UTC")))) {
			throw new PersistenceFailureException("This Task.Order is already finished and cannot be modified!",
				HttpStatus.FORBIDDEN, "httpStatus.forbidden.removeFromFinishedOrderForbidden");
		}
	}
	
	@Override
	public Long getIdentifier() {
		return super.getIdentifier();
	}
	
	/**
	 * Forces the included Order to be pre-updated (so that that Order also be updated with this renewed Task).
	 * If this Task hasn't been given a name, it concatenates all the names of the included Classifiers for this.
	 */
	@PreUpdate
	@Override
	public void preUpdate() {
		super.preUpdate();
		getOrder().preUpdate();
		if (this.name == null && this.classifiers != null) {
			this.classifiers.forEach(classifier -> this.name += classifier.getName() + "&");
		}
	}
}