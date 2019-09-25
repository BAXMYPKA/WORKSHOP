package workshop.internal.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import workshop.internal.entities.AuthorityPermission;
import workshop.internal.entities.Classifier;
import workshop.internal.entities.InternalAuthority;
import workshop.internal.entities.Task;
import workshop.internal.entities.hibernateValidation.PersistenceValidation;
import workshop.internal.entities.hibernateValidation.UpdateValidation;
import workshop.internal.hateoasResources.AuthorityPermissionsResourceAssembler;
import workshop.internal.hateoasResources.InternalAuthoritiesResourceAssembler;
import workshop.internal.services.AuthorityPermissionsService;
import workshop.internal.services.InternalAuthoritiesService;

@RestController
@RequestMapping(path = "/internal/authority-permissions", produces = MediaTypes.HAL_JSON_UTF8_VALUE)
@ExposesResourceFor(AuthorityPermission.class)

public class AuthorityPermissionsController extends WorkshopControllerAbstract<AuthorityPermission> {
	
	public static final String GET_INTERNAL_AUTHORITY_METHOD_NAME = "getInternalAuthority";
	
	@Autowired
	private InternalAuthoritiesService internalAuthoritiesService;
	
	@Autowired
	private InternalAuthoritiesResourceAssembler internalAuthoritiesResourceAssembler;
	
	/**
	 * @param authorityPermissionsService           By this instance we set the concrete instance of WorkshopServiceAbstract
	 *                                              and through it set the concrete type of WorkshopEntity as {@link #getWorkshopEntityClass()}
	 *                                              to operate with.
	 * @param authorityPermissionsResourceAssembler
	 */
	public AuthorityPermissionsController(
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
	
	@PostMapping(path = "/{id}/tasks",
				 consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	@PreAuthorize("hasPermission('Task', 'post')")
	public ResponseEntity<String> postTask(@PathVariable(name = "id") Long id,
		@Validated(PersistenceValidation.class) @RequestBody Task task,
		BindingResult bindingResult) {
		
		super.validateBindingResult(bindingResult);
		tasksService.persistEntity(task);
		Classifier thisClassifier = getWorkshopEntitiesService().findById(id);
		task.addClassifier(thisClassifier);
		Task mergedTask = tasksService.mergeEntity(task);
		Resource<Task> taskResource = tasksResourceAssembler.toResource(mergedTask);
		String jsonTaskResource = getJsonServiceUtils().workshopEntityObjectsToJson(taskResource);
		return ResponseEntity.status(HttpStatus.CREATED).body(jsonTaskResource);
	}
	
	@PutMapping(path = "/{id}/tasks",
				consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	@PreAuthorize("hasPermission(#authentication, 'Task', 'put')")
	public ResponseEntity<String> putTask(@PathVariable(name = "id") Long id,
		@Validated(UpdateValidation.class) @RequestBody Task task,
		BindingResult bindingResult) {
		
		super.validateBindingResult(bindingResult);
		Task updatedTask = tasksService.mergeEntity(task);
		Classifier thisClassifier = getWorkshopEntitiesService().findById(id);
		updatedTask.addClassifier(thisClassifier);
		Task mergedTask = tasksService.mergeEntity(updatedTask);
		Resource<Task> taskResource = tasksResourceAssembler.toResource(mergedTask);
		String jsonTaskResource = getJsonServiceUtils().workshopEntityObjectsToJson(taskResource);
		return ResponseEntity.status(HttpStatus.OK).body(jsonTaskResource);
	}
	
	/**
	 * Just removes Task from a given Classifier
	 *
	 * @param id     Classifier id
	 * @param taskId Task id
	 * @return A Task without this Classifier.
	 */
	@DeleteMapping(path = "/{id}/tasks/{taskId}")
	@PreAuthorize("hasPermission(#authentication, 'Task', 'put')")
	public ResponseEntity<String> deleteTask(@PathVariable(name = "id") Long id,
		@PathVariable(name = "taskId") Long taskId) {
		
		Classifier classifier = getWorkshopEntitiesService().findById(id);
		Task task = tasksService.findById(taskId);
		task.getClassifiers().remove(classifier);
		Task mergedTask = tasksService.mergeEntity(task);
		Resource<Task> taskResource = tasksResourceAssembler.toResource(mergedTask);
		String jsonTaskResource = getJsonServiceUtils().workshopEntityObjectsToJson(taskResource);
		return ResponseEntity.status(HttpStatus.OK).body(jsonTaskResource);
	}
	
}
