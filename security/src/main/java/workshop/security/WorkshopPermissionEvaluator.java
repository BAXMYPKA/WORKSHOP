package workshop.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import workshop.internal.entities.WorkshopEntity;

import java.io.Serializable;

/**
 * {@literal
 * Class for analyzing methodSecurity permissions by, e.g.,
 *
 * @PreAuthorize("hasPermission(#authentication, 'Task', 'read')")
 * where:
 * #authentication - is the current Authentication from the SecurityContext,
 * 'Task' - literal name of a domain object or target type which the current Authentication is going to have an access,
 * 'read' - literal type of access for the domain object ('read', 'write', 'full')
 * Throws {@link org.springframework.security.access.AccessDeniedException} by SpringSecurity and intercepted by the
 * {@link workshop.internal.controllers.ExceptionHandlerController} then.
 * }
 */
@Slf4j
@Component
public class WorkshopPermissionEvaluator implements PermissionEvaluator {
	
	@Override
	public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
		log.debug("Permissions of Authentication={} evaluating for DomainObject={} with action={}",
			authentication.getName(), targetDomainObject, permission);
		if (authentication.getAuthorities().stream()
			.anyMatch(auth -> auth.getAuthority().equalsIgnoreCase("ADMINISTRATOR"))) {
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
		log.debug("Permissions of Authentication={} evaluating for targetId={}, targetType={} with permission={}",
			authentication.getName(), targetId, targetType, permission);
		
		PermissionType permissionType1 = InternalAuthority.ADMIN_READ.permissionType;
		
		try {
			//Check the given PermissionType
			PermissionType permissionType = PermissionType.valueOf(permission.toString().toUpperCase());
			//Check the given WorkshopEntityTargetType
			if (!WorkshopEntity.workshopEntitiesNames.contains(targetType)) {
				throw new IllegalArgumentException("TargetType=" + targetType + " of the given WorkshopEntity name cannot" +
					" be evaluated through all the possible names of WorkshopEntity.workshopEntitiesNames()!");
			}
			//Can throw IllegalArgumentException if the given Authentication doesnt contain proper InternalAuthority
			return authentication.getAuthorities().stream()
				.map(auth -> InternalAuthority.valueOf(auth.getAuthority()))
				.map(InternalAuthority.allAuthoritiesPermissions::get)
				.anyMatch(permissionTypesToTargetTypes ->
					permissionTypesToTargetTypes.get(permissionType).contains(targetType));
			
		} catch (IllegalArgumentException e) {
			log.error(e.getMessage(), e);
			return false;
		} catch (NullPointerException npe) {
			log.info(npe.getMessage(), npe);
			return false;
		}
	}
	
	//TODO: to implement full authorization process
	
	public boolean hasWorkshopPermission(
		Authentication authentication,
		String workshopEntityClass,
		Class<PermissionType> permissionTypeClass) {
		
		log.debug("Permissions of Authentication={} evaluating for DomainObject={} with action={}",
			authentication.getName(), workshopEntityClass, permissionTypeClass);
		if (authentication.getAuthorities().stream()
			.anyMatch(auth -> auth.getAuthority().equalsIgnoreCase("ADMINISTRATOR"))) {
			return true;
		} else {
			return false;
		}
	}
}
