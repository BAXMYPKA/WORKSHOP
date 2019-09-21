package workshop.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

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
	
	//TODO: to implement full authorization process
	
	@Override
	public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
		log.debug("Permissions of Authentication={} evaluating for targetId={}, targetType={} with permission={}",
			authentication.getName(), targetId, targetType, permission);
		if (authentication.getAuthorities().stream()
			.anyMatch(auth -> auth.getAuthority().equalsIgnoreCase("ADMINISTRATOR"))) {
			return true;
		} else {
			return false;
		}
	}
}
