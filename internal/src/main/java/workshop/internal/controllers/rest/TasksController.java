package workshop.internal.controllers.rest;

import org.springframework.beans.factory.annotation.Autowired;
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
import workshop.internal.controllers.WorkshopControllerAbstract;
import workshop.internal.entities.Classifier;
import workshop.internal.entities.Employee;
import workshop.internal.entities.Order;
import workshop.internal.entities.Task;
import workshop.internal.entities.hibernateValidation.PersistenceValidation;
import workshop.internal.entities.hibernateValidation.MergingValidation;
import workshop.internal.exceptions.EntityNotFoundException;
import workshop.internal.hateoasResources.ClassifiersResourceAssembler;
import workshop.internal.hateoasResources.EmployeesResourceAssembler;
import workshop.internal.hateoasResources.OrdersResourceAssembler;
import workshop.internal.hateoasResources.TasksResourceAssembler;
import workshop.internal.services.ClassifiersService;
import workshop.internal.services.TasksService;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashSet;

@RequestMapping(path = "/internal/tasks", produces = MediaTypes.HAL_JSON_UTF8_VALUE)
@RestController
@ExposesResourceFor(Task.class)
public class TasksController extends WorkshopControllerAbstract<Task> {
	
	public static final String GET_TASK_CLASSIFIERS_METHOD_NAME = "taskClassifiers";
	@Autowired
	private EmployeesResourceAssembler employeesResourceAssembler;
	@Autowired
	private OrdersResourceAssembler ordersResourceAssembler;
	@Autowired
	private ClassifiersResourceAssembler classifiersResourceAssembler;
	@Autowired
	private ClassifiersService classifiersService;
	
	/**
	 * @param tasksService           By this instance we set the concrete instance of WorkshopServiceAbstract
	 *                               and through it set the concrete type of WorkshopEntity as {@link #getWorkshopEntityClass()}
	 *                               to operate with.
	 * @param tasksResourceAssembler
	 */
	public TasksController(TasksService tasksService, TasksResourceAssembler tasksResourceAssembler) {
		super(tasksService, tasksResourceAssembler);
	}
	
	@GetMapping(path = "/{id}/appointed_to")
	@PreAuthorize("hasPermission('Employee', 'get')")
	public ResponseEntity<String> taskEmployeeAppointedTo(@PathVariable("id") Long id) {
		
		Task taskById = getWorkshopEntitiesService().findById(id);
		if (taskById.getAppointedTo() != null) {
			Employee employeeAppointedTo = taskById.getAppointedTo();
			Resource<Employee> employeeResource = employeesResourceAssembler.toResource(employeeAppointedTo);
			String jsonEmployeeResource = getJsonServiceUtils().workshopEntityObjectsToJson(employeeResource);
			return ResponseEntity.ok(jsonEmployeeResource);
		} else {
			throw new EntityNotFoundException("No appointed Employee for Task.id=" + id, HttpStatus.NOT_FOUND,
				getMessageSource().getMessage(
					"httpStatus.notFound(2)",
					new Object[]{"Employee", getWorkshopEntityClassName() + " ID=" + id},
					LocaleContextHolder.getLocale()));
		}
	}
	
