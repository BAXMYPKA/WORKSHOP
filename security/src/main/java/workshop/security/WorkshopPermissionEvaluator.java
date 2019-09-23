package workshop.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.ServletWebRequest;
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
	
	/**
	 * @param authentication     {@link Authentication} {@literal The current #authentication from the SecurityContext}
	 * @param targetDomainObject {@link String} One of the {@link WorkshopEntity#workshopEntitiesNames} WorkshopEntity
	 *                           simple class names.
	 * @param permission         {@link String} as 'read', 'write' etc. Will be converted to {@link PermissionType}
	 * @return True if the current {@link Authentication} {@link InternalAuthority#allAuthoritiesPermissions} contains
	 * {@link PermissionType} for such kind of 'targetType'.
	 */
	@Override
	public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
		return evaluateParameters(authentication, targetDomainObject, permission);
	}
	
	/**
	 * @param authentication {@link Authentication} {@literal The current #authentication from the SecurityContext}
	 * @param targetId       Nullable
	 * @param targetType     {@link String} One of the {@link WorkshopEntity#workshopEntitiesNames} WorkshopEntity simple
	 *                       class names.
	 * @param permission     {@link String} as 'read', 'write' etc. Will be converted to {@link PermissionType}
	 * @return True if the current {@link Authentication} {@link InternalAuthority#allAuthoritiesPermissions} contains
	 * {@link PermissionType} for such kind of 'targetType'.
	 */
	@Override
	public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
		return evaluateParameters(authentication, targetType, permission);
	}
	
	/**
	 * @param authentication     Cannot be null. It is inserted by SpringSecurity automatically, no need to specially
	 *                           pass it with SPeL expressions onto this method.
	 * @param targetDomainObject If it is the instance of {@link ServletWebRequest} it is enough to obtain full info
	 *                           with {@link this#evaluateServletWebRequest(Authentication, ServletWebRequest)}.
	 *                           In not, it may be String representation of WorkshopEntity as 'Task' or the object
	 *                           itself with mandatory overridden .toString() with the explicit name.
	 * @param permission         Non-null. String representation of PermissionType as 'post', 'get' etc.
	 * @return true of false
	 */
	private boolean evaluateParameters(Authentication authentication, Object targetDomainObject, Object permission) {
		//Those are cannot be null
		if (authentication == null || targetDomainObject == null) {
			return false;
		}
		//Final evaluation if ServletWebRequest is present
		if (targetDomainObject.getClass().isAssignableFrom(ServletWebRequest.class)) {
			return evaluateServletWebRequest(authentication, (ServletWebRequest) targetDomainObject);
		}
		//Cannot be null, should contain PermissionType as 'get', 'post' etc.
		if (permission == null) {
			return false;
		}
		String workshopEntityName;
		String permissionTypeName;
		if (targetDomainObject.getClass().isAssignableFrom(String.class)) {
			workshopEntityName = (String) targetDomainObject;
		} else {
			workshopEntityName = targetDomainObject.toString();
		}
		if (permission.getClass().isAssignableFrom(String.class)) {
			permissionTypeName = (String) permission;
		} else {
			permissionTypeName = permission.toString();
		}
		log.debug("Permissions of Authentication={} evaluating for workshopEntity={} with permission={}",
			authentication.getName(), workshopEntityName, permissionTypeName);
		try {
			//Check the given PermissionType
			PermissionType permissionType = PermissionType.valueOf(permissionTypeName.toUpperCase());
			//Check the given WorkshopEntityTargetType
			if (!WorkshopEntity.workshopEntitiesNames.contains(workshopEntityName)) {
				throw new IllegalArgumentException("TargetType=" + workshopEntityName + " of the given WorkshopEntity name cannot" +
					" be evaluated through all the possible names of WorkshopEntity.workshopEntitiesNames()!");
			}
			//Can throw IllegalArgumentException if the given Authentication doesnt contain proper InternalAuthority
			return finalAuthenticationEvaluation(authentication, permissionType, workshopEntityName);
			
		} catch (IllegalArgumentException e) { //PermissionType or WorkshopEntity name is wrong
			log.error(e.getMessage(), e);
			return false;
		} catch (NullPointerException npe) { //Authentication doesnt have authorities
			log.info(npe.getMessage(), npe);
			return false;
		}
	}
	
	private boolean evaluateServletWebRequest(Authentication authentication, ServletWebRequest webRequest) {
		try {
			PermissionType permissionType = PermissionType.valueOf(webRequest.getHttpMethod().name().toUpperCase());
			
			String contextPath = webRequest.getRequest().getPathInfo();
			int startIndex = contextPath.indexOf("/internal/") + 10;
			int endIndex = contextPath.indexOf("/", startIndex) != -1 ? contextPath.indexOf("/", startIndex) :
				contextPath.length();
			//Capitalize first letter + the rest part of the WorkshopEntityName - last letter "s"
			String workshopEntityName = contextPath.substring(startIndex, startIndex + 1).toUpperCase() +
				contextPath.substring(startIndex + 1, endIndex - 1);
			
			if (workshopEntityName.startsWith("Internal")) {
				workshopEntityName = "InternalAuthority";
			} else if (workshopEntityName.startsWith("External")) {
				workshopEntityName = "ExternalAuthority";
			}
			
			String finalWorkshopEntityName = workshopEntityName;
			workshopEntityName = WorkshopEntity.workshopEntitiesNames.stream()
				.filter(ename -> ename.contains(finalWorkshopEntityName))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException(
					"TargetType=" + finalWorkshopEntityName + " of the given WorkshopEntity name cannot be evaluated" +
						" through all the possible names of WorkshopEntity.workshopEntitiesNames()!"));
			
			//Can throw IllegalArgumentException if the given Authentication doesnt contain proper InternalAuthority
			return finalAuthenticationEvaluation(authentication, permissionType, finalWorkshopEntityName);
			
		} catch (IllegalArgumentException e) { //PermissionType or WorkshopEntity name is wrong
			log.error(e.getMessage(), e);
			return false;
		} catch (NullPointerException npe) { //Authentication doesnt have authorities
			log.info(npe.getMessage(), npe);
			return false;
		}
	}
	
	private boolean finalAuthenticationEvaluation(
		Authentication authentication, PermissionType permissionType, String workshopEntityName)
		throws IllegalArgumentException {
		//Can throw IllegalArgumentException if the given Authentication doesnt contain proper InternalAuthority
		return authentication.getAuthorities().stream()
			.map(auth -> InternalAuthority.valueOf(auth.getAuthority()))
			.map(InternalAuthority.allAuthoritiesPermissions::get)
			.anyMatch(permissionTypesToTargetTypes ->
				permissionTypesToTargetTypes.get(permissionType).contains(workshopEntityName));
	}
}
