package internal.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import internal.entities.hibernateValidation.MergingValidation;
import internal.entities.hibernateValidation.PersistenceValidation;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
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
@ToString(of = {"identifier", "phone"})
@JsonIgnoreProperties(value = {"employee", "user"}, allowGetters = true)
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(name = "Phones", schema = "INTERNAL")
public class Phone extends WorkshopEntityAbstract {
	
	@Transient
	private static final long serialVersionUID = WorkshopEntity.serialVersionUID;
	
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "phones_sequence")
	@SequenceGenerator(name = "phones_sequence", schema = "INTERNAL", initialValue = 100, allocationSize = 1)
	@NotNull(groups = {MergingValidation.class, Default.class}, message = "{validation.notNull}")
	@Positive(groups = {MergingValidation.class, Default.class}, message = "{validation.positive}")
	@Null(groups = {PersistenceValidation.class}, message = "{validation.null}")
	@EqualsAndHashCode.Include
	private Long identifier;
	
	@Column
	@EqualsAndHashCode.Include
	private String name;
	
	@Column(updatable = false)
	@PastOrPresent(groups = {PersistenceValidation.class}, message = "{validation.pastOrPresent}")
	@EqualsAndHashCode.Include
	private ZonedDateTime created;
	
	/**
	 * Min digits = 4, Max = 15.
	 * Can starts with '+', may contain '()', '-' and single whitespaces.
	 */
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@Column(unique = true, nullable = false)
	@NotNull(groups = {Default.class, PersistenceValidation.class, MergingValidation.class}, message = "{validation.notNull}")
	@Pattern(groups = {Default.class, PersistenceValidation.class, MergingValidation.class}, message = "{validation.phone}",
		regexp = "^(\\+?\\s?-?\\(?\\d\\)?-?\\s?){5,15}[^\\s\\D]$")
	@EqualsAndHashCode.Include
	private String phone;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, targetEntity = Employee.class)
	@JoinColumn(name = "employee_id")
	@Valid
	private Employee employee;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE}, targetEntity = User.class)
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