package workshop.controllers.internal.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import workshop.internal.entities.AuthorityPermission;
import workshop.internal.entities.InternalAuthority;
import workshop.internal.entities.WorkshopEntityType;
import workshop.internal.entities.hibernateValidation.PersistenceValidation;
import workshop.internal.entities.hibernateValidation.MergingValidation;
import workshop.internal.exceptions.EntityNotFoundException;
import workshop.internal.hateoasResources.AuthorityPermissionsResourceAssembler;
import workshop.internal.hateoasResources.InternalAuthoritiesResourceAssembler;
import workshop.internal.hateoasResources.WorkshopEntityTypesResourceAssembler;
import workshop.internal.services.AuthorityPermissionsService;
import workshop.internal.services.InternalAuthoritiesService;
import workshop.internal.services.WorkshopEntityTypesService;

import javax.validation.groups.Default;
import java.util.Arrays;
import java.util.HashSet;

@RestController
@RequestMapping(path = "/internal/authority-permissions", produces = MediaTypes.HAL_JSON_UTF8_VALUE)
@ExposesResourceFor(AuthorityPermission.class)
public class AuthorityPermissionsRestController extends WorkshopRestControllerAbstract<AuthorityPermission> {
	
	public static final String GET_AUTHORITY_PERMISSION_ENTITY_TYPES = "getWorkshopEntitiesTypes";
	
	@Autowired
	private InternalAuthoritiesService internalAuthoritiesService;
	
	@Autowired
	private WorkshopEntityTypesService workshopEntityTypesService;
	
	@Autowired
	private InternalAuthoritiesResourceAssembler internalAuthoritiesResourceAssembler;
	
	@Autowired
	private WorkshopEntityTypesResourceAssembler workshopEntityTypesResourceAssembler;
	
	/**
	 * @param authorityPermissionsService           By this instance we set the concrete instance of WorkshopServiceAbstract
	 *                                              and through it set the concrete type of WorkshopEntity as {@link #getWorkshopEntityClass()}
	 *                                              to operate with.
	 * @param authorityPermissionsResourceAssembler
	 */
	public AuthorityPermissionsRestController(
		AuthorityPermissionsService authorityPermissionsService,
		AuthorityPermissionsResourceAssembler authorityPermissionsResourceAssembler) {
		super(authorityPermissionsService, authorityPermissionsResourceAssembler);
	}
	
	@GetMapping(path = "/{id}/internal-authority")
	@PreAuthorize("hasPermission('InternalAuthority', 'get')")
	public ResponseEntity<String> getInternalAuthority(@PathVariable(name = "id") Long id) {
		
		InternalAuthority internalAuthority =
			internalAuthoritiesService.findInternalAuthorityByAuthorityPermission(id);
		Resource<InternalAuthority> internalAuthorityResource =
			internalAuthoritiesResourceAssembler.toResource(internalAuthority);
		String jsonInternalAuthorityResource =
			getJsonServiceUtils().workshopEntityObjectsToJson(internalAuthorityResource);
		return ResponseEntity.ok(jsonInternalAuthorityResource);
	}
	
