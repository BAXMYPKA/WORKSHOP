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
import sun.reflect.generics.tree.ClassTypeSignature;
import workshop.internal.entities.*;
import workshop.internal.entities.hibernateValidation.PersistenceValidation;
import workshop.internal.entities.hibernateValidation.MergingValidation;
import workshop.internal.hateoasResources.ClassifierTypesResourceAssembler;
import workshop.internal.hateoasResources.ClassifiersResourceAssembler;
import workshop.internal.hateoasResources.TasksResourceAssembler;
import workshop.internal.services.ClassifierTypesService;
import workshop.internal.services.ClassifiersService;
import workshop.internal.services.TasksService;

@RestController
@RequestMapping(path = "/internal/classifiers", produces = MediaTypes.HAL_JSON_UTF8_VALUE)
@ExposesResourceFor(Classifier.class)
public class ClassifiersRestController extends WorkshopRestControllerAbstract<Classifier> {
	
	public static final String GET_TASKS_METHOD_NAME = "getTasks";
	
	@Autowired
	private ClassifierTypesService classifierTypesService;
	
	@Autowired
	private TasksService tasksService;
	
	@Autowired
	private TasksResourceAssembler tasksResourceAssembler;
	
	@Autowired
	private ClassifierTypesResourceAssembler classifierTypesResourceAssembler;
	
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
	
	@GetMapping(path = "/{id}/classifier-type")
	@PreAuthorize("hasPermission('ClassifierType', 'get')")
	public ResponseEntity<String> getClassifierType(@PathVariable("id") Long id) {
		ClassifierType classifierTypeByClassifier = classifierTypesService.findClassifierTypeByClassifier(id);
		Resource<ClassifierType> classifierTypeResource =
			classifierTypesResourceAssembler.toResource(classifierTypeByClassifier);
		String jsonClassifierTypeResource = getJsonServiceUtils().workshopEntityObjectsToJson(classifierTypeResource);
		return ResponseEntity.ok(jsonClassifierTypeResource);
	}
	
	@PostMapping(path = "/{id}/classifier-type",
				 consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	@PreAuthorize("hasPermission('Classifier', 'put') and hasPermission('ClassifierType', 'post')")
	public ResponseEntity<String> postClassifierType(
		@PathVariable(name = "id") Long id,
		@Validated(PersistenceValidation.class) @RequestBody ClassifierType classifierType,
		BindingResult bindingResult) {
		
		super.validateBindingResult(bindingResult);
		
		Classifier classifier = getWorkshopEntitiesService().findById(id);
		classifierTypesService.persistEntity(classifierType);
		((ClassifiersService)getWorkshopEntitiesService()).setClassifierType(
			classifier.getIdentifier(),	classifierType.getIdentifier());
		classifierType = classifierTypesService.findById(classifierType.getIdentifier());
		
		Resource<ClassifierType> classifierTypeResource =
			classifierTypesResourceAssembler.toResource(classifierType);
		String jsonClassirierTypeResource = getJsonServiceUtils().workshopEntityObjectsToJson(classifierTypeResource);
		return ResponseEntity.status(HttpStatus.CREATED).body(jsonClassirierTypeResource);
	}
	
	@PutMapping(path = "/{id}/classifier-type",
				consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	@PreAuthorize("hasPermission('ClassifierType', 'put') and hasPermission('Classifier', 'put')")
	public ResponseEntity<String> putClassifierType(
		@PathVariable(name = "id") Long id,
		@Validated(PersistenceValidation.class) @RequestBody ClassifierType classifierType,
		BindingResult bindingResult) {
		
		super.validateBindingResult(bindingResult);
		classifierType = classifierTypesService.mergeEntity(classifierType);
		Resource<ClassifierType> classifierTypeResource = classifierTypesResourceAssembler.toResource(classifierType);
		String jsonClassifierTypeResource = getJsonServiceUtils().workshopEntityObjectsToJson(classifierTypeResource);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(jsonClassifierTypeResource);
	}
	
	/**
	 * Just deletes the {@link ClassifierType} from this {@link Classifier}
	 *
	 * @param id         {@link Classifier#getIdentifier()}
	 * @param classifierTypeId Self-description.
	 * @return HATEOAS JSON Resource with the given {@link ClassifierType} by id.
	 */
	@DeleteMapping(path = "/{id}/classifier-type/{classifierTypeId}")
	@PreAuthorize("hasPermission('ClassifierType', 'put') and hasPermission('Classifier', 'put')")
	public ResponseEntity<String> deleteClassifierClassifierType(
		@PathVariable(name = "id") Long id,
		@PathVariable(name = "classifierTypeId") Long classifierTypeId) {
		
		Classifier classifier = getWorkshopEntitiesService().findById(id);
		classifier.setClassifierType(null);
		//TODO: to test if the above will have the effect
		getWorkshopEntitiesService().mergeEntity(classifier);
		
		ClassifierType classifierType = classifierTypesService.findById(classifierTypeId);
		Resource<ClassifierType> classifierTypeResource = classifierTypesResourceAssembler.toResource(classifierType);
		String jsonClassifierTypeResource = getJsonServiceUtils().workshopEntityObjectsToJson(classifierTypeResource);
		return ResponseEntity.status(HttpStatus.OK).body(jsonClassifierTypeResource);
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
