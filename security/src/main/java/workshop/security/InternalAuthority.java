package workshop.security;

import workshop.internal.entities.WorkshopEntity;

import java.util.*;

/**
 * Don't use this Enums in bean's or classes initializers or constructors! It has to be initialized lazily!
 * So its {@link #allAuthoritiesPermissions} will be initialized after the first access to any of InternalAuthority.
 * So {@link WorkshopEntity#workshopEntitiesNames} will be filled in up to that time.
 */
public enum InternalAuthority {
	
	//TODO: to grand access for all Authorities left.
	
	ADMIN_READ(PermissionType.READ, WorkshopEntity.workshopEntitiesNames.toArray(new String[0])),
	ADMIN_WRITE(PermissionType.READ, WorkshopEntity.workshopEntitiesNames.toArray(new String[0])),
	ADMIN_FULL(PermissionType.READ, WorkshopEntity.workshopEntitiesNames.toArray(new String[0])),
	
	WORKSHOP_READ(PermissionType.READ, new String[]{"Employee", "User", "Phone"}),
	WORKSHOP_WRITE(PermissionType.READ, new String[]{"Employee", "User", "Phone"}),
	WORKSHOP_FULL(PermissionType.READ, new String[]{"Employee", "User", "Phone"}),
	
	HR_READ(PermissionType.READ, new String[]{"Employee", "User", "Phone"}),
	HR_WRITE(PermissionType.WRITE, new String[]{"Employee", "User", "Phone"}),
	HR_FULL(PermissionType.FULL, new String[]{"Employee", "User", "Phone"}),
	
	EMPLOYEE(PermissionType.READ, new String[]{"Employee", "Phone"});
	
	public static final Map<InternalAuthority, Map<PermissionType, Set<String>>> allAuthoritiesPermissions = new HashMap<>();
	
	static {
		for (InternalAuthority authority : values()) {
			Map<PermissionType, Set<String>> internalAuthorityPermissions = new HashMap<>();
			internalAuthorityPermissions.put(authority.permissionType, authority.availableWorkshopEntities);
			allAuthoritiesPermissions.put(authority, internalAuthorityPermissions);
		}
	}
	
	public PermissionType permissionType;
	public Set<String> availableWorkshopEntities;
	
	InternalAuthority(PermissionType permissionType, String[] availableWorkshopEntities) {
		this.permissionType = permissionType;
		this.availableWorkshopEntities = new HashSet<>(Arrays.asList(availableWorkshopEntities));
	}
}