	/**
	 * Receives a new InternalAuthority and reassign this existing AuthorityPermission to it.
	 *
	 * @param id                Existing AuthorityPermission to be reassigned to.
	 * @param internalAuthority New InternalAuthority.
	 * @return JSON InternalAuthority as a HATEOAS Resource with this AuthorityPermission assigned.
	 */
	@PostMapping(path = "/{id}/internal-authority",
				 consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	@PreAuthorize("hasPermission('InternalAuthority', 'post')")
	public ResponseEntity<String> postInternalAuthority(@PathVariable(name = "id") Long id,
		@Validated(PersistenceValidation.class) @RequestBody InternalAuthority internalAuthority,
		BindingResult bindingResult) {
		
		super.validateBindingResult(bindingResult);
		internalAuthority = internalAuthoritiesService.persistEntity(internalAuthority);
		
		AuthorityPermission authorityPermission = getWorkshopEntitiesService().findById(id);
		authorityPermission.setInternalAuthority(internalAuthority);
		getWorkshopEntitiesService().mergeEntity(authorityPermission);
		
		internalAuthority = internalAuthoritiesService.findById(internalAuthority.getIdentifier());
		Resource<InternalAuthority> internalAuthorityResource =
			internalAuthoritiesResourceAssembler.toResource(internalAuthority);
		String jsonInternalAuthorityResource =
			getJsonServiceUtils().workshopEntityObjectsToJson(internalAuthorityResource);
		return ResponseEntity.status(HttpStatus.CREATED).body(jsonInternalAuthorityResource);
	}
	
	/**
	 * Receives an existing InternalAuthority and reassign this existing AuthorityPermission to it.
	 *
	 * @param id                An existing AuthorityPermission to be reassigned to.
	 * @param internalAuthority An existing InternalAuthority.
	 * @return JSON InternalAuthority as a HATEOAS Resource with this AuthorityPermission assigned.
	 */
	@PutMapping(path = "/{id}/internal-authority",
				consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	@PreAuthorize("hasPermission('InternalAuthority', 'put')")
	public ResponseEntity<String> putInternalAuthority(@PathVariable(name = "id") Long id,
		@Validated(MergingValidation.class) @RequestBody InternalAuthority internalAuthority,
		BindingResult bindingResult) {
		
		super.validateBindingResult(bindingResult);
		AuthorityPermission authorityPermission = getWorkshopEntitiesService().findById(id);
		authorityPermission.setInternalAuthority(internalAuthority);
		getWorkshopEntitiesService().mergeEntity(authorityPermission);
		
		internalAuthority = internalAuthoritiesService.findById(internalAuthority.getIdentifier());
		Resource<InternalAuthority> internalAuthorityResource =
			internalAuthoritiesResourceAssembler.toResource(internalAuthority);
		String jsonInternalAuthorityResource = getJsonServiceUtils().workshopEntityObjectsToJson(internalAuthorityResource);
		return ResponseEntity.status(HttpStatus.OK).body(jsonInternalAuthorityResource);
	}
	
	/**
	 * Deletes this AuthorityPermission (as it cant exist without InternalAuthority) and removes it from the given
	 * AuthorityPermission.
	 *
	 * @param id                  The id of the AuthorityPermission to be deleted.
	 * @param internalAuthorityId InternalAuthorityId this AuthorityPermission has to be removed from.
	 * @return The InternalAuthority without this AuthorityPermission.
	 */
	@DeleteMapping(path = "/{id}/internal-authority/{internalAuthorityId}")
	@PreAuthorize("hasPermission('AuthorityPermission', 'delete') and hasPermission('InternalAuthority', 'put')")
	public ResponseEntity<String> deleteInternalAuthority(@PathVariable(name = "id") Long id,
		@PathVariable(name = "internalAuthorityId") Long internalAuthorityId) {
		
		getWorkshopEntitiesService().verifyIdForNullZeroBelowZero(id, internalAuthorityId);
		InternalAuthority internalAuthority = internalAuthoritiesService.findById(internalAuthorityId);
		
		String errorLocalizedMessage = getMessageSource().getMessage("httpStatus.notFound(2)",
			new Object[]{"AuthorityPermission.ID=" + id, "InternalAuthority.ID=" + internalAuthorityId},
			LocaleContextHolder.getLocale());
		
		if (internalAuthority.getAuthorityPermissions() == null) {
			return getResponseEntityWithErrorMessage(HttpStatus.NOT_FOUND, errorLocalizedMessage);
		}
		AuthorityPermission authorityPermission = internalAuthority.getAuthorityPermissions().stream()
			.filter(permission -> permission.getIdentifier().equals(id))
			.findFirst()
			.orElseThrow(() ->
				new EntityNotFoundException("Nothing found!", HttpStatus.NOT_FOUND, errorLocalizedMessage));
		
		getWorkshopEntitiesService().removeEntity(authorityPermission);
		
		internalAuthority = internalAuthoritiesService.findById(internalAuthorityId);
		Resource<InternalAuthority> internalAuthorityResource =
			internalAuthoritiesResourceAssembler.toResource(internalAuthority);
		String jsonInternalAuthorityResource =
			getJsonServiceUtils().workshopEntityObjectsToJson(internalAuthorityResource);
		
		return ResponseEntity.status(HttpStatus.NO_CONTENT).body(jsonInternalAuthorityResource);
	}
	
	//TODO: methods for ExternalAuthorities
	
	/**
	 * @param id       AuthorityPermission.ID
	 * @param pageSize min=0 ({@link #getDEFAULT_PAGE_SIZE()} will be returned), max={@link #getMAX_PAGE_SIZE()}
	 * @param pageNum  min=1
	 * @param orderBy  Property name this WorkshopEntityTypes has be ordered by.
	 * @param order    ASC or DESC
	 * @return List of {@literal Resources<Resource<WorkshopEntityType>>} of this {@link AuthorityPermission}
	 */
	@GetMapping(path = "/{id}/entities-types")
	@PreAuthorize("hasPermission('AuthorityPermission', 'get') and hasPermission('WorkshopEntityType', 'get')")
	public ResponseEntity<String> getWorkshopEntitiesTypes(
		@PathVariable(name = "id") Long id,
		@RequestParam(value = "pageSize", required = false, defaultValue = "${page.size.default}") Integer pageSize,
		@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
		@RequestParam(name = "order-by", required = false, defaultValue = "${default.orderBy}") String orderBy,
		@RequestParam(name = "order", required = false, defaultValue = "${default.order}") String order) {
		
		Pageable pageable = super.getPageable(pageSize, pageNum, orderBy, order);
		Page<WorkshopEntityType> permissionEntitiesTypesPage =
			workshopEntityTypesService.findEntityTypesByAuthorityPermission(pageable, id);
		Resources<Resource<WorkshopEntityType>> authorityPermissionEntityTypesPagedResources =
			workshopEntityTypesResourceAssembler.toPagedSubResources(
				permissionEntitiesTypesPage, id, GET_AUTHORITY_PERMISSION_ENTITY_TYPES);
		String jsonEntityTypesPagedResources =
			getJsonServiceUtils().workshopEntityObjectsToJson(authorityPermissionEntityTypesPagedResources);
		return ResponseEntity.ok(jsonEntityTypesPagedResources);
	}
	
	/**
	 * Receives a new {@link WorkshopEntityType}, persist it and adds to this {@link AuthorityPermission}
	 *
	 * @return HttpStatus.CREATED and the persisted WorkshopEntityType instance.
	 */
	@PostMapping(path = "/{id}/entities-types", consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	@PreAuthorize("hasPermission('WorkshopEntityType', 'post') and hasPermission('AuthorityPermission', 'put')")
	public ResponseEntity<String> postAuthorityPermissionWorkshopEntityType(
		@PathVariable(name = "id") Long id,
		@Validated({PersistenceValidation.class}) WorkshopEntityType workshopEntityType, BindingResult bindingResult) {
		
		super.validateBindingResult(bindingResult);
		AuthorityPermission authorityPermission = getWorkshopEntitiesService().findById(id);
		workshopEntityType = workshopEntityTypesService.persistEntity(workshopEntityType);
		if (workshopEntityType.getAuthorityPermissions() == null) {
			workshopEntityType.setAuthorityPermissions(new HashSet<>(Arrays.asList(authorityPermission)));
		} else {
			workshopEntityType.getAuthorityPermissions().add(authorityPermission);
		}
		workshopEntityType = workshopEntityTypesService.mergeEntity(workshopEntityType);
		Resource<WorkshopEntityType> workshopEntityTypeResource =
			workshopEntityTypesResourceAssembler.toResource(workshopEntityType);
		String jsonEntityTypeResource = getJsonServiceUtils().workshopEntityObjectsToJson(workshopEntityTypeResource);
		return ResponseEntity.status(HttpStatus.CREATED).body(jsonEntityTypeResource);
	}
	
	/**
	 * Receives an existing {@link WorkshopEntityType}, adds to this {@link AuthorityPermission} and update both ones.
	 *
	 * @return HttpStatus.ACCEPTED and the updated WorkshopEntityType instance.
	 */
	@PutMapping(path = "/{id}/entities-types", consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	@PreAuthorize("hasPermission('WorkshopEntityType', 'put') and hasPermission('AuthorityPermission', 'put')")
	public ResponseEntity<String> putAuthorityPermissionWorkshopEntityType(
		@PathVariable(name = "id") Long id,
		@Validated({MergingValidation.class, Default.class}) WorkshopEntityType workshopEntityType,
		BindingResult bindingResult) {
		
		super.validateBindingResult(bindingResult);
		AuthorityPermission authorityPermission = getWorkshopEntitiesService().findById(id);
		workshopEntityType = workshopEntityTypesService.mergeEntity(workshopEntityType);
		if (workshopEntityType.getAuthorityPermissions() == null) {
			workshopEntityType.setAuthorityPermissions(new HashSet<>(Arrays.asList(authorityPermission)));
		} else {
			workshopEntityType.getAuthorityPermissions().add(authorityPermission);
		}
		workshopEntityType = workshopEntityTypesService.mergeEntity(workshopEntityType);
		Resource<WorkshopEntityType> workshopEntityTypeResource =
			workshopEntityTypesResourceAssembler.toResource(workshopEntityType);
		String jsonEntityTypeResource = getJsonServiceUtils().workshopEntityObjectsToJson(workshopEntityTypeResource);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(jsonEntityTypeResource);
	}
	
	/**
	 * Just removes a given {@link WorkshopEntityType} from a given {@link AuthorityPermission}
	 *
	 * @param id                   AuthorityPermission.id
	 * @param workshopEntityTypeId WorkshopEntityType.id
	 * @return The HATEOAS Resource WorkshopEntityType without this AuthorityPermission.
	 */
	@DeleteMapping(path = "/{id}/entities-types/{workshopEntityTypeId}")
	@PreAuthorize("hasPermission('WorkshopEntityType', 'put') and hasPermission('AuthorityPermission', 'put')")
	public ResponseEntity<String> deleteTask(
		@PathVariable(name = "id") Long id,
		@PathVariable(name = "workshopEntityTypeId") Long workshopEntityTypeId) {
		
		WorkshopEntityType entityType = workshopEntityTypesService.findById(workshopEntityTypeId);
		if (entityType.getAuthorityPermissions() != null &&
			entityType.getAuthorityPermissions().removeIf(permission -> permission.getIdentifier().equals(id))) {
			entityType = workshopEntityTypesService.mergeEntity(entityType);
			Resource<WorkshopEntityType> workshopEntityTypeResource =
				workshopEntityTypesResourceAssembler.toResource(entityType);
			String jsonEntityTypeResource =
				getJsonServiceUtils().workshopEntityObjectsToJson(workshopEntityTypeResource);
			return ResponseEntity.ok(jsonEntityTypeResource);
		} else {
			String notFoundMessage = getMessageSource().getMessage("httpStatus.notFound(2)",
				new Object[]{"AuthorityPermission.ID" + id, "WorkshopEntityType.ID=" + workshopEntityTypeId},
				LocaleContextHolder.getLocale());
			return getResponseEntityWithErrorMessage(HttpStatus.NOT_FOUND, notFoundMessage);
		}
	}
}
