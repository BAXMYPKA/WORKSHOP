package workshop.security;

import workshop.internal.entities.utils.PermissionType;
import workshop.internal.entities.WorkshopEntity;

import java.util.*;

/**
 * Don't use this Enums in bean's or classes initializers or constructors! It has to be initialized lazily!
 * So its {@link #allAuthoritiesPermissions} will be initialized after the first access to any of InternalAuthority.
 * So {@link WorkshopEntity#workshopEntitiesNames} will be filled in up to that time.
 */
public enum InternalAuthority {
	
	/**
	 * Includes only {@link PermissionType#GET} type of permission.
	 */
	ADMIN_READ(
		PermissionType.GET, WorkshopEntity.workshopEntitiesNames.toArray(new String[0])),
	/**
	 * Also includes {@link PermissionType#GET}.
	 */
	ADMIN_WRITE(
		PermissionType.PUT, WorkshopEntity.workshopEntitiesNames.toArray(new String[0])),
	/**
	 * Also includes
	 * {@link PermissionType#GET}, {@link PermissionType#PUT}, {@link PermissionType#POST}, {@link PermissionType#DELETE}
	 */
	ADMIN_FULL(
		PermissionType.POST, WorkshopEntity.workshopEntitiesNames.toArray(new String[0])),
	/**
	 * Includes only {@link PermissionType#GET} type of permission.
	 */
	WORKSHOP_READ(
		PermissionType.GET, new String[]{"Employee", "Task", "Order", "Classifier", "User", "Phone", "ExternalAuthority"}),
	/**
	 * Also includes {@link PermissionType#GET}.
	 */
	WORKSHOP_WRITE(
		PermissionType.PUT, new String[]{"Task", "Order", "Classifier", "User", "Phone"}),
	/**
	 * Also includes
	 * {@link PermissionType#GET}, {@link PermissionType#PUT}, {@link PermissionType#POST}, {@link PermissionType#DELETE}
	 */
	WORKSHOP_FULL(
		PermissionType.POST, new String[]{"Task", "Order", "Classifier", "User", "Phone", "ExternalAuthority"}),
	/**
	 * Includes only {@link PermissionType#GET} type of permission.
	 */
	HR_READ(
		PermissionType.GET,
		new String[]{"Department", "Employee", "Position", "Phone", "Task", "Order", "User"}),
	/**
	 * Also includes {@link PermissionType#GET}.
	 */
	HR_WRITE(
		PermissionType.PUT,
		new String[]{"Employee", "Phone", "Task", "Order", "User", "ExternalAuthority"}),
	/**
	 * Also includes
	 * {@link PermissionType#GET}, {@link PermissionType#PUT}, {@link PermissionType#POST}, {@link PermissionType#DELETE}
	 */
	HR_FULL(
		PermissionType.POST,
		new String[]{"Department", "Employee", "Position", "InternalAuthority", "Phone", "Task", "Order", "Classifier", "User", "ExternalAuthority"}),
	/**
	 * Includes only {@link PermissionType#GET} type of permission.
	 */
	EMPLOYEE_READ(
		PermissionType.GET, new String[]{"Employee", "Phone"});
	
	public static final Map<InternalAuthority, Map<PermissionType, Set<String>>> allAuthoritiesPermissions = new HashMap<>();
	
	static {
		for (InternalAuthority authority : values()) {
			System.out.println();
			Map<PermissionType, Set<String>> authorityPermissions = new HashMap<>();
			
			authorityPermissions.put(authority.permissionType, authority.availableWorkshopEntities);
			
			if (authority.permissionType.equals(PermissionType.PUT)) {
				authorityPermissions.put(PermissionType.GET, authority.availableWorkshopEntities);
			} else if (authority.permissionType.equals(PermissionType.POST)) {
				authorityPermissions.put(PermissionType.GET, authority.availableWorkshopEntities);
				authorityPermissions.put(PermissionType.PUT, authority.availableWorkshopEntities);
				authorityPermissions.put(PermissionType.DELETE, authority.availableWorkshopEntities);
			}
			allAuthoritiesPermissions.put(authority, authorityPermissions);
		}
	}
	
	public PermissionType permissionType;
	public Set<String> availableWorkshopEntities;
	
	InternalAuthority(PermissionType permissionType, String[] availableWorkshopEntities) {
		this.permissionType = permissionType;
		this.availableWorkshopEntities = new HashSet<>(Arrays.asList(availableWorkshopEntities));
	}
	
	
}
