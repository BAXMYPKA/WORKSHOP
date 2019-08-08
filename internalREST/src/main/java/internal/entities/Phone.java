package internal.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Getter
@Setter
@Entity
@EqualsAndHashCode(of = {"identifier", "phone"})
@ToString(of = {"identifier", "phone"})
@JsonIgnoreProperties(value = {"employee", "user"}, allowGetters = true)
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(name = "Phones", schema = "INTERNAL")
public class Phone extends WorkshopEntityAbstract {
	
//	@Transient
//	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "phones_sequence")
	@SequenceGenerator(name = "phones_sequence", schema = "INTERNAL", initialValue = 100, allocationSize = 1)
	private Long identifier;
	
	@Column
	private String name;
	
	/**
	 * Min digits = 4, Max = 15.
	 * Can starts with '+', may contain '()', '-' and single whitespaces.
	 */
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@Column(unique = true, nullable = false)
	@Pattern(regexp = "^(\\+?\\s?-?\\(?\\d\\)?-?\\s?){5,15}[^\\s\\D]$", message = "{validation.phone}")
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
}