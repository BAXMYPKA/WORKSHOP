package workshop.internal.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import workshop.internal.entities.hibernateValidation.PersistenceValidation;
import workshop.internal.entities.hibernateValidation.UpdateValidation;
import workshop.internal.entities.utils.PermissionType;
import workshop.internal.entities.utils.PermissionTypeToPropertyConverter;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.groups.Default;
import java.util.Set;

/**
 * The class is the container for {@link WorkshopEntityType}.
 * Class instances are equal to {@link PermissionType},
 * Which is also representation of {@link org.springframework.http.HttpMethod}s ENUM
 * as the permission types for 'GET' ('read'), 'POST' ('modify'), 'PUT' ('write'), 'DELETE' ('delete').
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@JsonIgnoreProperties
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity(name = "Authority_Permission")
@Table(name = "Authority_Permissions", schema = "INTERNAL")
public class AuthorityPermission extends Trackable {
	
	@Transient
	@Getter(AccessLevel.PRIVATE)
	private static final long serialVersionUID = WorkshopEntity.serialVersionUID;
	
	@Column(unique = false, nullable = false)
	@Enumerated(EnumType.STRING)
	@Convert(converter = PermissionTypeToPropertyConverter.class)
	@NotBlank(groups = {PersistenceValidation.class, UpdateValidation.class, Default.class},
		message = "{validation.notBlank}")
	@EqualsAndHashCode.Include
	private PermissionType permissionType;
	
	@Column
	@Size(groups = {PersistenceValidation.class, UpdateValidation.class, Default.class}, max = 254,
		message = "{validation.size}")
	@EqualsAndHashCode.Include
	private String description;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
//	@NotNull(groups = {PersistenceValidation.class, UpdateValidation.class, Default.class}, message = "{validation.notNull}")
	@Valid
	private InternalAuthority internalAuthority;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
	@Valid
	private ExternalAuthority externalAuthority;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@ManyToMany(mappedBy = "authorityPermissions", fetch = FetchType.EAGER,
		  cascade = {CascadeType.MERGE, CascadeType.REFRESH})
	@EqualsAndHashCode.Include
	private Set<@Valid WorkshopEntityType> workshopEntityTypes;
	
}