	/**
	 * @return HttpStatus.FORBIDDEN with an explanation about the fact that Employees must be saved by their
	 * individual links before.
	 */
	@PostMapping(path = "/{id}/appointed_to",
				 consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	@PreAuthorize("hasPermission('Employee', 'post') and hasPermission('Task', 'put')")
	public ResponseEntity<String> postEmployeeAppointedTo(@PathVariable(name = "id") Long id,
		@RequestBody(required = false) Employee employee,
		HttpServletRequest request) {
		return getResponseEntityWithErrorMessage(
			HttpStatus.FORBIDDEN,
			getMessageSource().getMessage(
				"httpStatus.forbidden.withDescription(2)",
				new Object[]{request.getMethod(), " Save the Employee with a dedicated link first!"},
				LocaleContextHolder.getLocale()));
	}
	
	@PutMapping(path = "/{id}/appointed_to",
				consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	@PreAuthorize("hasPermission('Employee', 'put') and hasPermission('Task', 'put')")
	public ResponseEntity<String> putEmployeeAppointedTo(@PathVariable(name = "id") Long id,
		@Validated(MergingValidation.class) @RequestBody Employee employee,
		BindingResult bindingResult) {
		super.validateBindingResult(bindingResult);
		Task task = getWorkshopEntitiesService().findById(id);
		task.setAppointedTo(employee);
		getWorkshopEntitiesService().mergeEntity(task);
		if ((employee.getAppointedTasks() != null)) {
			employee.getAppointedTasks().add(task);
		} else {
			employee.setAppointedTasks(new HashSet<>(Collections.singletonList(task)));
		}
		Resource<Task> taskResource = getWorkshopEntityResourceAssembler().toResource(task);
		String jsonTaskResource = getJsonServiceUtils().workshopEntityObjectsToJson(taskResource);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(jsonTaskResource);
	}
	
	/**
	 * Just removes this Task from being appointed to the given Employee
	 */
	@DeleteMapping(path = "/{id}/appointed_to/{employeeId}")
	@PreAuthorize("hasPermission('Task', 'put')")
	public ResponseEntity<String> deleteEmployeeAppointedTo(@PathVariable(name = "id") Long id,
		@PathVariable(name = "employeeId") Long employeeId) {
		Task task = getWorkshopEntitiesService().findById(id);
		if (task.getAppointedTo() != null && task.getAppointedTo().getIdentifier().equals(employeeId)) {
			
			Employee employeeFromTask = task.getAppointedTo();
			if (employeeFromTask.getAppointedTasks() != null) {
				employeeFromTask.getAppointedTasks().remove(task);
			}
			
			task.setAppointedTo(null);
			getWorkshopEntitiesService().mergeEntity(task);
			
			Resource<Employee> employeeResource = employeesResourceAssembler.toResource(employeeFromTask);
			String jsonEmployeeResource = getJsonServiceUtils().workshopEntityObjectsToJson(employeeResource);
			return ResponseEntity.status(HttpStatus.GONE).body(jsonEmployeeResource);
		} else {
			return getResponseEntityWithErrorMessage(HttpStatus.NOT_FOUND, getMessageSource().getMessage(
				"httpStatus.notFound(2)",
				new Object[]{"Employee.ID=" + employeeId, "Order.ID=" + id},
				LocaleContextHolder.getLocale()));
		}
	}
	
	@GetMapping(path = "/{id}/order")
	@PreAuthorize("hasPermission('Order', 'get')")
	public ResponseEntity<String> taskOrder(@PathVariable("id") Long id) {
		
		Task taskById = getWorkshopEntitiesService().findById(id);
		Order taskOrder = taskById.getOrder();
		Resource<Order> taskOrderResource = ordersResourceAssembler.toResource(taskOrder);
		String jsonEmployeeResource = getJsonServiceUtils().workshopEntityObjectsToJson(taskOrderResource);
		return ResponseEntity.ok(jsonEmployeeResource);
	}
	
	@RequestMapping(path = {"/{id}/order", "/{id}/order/{orderId}"},
					method = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE},
					consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	@PreAuthorize(
		"hasPermission('Task', 'put') or hasPermission('Order', 'put') " +
			"or hasPermission('Order', 'post') or hasPermission('Order', 'delete')")
	public ResponseEntity<String> forbiddenMethodsTaskOrder(@PathVariable(name = "id", required = false) Long id,
		@PathVariable(name = "orderId", required = false) Long orderId,
		@RequestBody(required = false) Order order,
		HttpServletRequest request) {
		return getResponseEntityWithErrorMessage(HttpStatus.FORBIDDEN, getMessageSource().getMessage(
			"httpStatus.forbidden.withDescription(2)",
			new Object[]{request.getMethod() + " method ", " Use '{orderId}/tasks' instead!"},
			LocaleContextHolder.getLocale()));
	}
	
	@GetMapping(path = "/{id}/classifiers")
	@PreAuthorize("hasPermission('Classifier', 'get')")
	public ResponseEntity<String> taskClassifiers(
		@PathVariable("id") Long id,
		@RequestParam(value = "pageSize", required = false, defaultValue = "${page.size.default}") Integer pageSize,
		@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
		@RequestParam(name = "order-by", required = false, defaultValue = "${default.orderBy}") String orderBy,
		@RequestParam(name = "order", required = false, defaultValue = "${default.order}") String order) {
		
		Pageable pageable = getPageable(pageSize, pageNum, orderBy, order);
		Page<Classifier> allClassifiersByTaskPage = classifiersService.findAllClassifiersByTask(pageable, id);
		Resources<Resource<Classifier>> classifiersByTaskPagedResources =
			classifiersResourceAssembler.toPagedSubResources(allClassifiersByTaskPage, id, GET_TASK_CLASSIFIERS_METHOD_NAME);
		String jsonPagedResources = getJsonServiceUtils().workshopEntityObjectsToJson(classifiersByTaskPagedResources);
		return ResponseEntity.ok(jsonPagedResources);
	}
	
	/**
	 * Receives a new Classifier, persist if and set into the given Task.
	 *
	 * @param id         Task.ID
	 * @param classifier A Classifier to be persisted.
	 * @return The persisted Classifier with this Task set.
	 */
	@PostMapping(path = "/{id}/classifiers",
				 consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	@PreAuthorize("hasPermission('Classifier', 'post') or hasPermission('Task', 'put')")
	public ResponseEntity<String> postClassifier(
		@PathVariable(name = "id") Long id,
		@Validated(PersistenceValidation.class) @RequestBody Classifier classifier,
		BindingResult bindingResult) {
		
		super.validateBindingResult(bindingResult);
		classifier = classifiersService.persistEntity(classifier);
		Task task = getWorkshopEntitiesService().findById(id);
		task.addClassifier(classifier);
		getWorkshopEntitiesService().mergeEntity(task);
		if (classifier.getTasks() == null) { //Just to be fulfilled before sending back
			classifier.setTasks(new HashSet<>(Collections.singletonList(task)));
		} else {
			classifier.getTasks().add(task);
		}
		Resource<Classifier> classifierResource = classifiersResourceAssembler.toResource(classifier);
		String jsonClassifierResource = getJsonServiceUtils().workshopEntityObjectsToJson(classifierResource);
		return ResponseEntity.status(HttpStatus.CREATED).body(jsonClassifierResource);
	}
	
	/**
	 * Receives an existing Classifier, set into the given Task and updates it state.
	 *
	 * @param id         Task.ID
	 * @param classifier A Classifier to be updated.
	 * @return The updated Classifier with this Task set.
	 */
	@PutMapping(path = "/{id}/classifiers",
				consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	@PreAuthorize("hasPermission('Classifier', 'put') or hasPermission('Task', 'put')")
	public ResponseEntity<String> putClassifier(
		@PathVariable(name = "id") Long id,
		@Validated(MergingValidation.class) @RequestBody Classifier classifier,
		BindingResult bindingResult) {
		
		super.validateBindingResult(bindingResult);
		classifier = classifiersService.mergeEntity(classifier);
		Task task = getWorkshopEntitiesService().findById(id);
		task.addClassifier(classifier);
		getWorkshopEntitiesService().mergeEntity(task);
		if (classifier.getTasks() == null) { //Just to be fulfilled before sending back
			classifier.setTasks(new HashSet<>(Collections.singletonList(task)));
		} else {
			classifier.getTasks().add(task);
		}
		Resource<Classifier> classifierResource = classifiersResourceAssembler.toResource(classifier);
		String jsonClassifierResource = getJsonServiceUtils().workshopEntityObjectsToJson(classifierResource);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(jsonClassifierResource);
	}
	
	/**
	 * Just deletes a Classifier from a given Task.
	 *
	 * @param id           Task.ID the Classifier have to be deleted from.
	 * @param classifierId The Classifier.ID to be deleted from the Task.
	 * @return HttpStatus.NO_CONTENT
	 */
	@DeleteMapping(path = "{id}/classifiers/{classifierId}")
	@PreAuthorize("hasPermission('Task', 'put')")
	public ResponseEntity<String> deleteClassifier(@PathVariable(name = "id") Long id,
		@PathVariable(name = "classifierId") Long classifierId) {
		
		Task task = getWorkshopEntitiesService().findById(id);
		Classifier classifier = classifiersService.findById(classifierId);
		task.removeClassifier(classifier);
		getWorkshopEntitiesService().mergeEntity(task);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
}
