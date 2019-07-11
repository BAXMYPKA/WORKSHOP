package internal.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import internal.entities.hibernateValidation.PersistenceCheck;
import internal.entities.hibernateValidation.UpdationCheck;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.groups.Default;
import java.io.Serializable;

@Getter
@Setter
@Entity
@EqualsAndHashCode(of = {"id", "phone"})
@ToString(of = {"id", "phone"})
@JsonIgnoreProperties(value = {"employee", "user"}, allowGetters = true)
@Table(name = "Phones", schema = "INTERNAL")
public class Phone implements WorkshopEntity, Serializable {
	
	@Transient
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "phones_sequence")
	@SequenceGenerator(name = "phones_sequence", schema = "INTERNAL", initialValue = 100, allocationSize = 1)
	@Max(groups = PersistenceCheck.class, value = 0, message = "{validation.max}")
	@Min(groups = UpdationCheck.class, value = 1, message = "{validation.minimumDigitalValue}")
	private long id;
	
	@Column
	private String name;
	
	@Column(unique = true, nullable = false)
	@NotBlank(groups = {Default.class, PersistenceCheck.class}, message = "{validation.notBlank}")
	@Pattern(groups = {PersistenceCheck.class, UpdationCheck.class},
		regexp = "^(\\+?\\s?-?\\(?\\d\\)?-?\\s?){5,15}[^\\s\\D]$", message = "{validation.phone}")
	private String phone;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
	@JoinColumn(name = "employee_id")
	@Valid
	private Employee employee;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE})
	@JoinColumn(name = "user_id")
	@Valid
	private User user;
}
