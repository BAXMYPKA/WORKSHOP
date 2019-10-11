package workshop.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.ServletWebRequest;
import workshop.internal.entities.WorkshopEntity;
import workshop.internal.entities.WorkshopEntityType;
import workshop.internal.entities.utils.PermissionType;
import workshop.internal.services.InternalAuthoritiesService;
import workshop.internal.services.WorkshopEntityTypesService;

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
 * ExceptionHandlerController then.}
 */
@Slf4j
@Component
public class WorkshopPermissionEvaluator implements PermissionEvaluator {
	
	@Value("${internalPathName}")
	private String internalPathName;
	
	@Autowired
	private InternalAuthoritiesService internalAuthoritiesService;
	@Autowired
	private WorkshopEntityTypesService workshopEntityTypesService;
	
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
	 *                           with {@link #evaluateServletWebRequest(Authentication, ServletWebRequest)}.
	 *                           In not, it may be String representation of WorkshopEntity as 'Task' or the object
	 *                           itself with mandatory overridden .toString() with the explicit name.
	 * @param permission         Non-null. String representation of PermissionType as 'post', 'get' etc.
	 * @return true of false depending on results.
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
		String workshopEntityName = targetDomainObject.toString();
		String permissionTypeName = permission.toString();
		
		log.debug("Permissions of Authentication={} evaluating for workshopEntity={} with permission={}",
			authentication.getName(), workshopEntityName, permissionTypeName);
		try {
			//Check the given PermissionType, may throw IllegalArgumentException
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
	
	/**
	 * JavaDoc on private method as a reminder.
	 * {@link ServletWebRequest} object is the sufficient object to obtain all the information for securing a method.
	 * Its HttpMethod - is the analogue to {@link PermissionType}.
	 * Its part of the http path following the internal domain path is the potential name of the WorkshopEntity.
	 * And the {@link Authentication} is set automatically by Spring.
	 */
	private boolean evaluateServletWebRequest(Authentication authentication, ServletWebRequest webRequest) {
		try {
			PermissionType permissionType = PermissionType.valueOf(webRequest.getHttpMethod().name().toUpperCase());
			
			String contextPath = webRequest.getRequest().getPathInfo();
			//Start from the end of internal pathName
			int startIndex = contextPath.indexOf(internalPathName) + internalPathName.length();
			//End up to the next slash
			int endIndex = contextPath.indexOf("/", startIndex) != -1 ? contextPath.indexOf("/", startIndex) :
				contextPath.length();
			//Entity name is the between internalPathName and the next slash
			//Capitalize first letter + the rest part of the WorkshopEntityName - last letter "s"
			String workshopEntityName = contextPath.substring(startIndex, startIndex + 1).toUpperCase() +
				contextPath.substring(startIndex + 1, endIndex - 1);
			
			if (workshopEntityName.startsWith("Internal")) {
				workshopEntityName = "InternalAuthority";
			} else if (workshopEntityName.startsWith("External")) {
				workshopEntityName = "ExternalAuthority";
			} else if (workshopEntityName.startsWith("Authority-permission")) {
				workshopEntityName = "AuthorityPermission";
			} else if (workshopEntityName.startsWith("Entity-type")) {
				workshopEntityName = "WorkshopEntityType";
			}
			
			String finalWorkshopEntityName = workshopEntityName;
			workshopEntityName = WorkshopEntity.workshopEntitiesNames.stream()
				.filter(ename -> ename.contains(finalWorkshopEntityName))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException(
					"TargetType=" + finalWorkshopEntityName + " of the given WorkshopEntity name cannot be evaluated" +
						" through all the possible names of WorkshopEntity.workshopEntitiesNames()!"));
			
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
	
	//TODO: to also implement evaluating ExternalAuthorities
	
	private boolean finalAuthenticationEvaluation(
		Authentication authentication, PermissionType permissionType, String workshopEntityName)
		throws IllegalArgumentException {
		
		WorkshopEntityType workshopEntityType = workshopEntityTypesService.findByProperty("name", workshopEntityName).get(0);
/*
		for (GrantedAuthority auth : authentication.getAuthorities()) { //For the checkout test purposes
			workshop.internal.entities.InternalAuthority intAuth = internalAuthoritiesService.findByProperty("name", auth.getAuthority()).get(0);
		}
*/
		//Can throw IllegalArgumentException if the given Authentication doesnt contain proper InternalAuthority
		return authentication.getAuthorities().stream()
			.flatMap(intAuth -> internalAuthoritiesService.findByProperty("name", intAuth.getAuthority())
				.stream())
			.flatMap(internalAuthority -> internalAuthority.getAuthorityPermissions()
				.stream())
			.anyMatch(authorityPermission ->
				authorityPermission.getPermissionType().equals(permissionType) &&
					authorityPermission.getWorkshopEntityTypes().contains(workshopEntityType));
	}
}
