package workshop.internal.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import workshop.internal.entities.hibernateValidation.Persist;
import workshop.internal.entities.hibernateValidation.Merge;
import workshop.exceptions.IllegalArgumentsException;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.http.HttpStatus;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Class also plays a role for granting access to the inner App resources by its name
 * by containing a set of GrantedAuthorities
 */
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true, of = {"name", "department"})
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@JsonIgnoreProperties(value = {"department", "internalGrantedAuthorities", "workshopEntityName"}, allowGetters = true)
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Entity
@Table(name = "Positions", schema = "INTERNAL")
@AttributeOverride(name = "finished", column = @Column(name = "deleted"))
public class Position extends WorkshopAudibleEntityAbstract {
	
	@Transient
	private static final long serialVersionUID = WorkshopEntity.serialVersionUID;
	
	/**
	 * Also uses as the WorkshopGrantedAuthority name
	 */
	@Column(unique = true, nullable = false)
	@NotBlank(groups = {Default.class, Persist.class, Merge.class}, message = "{validation.notBlank}")
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
	@NotNull(groups = {Merge.class, Default.class}, message = "{validation.notNull}")
	private Department department;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@ManyToMany(mappedBy = "positions", fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
	private Set<@Valid InternalAuthority> internalAuthorities;
	
	@Builder
	public Position(@NotBlank(groups = {Default.class, Persist.class}, message = "{validation.notBlank}") String name, @Valid Department department) {
		this.name = name;
		this.department = department;
	}
	
	/**
	 * Works properly ONLY under a Transaction.
	 */
	public void addInternalAuthority(InternalAuthority... internalAuthorities) {
		if (internalAuthorities == null || internalAuthorities.length == 0) {
			throw new IllegalArgumentsException("'internalAuthorities' parameter cannot be null or empty!",
				"httpStatus.internalServerError.common", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		if (this.internalAuthorities == null) {
			this.internalAuthorities = new HashSet<>(Arrays.asList(internalAuthorities));
		}
	}
	
	public void setDepartment(Department department) {
		this.department = department;
		this.department.addPosition(this);
	}
	
	@Override
	public Long getIdentifier() {
		return super.getIdentifier();
	}
}
