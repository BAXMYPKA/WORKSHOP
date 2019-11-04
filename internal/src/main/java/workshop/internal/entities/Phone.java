package workshop.internal.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import workshop.internal.entities.hibernateValidation.Persist;
import workshop.internal.entities.hibernateValidation.Merge;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.*;
import javax.validation.groups.Default;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@ToString(of = {"identifier", "phone"})
@JsonIgnoreProperties(value = {"employee", "user", "workshopEntityName"})
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "Phones", schema = "INTERNAL")
public class Phone extends WorkshopEntityAbstract {
	
	@Transient
	private static final long serialVersionUID = WorkshopEntity.serialVersionUID;
	
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "phones_sequence")
	@SequenceGenerator(name = "phones_sequence", schema = "INTERNAL", initialValue = 200, allocationSize = 1)
	@NotNull(groups = {Merge.class, Default.class}, message = "{validation.notNull}")
	@Positive(groups = {Merge.class, Default.class}, message = "{validation.positive}")
	@Null(groups = {Persist.class}, message = "{validation.null}")
	@EqualsAndHashCode.Include
	private Long identifier;
	
	@Column(unique = false)
	private String name;
	
	@Column(updatable = false)
	@PastOrPresent(groups = {Persist.class}, message = "{validation.pastOrPresent}")
	private ZonedDateTime created;
	
	/**
	 * Min digits = 4, Max = 15.
	 * Can starts with '+', may contain '()', '-' and single whitespaces.
	 */
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@Column(unique = true, nullable = false)
	@NotNull(groups = {Default.class, Persist.class, Merge.class}, message = "{validation.notNull}")
	@Pattern(groups = {Default.class, Persist.class, Merge.class}, message = "{validation.pattern.phone}",
			 regexp = "^(\\+?\\s?-?\\(?\\d\\)?-?\\s?){5,15}[^\\s\\D]$")
	@EqualsAndHashCode.Include
	private String phone;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@ManyToOne(targetEntity = Employee.class, fetch = FetchType.EAGER,
			   cascade = {CascadeType.MERGE, CascadeType.REFRESH})
	@JoinColumn(name = "employee_id", referencedColumnName = "id")
	@Valid
	private Employee employee;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@ManyToOne(targetEntity = User.class, fetch = FetchType.EAGER, cascade = {CascadeType.REFRESH, CascadeType.MERGE})
	@JoinColumn(name = "user_id", referencedColumnName = "id")
	@Valid
	private User user;
	
	public Phone(String name,
		@NotNull(
			groups = {Default.class, Persist.class, Merge.class},
			message = "{validation.notNull}")
		@Pattern(
			groups = {Default.class, Persist.class, Merge.class},
			message = "{validation.pattern.phone}",
			regexp = "^(\\+?\\s?-?\\(?\\d\\)?-?\\s?){5,15}[^\\s\\D]$")
			String phone) {
		this.name = name;
		this.phone = phone;
	}
	
	@PrePersist
	public void prePersist() {
		if (created == null) {
			created = ZonedDateTime.now();
		}
	}
	
/*
	@PreRemove
	public void preRemove() {
		if (user != null) {
//			Set<Phone> userPhones = new HashSet<>(user.getPhones());
//			boolean contains = userPhones.contains(this);
//			userPhones.forEach(phone1 -> System.out.println("INCLUDED HASH: "+phone1.hashCode()));
//			System.out.println("THIS HASH: "+this.hashCode());
			user.getPhones().remove(this);
		} else if (employee != null) {
			employee.getPhones().remove(this);
		}
	}
*/
}