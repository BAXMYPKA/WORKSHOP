package internal.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import internal.entities.hibernateValidation.UpdateValidation;
import internal.entities.hibernateValidation.PersistenceValidation;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.*;
import javax.validation.groups.Default;
import java.time.ZonedDateTime;

@Getter
@Setter
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@ToString(of = {"identifier", "phone"})
@JsonIgnoreProperties(value = {"employee", "user"})
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(name = "Phones", schema = "INTERNAL")
public class Phone extends WorkshopEntityAbstract {
	
	public Phone(String name,
				 @NotNull(
					 groups = {Default.class, PersistenceValidation.class, UpdateValidation.class},
					 message = "{validation.notNull}")
				 @Pattern(
					 groups = {Default.class, PersistenceValidation.class, UpdateValidation.class},
					 message = "{validation.phone}",
					 regexp = "^(\\+?\\s?-?\\(?\\d\\)?-?\\s?){5,15}[^\\s\\D]$")
					 String phone) {
		this.name = name;
		this.phone = phone;
	}
	
	@Transient
	private static final long serialVersionUID = WorkshopEntity.serialVersionUID;
	
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "phones_sequence")
	@SequenceGenerator(name = "phones_sequence", schema = "INTERNAL", initialValue = 200, allocationSize = 1)
	@NotNull(groups = {UpdateValidation.class, Default.class}, message = "{validation.notNull}")
	@Positive(groups = {UpdateValidation.class, Default.class}, message = "{validation.positive}")
	@Null(groups = {PersistenceValidation.class}, message = "{validation.null}")
	@EqualsAndHashCode.Include
	private Long identifier;
	
	@Column
	@EqualsAndHashCode.Include
	private String name;
	
	@Column(updatable = false)
	@PastOrPresent(groups = {PersistenceValidation.class}, message = "{validation.pastOrPresent}")
	private ZonedDateTime created;
	
	/**
	 * Min digits = 4, Max = 15.
	 * Can starts with '+', may contain '()', '-' and single whitespaces.
	 */
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@Column(unique = true, nullable = false)
	@NotNull(groups = {Default.class, PersistenceValidation.class, UpdateValidation.class}, message = "{validation.notNull}")
	@Pattern(groups = {Default.class, PersistenceValidation.class, UpdateValidation.class}, message = "{validation.phone}",
		regexp = "^(\\+?\\s?-?\\(?\\d\\)?-?\\s?){5,15}[^\\s\\D]$")
	@EqualsAndHashCode.Include
	private String phone;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH}, targetEntity = Employee.class,
			   fetch = FetchType.EAGER)
	@JoinColumn(name = "employee_id")
	@Valid
	private Employee employee;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@ManyToOne(cascade = {CascadeType.REFRESH, CascadeType.MERGE}, targetEntity = User.class, fetch = FetchType.EAGER)
	@JoinColumn(name = "user_id", referencedColumnName = "id")
	@Valid
	private User user;
	
	@PrePersist
	public void prePersist() {
		if (created == null) {
			created = ZonedDateTime.now();
		}
	}
}