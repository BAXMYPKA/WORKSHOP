package internal.entities;

import com.fasterxml.jackson.annotation.*;
import internal.entities.hibernateValidation.MergingCheck;
import internal.entities.hibernateValidation.PersistenceCheck;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.groups.Default;
import java.util.Collection;

/**
 * Class also plays a role for granting access to the inner App resources by its name
 * by implementing WorkshopGrantedAuthority interface
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true, of = {"name", "department"})
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
	@NotBlank(groups = {Default.class, PersistenceCheck.class, MergingCheck.class}, message = "{validation.notBlank}")
	private String name;
	
	/**
	 * Contains Russian translation with a little description of the Position
	 */
	@Column
	private String description;
	
	@JsonIgnore
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@OneToMany(mappedBy = "position", orphanRemoval = false, cascade = {
		CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH})
	private Collection<@Valid Employee> employees;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@ManyToOne(optional = false, cascade = {
		CascadeType.REMOVE, CascadeType.REFRESH, CascadeType.MERGE})
	@JoinTable(name = "Departments_to_Positions", schema = "INTERNAL",
		joinColumns =
		@JoinColumn(name = "position_id", referencedColumnName = "id", nullable = false, table = "Positions"),
		inverseJoinColumns =
		@JoinColumn(name = "department_id", referencedColumnName = "id", nullable = false, table = "Departments"))
	@Valid
	private Department department;
	
	@Builder
	public Position(@NotBlank(groups = {Default.class, PersistenceCheck.class}, message = "{validation.notBlank}") String name, @Valid Department department) {
		this.name = name;
		this.department = department;
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
