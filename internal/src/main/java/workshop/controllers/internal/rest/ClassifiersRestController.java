package workshop.controllers.internal.rest;

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
import workshop.internal.entities.*;
import workshop.internal.entities.hibernateValidation.PersistenceValidation;
import workshop.internal.entities.hibernateValidation.MergingValidation;
import workshop.internal.hateoasResources.ClassifiersGroupsResourceAssembler;
import workshop.internal.hateoasResources.ClassifiersResourceAssembler;
import workshop.internal.hateoasResources.TasksResourceAssembler;
import workshop.internal.services.ClassifiersGroupsService;
import workshop.internal.services.ClassifiersService;
import workshop.internal.services.TasksService;

@RestController
@RequestMapping(path = "/internal/classifiers", produces = MediaTypes.HAL_JSON_UTF8_VALUE)
@ExposesResourceFor(Classifier.class)
public class ClassifiersRestController extends WorkshopRestControllerAbstract<Classifier> {
	
	public static final String GET_TASKS_METHOD_NAME = "getTasks";
	
	@Autowired
	private ClassifiersGroupsService classifiersGroupsService;
	
	@Autowired
	private TasksService tasksService;
	
	@Autowired
	private TasksResourceAssembler tasksResourceAssembler;
	
	@Autowired
	private ClassifiersGroupsResourceAssembler classifiersGroupsResourceAssembler;
	
	/**
	 * @param classifiersService           By this instance we set the concrete instance of WorkshopServiceAbstract
	 *                                     and through it set the concrete type of WorkshopEntity as {@link #getWorkshopEntityClass()}
	 *                                     to operate with.
	 * @param classifiersResourceAssembler
	 */
	public ClassifiersRestController(
		ClassifiersService classifiersService, ClassifiersResourceAssembler classifiersResourceAssembler) {
		super(classifiersService, classifiersResourceAssembler);
	}
	
	@GetMapping(path = "/{id}/classifiers-group")
	@PreAuthorize("hasPermission('ClassifiersGroup', 'get')")
	public ResponseEntity<String> getClassifierGroup(@PathVariable("id") Long id) {
		ClassifiersGroup classifiersGroupByClassifiers = classifiersGroupsService.findClassifiersGroupByClassifier(id);
		Resource<ClassifiersGroup> classifiersGroupResource =
			classifiersGroupsResourceAssembler.toResource(classifiersGroupByClassifiers);
		String jsonClassifierGroupResource = getJsonServiceUtils().workshopEntityObjectsToJson(classifiersGroupResource);
		return ResponseEntity.ok(jsonClassifierGroupResource);
	}
	
	@PostMapping(path = "/{id}/classifiers-group",
				 consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	@PreAuthorize("hasPermission('Classifier', 'put') and hasPermission('ClassifiersGroup', 'post')")
	public ResponseEntity<String> postClassifierGroup(
		@PathVariable(name = "id") Long id,
		@Validated(PersistenceValidation.class) @RequestBody ClassifiersGroup classifiersGroup,
		BindingResult bindingResult) {
		
		super.validateBindingResult(bindingResult);
		
		Classifier classifier = getWorkshopEntitiesService().findById(id);
		classifiersGroupsService.persistEntity(classifiersGroup);
		((ClassifiersService)getWorkshopEntitiesService()).setClassifierGroup(
			classifier.getIdentifier(),	classifiersGroup.getIdentifier());
		classifiersGroup = classifiersGroupsService.findById(classifiersGroup.getIdentifier());
		
		Resource<ClassifiersGroup> classifiersGroupResource =
			classifiersGroupsResourceAssembler.toResource(classifiersGroup);
		String jsonClassifierTypeResource = getJsonServiceUtils().workshopEntityObjectsToJson(classifiersGroupResource);
		return ResponseEntity.status(HttpStatus.CREATED).body(jsonClassifierTypeResource);
	}
	
	@PutMapping(path = "/{id}/classifiers-group",
				consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	@PreAuthorize("hasPermission('ClassifiersGroup', 'put') and hasPermission('Classifier', 'put')")
	public ResponseEntity<String> putClassifierGroup(
		@PathVariable(name = "id") Long id,
		@Validated(PersistenceValidation.class) @RequestBody ClassifiersGroup classifiersGroup,
		BindingResult bindingResult) {
		
		super.validateBindingResult(bindingResult);
		classifiersGroup = classifiersGroupsService.mergeEntity(classifiersGroup);
		Resource<ClassifiersGroup> classifiersGroupResource = classifiersGroupsResourceAssembler.toResource(classifiersGroup);
		String jsonClassifierGroupResource = getJsonServiceUtils().workshopEntityObjectsToJson(classifiersGroupResource);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(jsonClassifierGroupResource);
	}
	
	/**
	 * Just deletes the {@link ClassifiersGroup} from this {@link Classifier}
	 *
	 * @param id         {@link Classifier#getIdentifier()}
	 * @param classifiersGroupId Self-description.
	 * @return HATEOAS JSON Resource with the given {@link ClassifiersGroup} by id.
	 */
	@DeleteMapping(path = "/{id}/classifiers-group/{classifiersGroupId}")
	@PreAuthorize("hasPermission('ClassifiersGrou', 'put') and hasPermission('Classifier', 'put')")
	public ResponseEntity<String> deleteClassifierClassifierGroup(
		@PathVariable(name = "id") Long id,
		@PathVariable(name = "classifiersGroupId") Long classifiersGroupId) {
		
		Classifier classifier = getWorkshopEntitiesService().findById(id);
		classifier.setClassifiersGroup(null);
		//TODO: to test if the above will have the effect
		getWorkshopEntitiesService().mergeEntity(classifier);
		
		ClassifiersGroup classifiersGroup = classifiersGroupsService.findById(classifiersGroupId);
		Resource<ClassifiersGroup> classifiersGroupResource = classifiersGroupsResourceAssembler.toResource(classifiersGroup);
		String jsonClassifierGroupResource = getJsonServiceUtils().workshopEntityObjectsToJson(classifiersGroupResource);
		return ResponseEntity.status(HttpStatus.OK).body(jsonClassifierGroupResource);
	}
	
	@GetMapping(path = "/{id}/tasks")
	@PreAuthorize("hasPermission(#authentication, 'Task', 'get')")
	public ResponseEntity<String> getTasks(
		@PathVariable(name = "id") Long id,
		@RequestParam(value = "pageSize", required = false, defaultValue = "${page.size.default}") Integer pageSize,
		@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
		@RequestParam(name = "order-by", required = false, defaultValue = "${default.orderBy}") String orderBy,
		@RequestParam(name = "order", required = false, defaultValue = "${default.order}") String order) {
		
		Pageable pageable = super.getPageable(pageSize, pageNum, orderBy, order);
		Page<Task> tasksAppointedToEmployeePage = tasksService.findTasksByClassifier(pageable, id);
		Resources<Resource<Task>> classifierTasksPagedResources =
			tasksResourceAssembler.toPagedSubResources(tasksAppointedToEmployeePage, id, GET_TASKS_METHOD_NAME);
		String jsonClassifierTasksPagedResources =
			getJsonServiceUtils().workshopEntityObjectsToJson(classifierTasksPagedResources);
		return ResponseEntity.ok(jsonClassifierTasksPagedResources);
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
		@Validated(MergingValidation.class) @RequestBody Task task,
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
