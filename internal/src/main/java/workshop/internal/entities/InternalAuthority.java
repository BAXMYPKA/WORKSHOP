package workshop.internal.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.core.GrantedAuthority;
import workshop.internal.entities.hibernateValidation.PersistenceValidation;
import workshop.internal.entities.hibernateValidation.MergingValidation;
import workshop.internal.entities.utils.PermissionType;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.groups.Default;
import java.util.Set;

/**
 * Serves as the {@link GrantedAuthority} for the INTERNAL.{@link Employee}s for fine tuning their {@link PermissionType}.
 * The class is the container for {@link PermissionType}.
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = false, onlyExplicitlyIncluded = true)
@JsonIgnoreProperties(value = {"employees", "position"}, allowSetters = true)
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity(name = "Internal_Authority")
@Table(name = "Internal_Authorities", schema = "INTERNAL")
public class InternalAuthority extends Trackable implements GrantedAuthority {
	
	@Transient
	@Getter(AccessLevel.PRIVATE)
	private static final long serialVersionUID = WorkshopEntity.serialVersionUID;
	
	@Column(unique = true, nullable = false)
	@NotBlank(groups = {Default.class, PersistenceValidation.class, MergingValidation.class}, message = "{validation.notBlank}")
	@EqualsAndHashCode.Include
	@ToString.Include
	private String name;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@OneToMany(mappedBy = "internalAuthority", orphanRemoval = true, fetch = FetchType.EAGER,
		  cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.PERSIST, CascadeType.REMOVE})
	private Set<@Valid AuthorityPermission> authorityPermissions;
	
	@Column
	@Length(groups = {Default.class, PersistenceValidation.class, MergingValidation.class}, max = 254,
			message = "{validation.length}")
	private String description;
	
	/**
	 * Not serializable to JSON (to prevent huge amount of Users).
	 * But it is possible to deserialize from JSON to Object with the Users' set within particular WorkshopGrantedAuthority.
	 */
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@ManyToMany(targetEntity = Position.class, fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
	@JoinTable(name = "Positions_To_Internal_Authorities", schema = "INTERNAL",
		  joinColumns = {@JoinColumn(name = "internal_authority_id", nullable = false)},
		  inverseJoinColumns = {@JoinColumn(name = "position_id", nullable = false)})
	private Set<@Valid Position> positions;
	
	@Builder
	public InternalAuthority(String name) {
		this.name = name;
	}
	
	/**
	 * For compatibility with {@link GrantedAuthority} interface.
	 *
	 * @return InternalAuthority name.
	 */
	public String getAuthority() {
		return name;
	}
}