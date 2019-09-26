package workshop.internal.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.http.HttpStatus;
import workshop.internal.entities.hibernateValidation.PersistenceValidation;
import workshop.internal.entities.hibernateValidation.MergingValidation;
import workshop.internal.entities.utils.PermissionType;
import workshop.internal.exceptions.IllegalArgumentsException;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.groups.Default;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

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
@ToString(callSuper = false, onlyExplicitlyIncluded = true)
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "Workshop_Entity_Type", schema = "INTERNAL")
public class WorkshopEntityType extends Trackable {
	
	@Transient
	@Getter(AccessLevel.PRIVATE)
	private static final long serialVersionUID = WorkshopEntity.serialVersionUID;
	
	@Column(nullable = false, unique = true, updatable = false)
	@NotBlank(groups = {PersistenceValidation.class, MergingValidation.class, Default.class}, message = "{validation.notBlank}")
	@EqualsAndHashCode.Include
	@ToString.Include
	private String name;
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
	@ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
	@JoinTable(name = "Authority_Permissions_to_Workshop_Entity_Types", schema = "INTERNAL",
		joinColumns = @JoinColumn(name = "workshop_entity_type_id", referencedColumnName = "id", nullable = false),
		inverseJoinColumns = @JoinColumn(name = "authority_permission_id", referencedColumnName = "id", nullable = false))
	private Set<@Valid AuthorityPermission> authorityPermissions;
	
	public WorkshopEntityType(@NotBlank(groups = {PersistenceValidation.class, MergingValidation.class, Default.class},
		message = "{validation.notBlank}") String name) {
		this.name = name;
	}
	
	/**
	 * The {@link #name} has to be equal one of the {@link WorkshopEntity} from {@link WorkshopEntity#workshopEntitiesNames}
	 *
	 * @throws IllegalArgumentsException With the localized message and HttpStatus.NOT_ACCEPTABLE as a RuntimeException
	 *                                   to break a current Transaction if the given name not equal one of the WorkshopEntity name.
	 */
	@PrePersist
	public void nameEqualityCheck() throws IllegalArgumentsException {
		if (!WorkshopEntity.workshopEntitiesNames.contains(name)) {
			throw new IllegalArgumentsException("The given name has to be equal one of the WorkshopEntity names!",
				"httpStatus.notAcceptable.workshopEntityType", HttpStatus.NOT_ACCEPTABLE);
		}
	}
	
	/**
	 * This method strictly relies on being under {@link org.springframework.transaction.annotation.Transactional}!
	 */
	public void addAuthorityPermission(AuthorityPermission... authorityPermissions) {
		if (authorityPermissions == null || Stream.of(authorityPermissions).anyMatch(Objects::isNull)) {
			throw new IllegalArgumentsException(
				"AuthorityPermissions cannot be null!", "httpStatus.notAcceptable.null", HttpStatus.NOT_ACCEPTABLE);
		}
		if (this.authorityPermissions == null) {
			this.authorityPermissions = new HashSet<>();
		}
		this.authorityPermissions.addAll(Arrays.asList(authorityPermissions));
	}
	/**
	 * This method strictly relies on being under {@link org.springframework.transaction.annotation.Transactional}!
	 */
	public void removeAuthorityPermission(AuthorityPermission... authorityPermissions) {
		if (authorityPermissions == null || Stream.of(authorityPermissions).anyMatch(Objects::isNull)) {
			throw new IllegalArgumentsException(
				"AuthorityPermissions cannot be null!", "httpStatus.notAcceptable.null", HttpStatus.NOT_ACCEPTABLE);
		}
		if (this.authorityPermissions == null) {
			return;
		}
		this.authorityPermissions.removeAll(Arrays.asList(authorityPermissions));
	}
	
}
