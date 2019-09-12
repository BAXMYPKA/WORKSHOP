package internal.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import internal.entities.hibernateValidation.PersistenceValidation;
import internal.entities.hibernateValidation.UpdateValidation;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;
import java.util.Set;

/**
 * Class also plays a role for granting access to the inner App resources by its name
 * by implementing WorkshopGrantedAuthority interface
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true, of = {"name", "department"})
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@JsonIgnoreProperties(value = {"department"}, allowGetters = true)
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Entity
@Table(name = "Positions", schema = "INTERNAL")
@AttributeOverride(name = "finished", column = @Column(name = "deleted"))
public class Position extends Trackable implements GrantedAuthority {
	
	@Transient
	private static final long serialVersionUID = WorkshopEntity.serialVersionUID;
	
	/**
	 * Also uses as the WorkshopGrantedAuthority name
	 */
	@Column(unique = true, nullable = false)
	@NotBlank(groups = {Default.class, PersistenceValidation.class, UpdateValidation.class}, message = "{validation.notBlank}")
	@EqualsAndHashCode.Include
	private String name;
	
	/**
	 * Contains Russian translation with a little description of the Position
	 */
	@Column
	private String description;
	
	@JsonIgnore
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@OneToMany(mappedBy = "position", orphanRemoval = false, fetch = FetchType.LAZY,
		cascade = {CascadeType.MERGE, CascadeType.REFRESH})
	private Set<@Valid Employee> employees;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@ManyToOne(optional = false, cascade = {CascadeType.REFRESH, CascadeType.MERGE},
		fetch = FetchType.EAGER)
	@Valid
	@NotNull(groups = {UpdateValidation.class, Default.class}, message = "{validation.notNull}")
	private Department department;
	
	@Builder
	public Position(@NotBlank(groups = {Default.class, PersistenceValidation.class}, message = "{validation.notBlank}") String name, @Valid Department department) {
		this.name = name;
		this.department = department;
	}
	
	public void setDepartment(Department department) {
		this.department = department;
		this.department.addPosition(this);
	}
	
	@JsonIgnore
	@Override
	public String getAuthority() {
		return getName();
	}
	
	@Override
	public Long getIdentifier() {
		return super.getIdentifier();
	}
}
