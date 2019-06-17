package internal.entities;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Can be appointed to an Employee in the creation time or can be self-appointed that's why 'appointedTo' field can be null
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "Tasks", schema = "INTERNAL")
public class Task extends Trackable {
	
	@Column
	private String name;
	
	@Column
	//TODO: check the date has to be before the linked Order "deadline"
	private LocalDateTime deadline;
	
	@ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE})
	@JoinColumn(name = "appointed_to")
	private Employee appointedTo;
	
	/**
	 * One Task can have a number of Classifiers
	 */
	@ManyToMany(fetch = FetchType.EAGER, cascade = {
		CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE})
	@JoinTable(name = "Tasks_to_Classifiers", schema = "INTERNAL",
		joinColumns = {@JoinColumn(name = "task_id")},
		inverseJoinColumns = {@JoinColumn(name = "classifier_id")})
	private Set<Classifier> classifiers;
	
	@ManyToOne(optional = false, cascade = {
		CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE})
	@JoinColumn(name = "order_id", referencedColumnName = "id")
	private Order order;
	
	/**
	 * The sum of all Classifiers
	 */
	@Column(scale = 2)
	private BigDecimal price;
	
	
	public void addClassifier(Classifier classifier) throws IllegalArgumentException {
		if (classifier == null) {
			throw new IllegalArgumentException("Classifier cannot be null!");
		}
		if (classifiers == null) {
			classifiers = new HashSet<>(3);
		}
		classifiers.add(classifier);
		setPrice(price == null ? classifier.getPrice() : price.add(classifier.getPrice()));
	}
	
	/**
	 * Also sets the price for the Task
	 */
	public void setClassifiers(Set<Classifier> classifiers) {
		this.classifiers = classifiers;
		price = new BigDecimal(0);
		classifiers.forEach(classifier -> setPrice(price.add(classifier.getPrice())));
	}
	
	/**
	 * Before being updated the Task recalculates its price according to all Classifiers the Task consists of.
	 */
	@PreUpdate
	@Override
	public void preUpdate() {
		super.preUpdate();
		if (classifiers == null || classifiers.isEmpty()) {
			price = new BigDecimal(0);
		} else {
			price = new BigDecimal(0);
			setPrice(classifiers.stream().map(Classifier::getPrice).reduce(BigDecimal::add).orElseThrow(
				() -> new IllegalArgumentException("One or more Classifiers don't contain the price!")));
		}
	}
}
