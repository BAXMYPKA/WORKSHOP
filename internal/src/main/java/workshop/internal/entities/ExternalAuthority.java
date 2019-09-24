package workshop.internal.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.core.GrantedAuthority;
import workshop.internal.entities.hibernateValidation.PersistenceValidation;
import workshop.internal.entities.hibernateValidation.UpdateValidation;
import workshop.internal.entities.utils.PermissionType;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.groups.Default;
import java.util.Set;

/**
 * Serves as the {@link GrantedAuthority} for the EXTERNAL.{@link User}s for fine tuning their {@link PermissionType}.
 * The class is the container for {@link PermissionType}.
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@JsonIgnoreProperties(value = {"users"}, allowSetters = true)
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "External_Authorities", schema = "EXTERNAL")
public class ExternalAuthority extends Trackable implements GrantedAuthority {
	
	@Transient
	private static final long serialVersionUID = WorkshopEntity.serialVersionUID;
	
	@Column(unique = true, nullable = false)
	@NotBlank(groups = {Default.class, PersistenceValidation.class, UpdateValidation.class}, message = "{validation.notBlank}")
	@EqualsAndHashCode.Include
	private String name;
	
	@Column
	@Length(groups = {Default.class, PersistenceValidation.class, UpdateValidation.class}, max = 254,
		message = "{validation.length}")
	private String description;
	
	/**
	 * Not serializable to JSON (to prevent huge amount of Users).
	 * But it is possible to deserialize from JSON to Object with the Users' set within particular WorkshopGrantedAuthority.
	 */
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@ManyToMany(mappedBy = "externalAuthorities", fetch = FetchType.LAZY, cascade = {CascadeType.MERGE})
	private Set<User> users;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@ManyToMany(mappedBy = "externalAuthorities", targetEntity = AuthorityPermission.class, fetch = FetchType.EAGER,
		cascade = {CascadeType.MERGE, CascadeType.REFRESH})
	private Set<@Valid AuthorityPermission> authorityPermissions;
	
	/**
	 * Method name is for compatibility with {@link GrantedAuthority} interface to return a String representation.
	 *
	 * @return {@link #name}
	 */
	@Override
	public String getAuthority() {
		return name;
	}
}