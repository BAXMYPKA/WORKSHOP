package workshop.internal.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import workshop.internal.entities.hibernateValidation.PersistenceValidation;
import workshop.internal.entities.hibernateValidation.UpdateValidation;
import workshop.internal.entities.utils.PermissionType;
import workshop.internal.entities.utils.PermissionTypeToPropertyConverter;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
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
@Entity(name = "Authority_Permission")
@Table(name = "Authority_Permissions", schema = "INTERNAL")
public class AuthorityPermission extends Trackable {
	
	@Column(unique = true, nullable = false)
	@Enumerated(EnumType.STRING)
	@Convert(converter = PermissionTypeToPropertyConverter.class)
	@NotBlank(groups = {PersistenceValidation.class, UpdateValidation.class, Default.class},
		message = "{validation.notBlank}")
	private PermissionType permissionType;
	
	@Column
	@Size(groups = {PersistenceValidation.class, UpdateValidation.class, Default.class}, max = 254,
		message = "{validation.size}")
	private String description;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@ManyToMany(mappedBy = "authorityPermissions", fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
	private Set<@Valid WorkshopEntityType> workshopEntityTypes;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
	@JoinTable(name = "Internal_Authorities_to_Authorities_Permissions", schema = "INTERNAL",
		joinColumns = @JoinColumn(name = "permission_id", referencedColumnName = "id", nullable = false),
		inverseJoinColumns = @JoinColumn(name = "internal_authority_id", referencedColumnName = "id", nullable = false))
	private Set<@Valid InternalAuthority> internalAuthorities;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
	@JoinTable(name = "External_Authorities_to_Authorities_Permissions", schema = "EXTERNAL",
		joinColumns = @JoinColumn(name = "permission_id", referencedColumnName = "id", nullable = false),
		inverseJoinColumns = @JoinColumn(name = "external_authority_id", referencedColumnName = "id", nullable = false))
	private Set<@Valid ExternalAuthority> externalAuthorities;
	
}
