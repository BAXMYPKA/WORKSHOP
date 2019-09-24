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

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.groups.Default;
import java.util.Set;

/**
 * The class is the container for {@link WorkshopEntity} types (classes names).
 * All the available {@link WorkshopEntity} class names are available through {@link WorkshopEntity#workshopEntitiesNames}.
 * This class can contain only those exact names for giving access to the according {@link WorkshopEntity}
 * in conjunction with {@link PermissionType}.
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Entity
@Table(name = "Workshop_Entity_Type", schema = "INTERNAL")
public class WorkshopEntityType extends Trackable {
	
	@Column(nullable = false, unique = true, updatable = false)
	@NotBlank(groups = {PersistenceValidation.class, UpdateValidation.class, Default.class}, message = "{validation.notBlank}")
	private String name;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
	@JoinTable(name = "Authority_Permissions_to_Workshop_Entity_Types", schema = "INTERNAL",
		joinColumns = @JoinColumn(name = "workshop_entity_id", referencedColumnName = "id", nullable = false),
		inverseJoinColumns = @JoinColumn(name = "authority_permission_id", referencedColumnName = "id", nullable = false))
	private Set<@Valid AuthorityPermission> authorityPermissions;
}
