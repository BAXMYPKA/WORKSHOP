package workshop.internal.controllers.rest;

import org.springframework.beans.factory.annotation.Autowired;
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
import workshop.internal.controllers.WorkshopControllerAbstract;
import workshop.internal.entities.AuthorityPermission;
import workshop.internal.entities.WorkshopEntityType;
import workshop.internal.entities.hibernateValidation.PersistenceValidation;
import workshop.internal.entities.hibernateValidation.MergingValidation;
import workshop.internal.hateoasResources.AuthorityPermissionsResourceAssembler;
import workshop.internal.hateoasResources.WorkshopEntityTypesResourceAssembler;
import workshop.internal.services.AuthorityPermissionsService;
import workshop.internal.services.WorkshopEntityTypesService;

@RestController
@RequestMapping(path = "/internal/entity-types", produces = MediaTypes.HAL_JSON_UTF8_VALUE)
@ExposesResourceFor(WorkshopEntityType.class)
public class WorkshopEntityTypesController extends WorkshopControllerAbstract<WorkshopEntityType> {
	
	public static final String GET_ENTITY_TYPE_AUTHORITY_PERMISSIONS = "getEntityTypeAuthorityPermissions";
	
	@Autowired
	private AuthorityPermissionsService authorityPermissionsService;
	
	@Autowired
	private AuthorityPermissionsResourceAssembler authorityPermissionsResourceAssembler;
	
	/**
	 * @param workshopEntityTypesService           By this instance we set the concrete instance of WorkshopServiceAbstract
	 *                                             and through it set the concrete type of WorkshopEntity as {@link #getWorkshopEntityClass()}
	 *                                             to operate with.
	 * @param workshopEntityTypesResourceAssembler
	 */
	public WorkshopEntityTypesController(
		WorkshopEntityTypesService workshopEntityTypesService,
		WorkshopEntityTypesResourceAssembler workshopEntityTypesResourceAssembler) {
		super(workshopEntityTypesService, workshopEntityTypesResourceAssembler);
	}
	
	@GetMapping(path = "/{id}/authority-permissions")
	@PreAuthorize("hasPermission('AuthorityPermission', 'get')")
	public ResponseEntity<String> getEntityTypeAuthorityPermissions(
		@PathVariable("id") Long id,
		@RequestParam(value = "pageSize", required = false, defaultValue = "${page.size.default}") Integer pageSize,
		@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
		@RequestParam(name = "order-by", required = false, defaultValue = "${default.orderBy}") String orderBy,
		@RequestParam(name = "order", required = false, defaultValue = "${default.order}") String order) {
		
		Pageable pageable = getPageable(pageSize, pageNum, orderBy, order);
		Page<AuthorityPermission> permissionsByWorkshopEntityType =
			authorityPermissionsService.findAuthorityPermissionsByWorkshopEntityType(pageable, id);
		Resources<Resource<AuthorityPermission>> permissionsPagedResources = authorityPermissionsResourceAssembler
			.toPagedSubResources(permissionsByWorkshopEntityType, id, GET_ENTITY_TYPE_AUTHORITY_PERMISSIONS);
		String jsonPagedResources = getJsonServiceUtils().workshopEntityObjectsToJson(permissionsPagedResources);
		return ResponseEntity.ok(jsonPagedResources);
	}
	
	@PostMapping(path = "/{id}/authority-permissions",
				 consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	@PreAuthorize("hasPermission('AuthorityPermission', 'post')")
	public ResponseEntity<String> postEntityTypeAuthorityPermission(
		@PathVariable(name = "id") Long id,
		@Validated(PersistenceValidation.class) @RequestBody AuthorityPermission authorityPermission,
		BindingResult bindingResult) {
		
		super.validateBindingResult(bindingResult);
		WorkshopEntityType entityType = getWorkshopEntitiesService().findById(id);
		authorityPermissionsService.persistEntity(authorityPermission);
		entityType.addAuthorityPermission(authorityPermission);
		entityType = getWorkshopEntitiesService().mergeEntity(entityType);
		authorityPermission = authorityPermissionsService.findById(authorityPermission.getIdentifier());
		Resource<AuthorityPermission> authorityPermissionResource =
			authorityPermissionsResourceAssembler.toResource(authorityPermission);
		String jsonPermissionResource = getJsonServiceUtils().workshopEntityObjectsToJson(authorityPermissionResource);
		return ResponseEntity.status(HttpStatus.CREATED).body(jsonPermissionResource);
	}
	
	@PutMapping(path = "/{id}/authority-permissions",
				consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	@PreAuthorize("hasPermission('AuthorityPermission', 'put')")
	public ResponseEntity<String> putEntityTypeAuthorityPermission(
		@PathVariable(name = "id") Long id,
		@Validated(MergingValidation.class) @RequestBody AuthorityPermission authorityPermission,
		BindingResult bindingResult) {
		
		super.validateBindingResult(bindingResult);
		WorkshopEntityType entityType = getWorkshopEntitiesService().findById(id);
		entityType.addAuthorityPermission(authorityPermission);
		entityType = getWorkshopEntitiesService().mergeEntity(entityType);
		authorityPermission = authorityPermissionsService.findById(authorityPermission.getIdentifier());
		Resource<AuthorityPermission> authorityPermissionResource =
			authorityPermissionsResourceAssembler.toResource(authorityPermission);
		String jsonPermissionResource = getJsonServiceUtils().workshopEntityObjectsToJson(authorityPermissionResource);
		return ResponseEntity.status(HttpStatus.OK).body(jsonPermissionResource);
	}
	
	/**
	 * Just removes this EntityType from a given AuthorityPermission.
	 *
	 * @return An {@link AuthorityPermission} without this {@link WorkshopEntityType}
	 */
	@DeleteMapping(path = "/{id}/authority-permissions/{authorityPermissionId}")
	@PreAuthorize("hasPermission('AuthorityPermission', 'put') and hasPermission('WorkshopEntityType', 'put')")
	public ResponseEntity<String> deleteEntityTypeAuthorityPermission(
		@PathVariable(name = "id") Long id,
		@PathVariable(name = "authorityPermissionId") Long authorityPermissionId) {
		
		super.getWorkshopEntitiesService().verifyIdForNullZeroBelowZero(id, authorityPermissionId);
		WorkshopEntityType workshopEntityType = getWorkshopEntitiesService().findById(id);
		AuthorityPermission authorityPermission = authorityPermissionsService.findById(authorityPermissionId);
		workshopEntityType.removeAuthorityPermission(authorityPermission);
		getWorkshopEntitiesService().mergeEntity(workshopEntityType);
		authorityPermission = authorityPermissionsService.findById(authorityPermissionId);
		Resource<AuthorityPermission> authorityPermissionResource =
			authorityPermissionsResourceAssembler.toResource(authorityPermission);
		String jsonAuthorityResource = getJsonServiceUtils().workshopEntityObjectsToJson(authorityPermissionResource);
		return ResponseEntity.status(HttpStatus.OK).body(jsonAuthorityResource);
	}
	
}
