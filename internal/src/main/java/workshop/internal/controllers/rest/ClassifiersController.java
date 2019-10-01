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
import workshop.internal.entities.Classifier;
import workshop.internal.entities.Task;
import workshop.internal.entities.hibernateValidation.PersistenceValidation;
import workshop.internal.entities.hibernateValidation.MergingValidation;
import workshop.internal.hateoasResources.ClassifiersResourceAssembler;
import workshop.internal.hateoasResources.TasksResourceAssembler;
import workshop.internal.services.ClassifiersService;
import workshop.internal.services.TasksService;

@RestController
@RequestMapping(path = "/internal/classifiers", produces = MediaTypes.HAL_JSON_UTF8_VALUE)
@ExposesResourceFor(Classifier.class)
public class ClassifiersController extends WorkshopControllerAbstract<Classifier> {
	
	public static final String GET_TASKS_METHOD_NAME = "getTasks";
	
	@Autowired
	private TasksService tasksService;
	
	@Autowired
	private TasksResourceAssembler tasksResourceAssembler;
	
	/**
	 * @param classifiersService           By this instance we set the concrete instance of WorkshopServiceAbstract
	 *                                     and through it set the concrete type of WorkshopEntity as {@link #getWorkshopEntityClass()}
	 *                                     to operate with.
	 * @param classifiersResourceAssembler
	 */
	public ClassifiersController(
		ClassifiersService classifiersService, ClassifiersResourceAssembler classifiersResourceAssembler) {
		super(classifiersService, classifiersResourceAssembler);
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
