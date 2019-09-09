package internal.controllers;

import internal.entities.*;
import internal.entities.hibernateValidation.PersistenceValidation;
import internal.entities.hibernateValidation.UpdateValidation;
import internal.hateoasResources.*;
import internal.services.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Getter
@RestController
@RequestMapping(path = "/internal/employees", produces = {MediaTypes.HAL_JSON_UTF8_VALUE})
@ExposesResourceFor(Employee.class)
public class EmployeesController extends WorkshopControllerAbstract<Employee> {
	
	public static final String APPOINTED_TASKS_METHOD_NAME = "getAppointedTasks";
	public static final String TASKS_MODIFIED_BY_METHOD_NAME = "getTasksModifiedBy";
	public static final String TASKS_CREATED_BY_METHOD_NAME = "getTasksCreatedBy";
	public static final String ORDERS_MODIFIED_BY_METHOD_NAME = "getOrdersModifiedBy";
	public static final String ORDERS_CREATED_BY_METHOD_NAME = "getOrdersCreatedBy";
	public static final String PHONES_METHOD_NAME = "getPhones";
	@Autowired
	private PositionsResourceAssembler positionsResourceAssembler;
	@Autowired
	private TasksResourceAssembler tasksResourceAssembler;
	@Autowired
	private OrdersResourceAssembler ordersResourceAssembler;
	@Autowired
	private PhonesResourceAssembler phonesResourceAssembler;
	@Autowired
	private TasksService tasksService;
	@Autowired
	private OrdersService ordersService;
	@Autowired
	private PhonesService phonesService;
	@Autowired
	private PositionsService positionsService;
	
	/**
	 * @param employeesService By this instance we set the concrete instance of WorkshopServiceAbstract
	 *                         and through it set the concrete type of WorkshopEntity as {@link #getWorkshopEntityClass()}
	 *                         to operate with.
	 */
	public EmployeesController(EmployeesService employeesService, EmployeesResourceAssembler employeesResourceAssembler) {
		super(employeesService, employeesResourceAssembler);
	}
	
	@GetMapping(path = "/{id}/phones")
	public ResponseEntity<String> getPhones(
		@PathVariable("id") Long id,
		@RequestParam(value = "pageSize", required = false, defaultValue = "${page.size.default}") Integer pageSize,
		@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
		@RequestParam(name = "order-by", required = false, defaultValue = "${default.orderBy}") String orderBy,
		@RequestParam(name = "order", required = false, defaultValue = "${default.order}") String order) {
		
		Pageable phonesPage = super.getPageable(pageSize, pageNum, orderBy, order);
		Page<Phone> allPhonesByUserPage = phonesService.findAllPhonesByEmployee(phonesPage, id);
		Resources<Resource<Phone>> employeePhonesPagedResources =
			phonesResourceAssembler.toPagedSubResources(allPhonesByUserPage, id, PHONES_METHOD_NAME);
		String jsonEmployeePhonesPagedResources = getJsonServiceUtils().workshopEntityObjectsToJson(employeePhonesPagedResources);
		return ResponseEntity.ok(jsonEmployeePhonesPagedResources);
	}
	
	/**
	 * Creates a new Phone and set it to the existing Employee.
	 *
	 * @param id    Employee.ID to set the new Phone to.
	 * @param phone A new Phone to be created and set to existing Employee
	 * @return Persisted Phone as a Resource.
	 */
	@PostMapping(path = "/{id}/phones")
	public ResponseEntity<String> postPhone(
		@PathVariable(name = "id") long id,
		@Validated(PersistenceValidation.class) @RequestBody Phone phone,
		BindingResult bindingResult) {
		
		super.validateBindingResult(bindingResult);
		Phone phonePersisted = phonesService.addPhoneToEmployee(id, phone);
		Resource<Phone> phoneResource = phonesResourceAssembler.toResource(phonePersisted);
		String jsonPhoneResource = getJsonServiceUtils().workshopEntityObjectsToJson(phoneResource);
		return ResponseEntity.ok(jsonPhoneResource);
	}
	
	/**
	 * @param id    Employee.ID
	 * @param phone New or existing Phone to bind with existing Employee
	 * @return Updated Phone with the given Employee set.
	 */
	@PutMapping(path = "/{id}/phones")
	public ResponseEntity<String> putPhone(
		@PathVariable(name = "id") long id,
		@Validated(UpdateValidation.class) @RequestBody Phone phone,
		BindingResult bindingResult) {
		
		super.validateBindingResult(bindingResult);
		Phone phoneUpdated = phonesService.addPhoneToEmployee(id, phone);
		Resource<Phone> phoneResource = phonesResourceAssembler.toResource(phoneUpdated);
		String jsonPhoneResource = getJsonServiceUtils().workshopEntityObjectsToJson(phoneResource);
		return ResponseEntity.ok(jsonPhoneResource);
	}
	
	@DeleteMapping(path = "/{id}/phones/{phoneId}")
	public ResponseEntity<String> deletePhone(
		@PathVariable(name = "id") long id,
		@PathVariable(name = "phoneId") Long phoneId) {
		
		phonesService.deletePhoneFromEmployee(id, phoneId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT)
			.body(getDeleteMessageSuccessLocalized("Phone.ID" + phoneId));
	}
	
	@GetMapping(path = "/{id}/position")
	public ResponseEntity<String> getPosition(@PathVariable("id") Long id) {
		Employee employeeById = getWorkshopEntitiesService().findById(id);
		Position employeePosition = employeeById.getPosition();
		Resource<Position> positionResource = positionsResourceAssembler.toResource(employeePosition);
		String jsonPositionResource = getJsonServiceUtils().workshopEntityObjectsToJson(positionResource);
		return ResponseEntity.ok(jsonPositionResource);
	}
	
	@PostMapping(path = "/{id}/positions")
	public ResponseEntity<String> postPosition(
		@PathVariable(name = "id") Long id,
		@Validated(PersistenceValidation.class) @RequestBody Position position,
		BindingResult bindingResult) {
		
		super.validateBindingResult(bindingResult);
		Position persistedPosition = positionsService.addPositionToEmployee(id, position);
		Resource<Position> positionResource = positionsResourceAssembler.toResource(persistedPosition);
		String jsonPositionResource = getJsonServiceUtils().workshopEntityObjectsToJson(positionResource);
		return ResponseEntity.ok(jsonPositionResource);
	}
	
	@PutMapping(path = "/{id}/positions")
	public ResponseEntity<String> putPosition(
		@PathVariable(name = "id") Long id,
		@Validated(PersistenceValidation.class) @RequestBody Position position,
		BindingResult bindingResult) {
		return postPosition(id, position, bindingResult);
	}
	
	/**
	 * Wont delete the Position. Instead this method will return HttpStatus.FORBIDDEN and the message with the
	 * Link to the Position itself.
	 *
	 * @param id         Employee.ID
	 * @param positionId Self-description.
	 * @return HttpStatus.FORBIDDEN and the message with the Position as a Resource with the Link to it.
	 */
	@DeleteMapping(path = "/{id}/positions/{positionId}")
	public ResponseEntity<String> deletePosition(
		@PathVariable(name = "id") Long id,
		@PathVariable(name = "positionId") Long positionId) {
		
		Position position = positionsService.findById(positionId);
		Resource<Position> positionResource = positionsResourceAssembler.toResource(position);
		String jsonPositionResource = getJsonServiceUtils().workshopEntityObjectsToJson(positionResource);
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(jsonPositionResource);
	}
	
	@GetMapping(path = "/{id}/appointed-tasks")
	public ResponseEntity<String> getAppointedTasks(
		@PathVariable(name = "id") Long id,
		@RequestParam(value = "pageSize", required = false, defaultValue = "${page.size.default}") Integer pageSize,
		@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
		@RequestParam(name = "order-by", required = false, defaultValue = "${default.orderBy}") String orderBy,
		@RequestParam(name = "order", required = false, defaultValue = "${default.order}") String order) {
		
		Pageable pageable = super.getPageable(pageSize, pageNum, orderBy, order);
		Page<Task> tasksAppointedToEmployeePage = tasksService.findAllTasksAppointedToEmployee(pageable, id);
		Resources<Resource<Task>> employeeAppointedTasksResources =
			tasksResourceAssembler.toPagedSubResources(tasksAppointedToEmployeePage, id, APPOINTED_TASKS_METHOD_NAME);
		String jsonEmployeeAppointedTasksResources =
			getJsonServiceUtils().workshopEntityObjectsToJson(employeeAppointedTasksResources);
		return ResponseEntity.ok(jsonEmployeeAppointedTasksResources);
	}
	
	/**
	 * Receives a new Task, persists it and appoints to the existing Employee.
	 */
	@PostMapping(path = "/{id}/appointed-tasks")
	public ResponseEntity<String> postAppointedTask(
		@PathVariable(name = "id") Long id,
		@Validated(PersistenceValidation.class) @RequestBody Task task,
		BindingResult bindingResult) {
		
		super.validateBindingResult(bindingResult);
		Task taskAppointed = tasksService.appointTaskToEmployee(id, task);
		Resource<Task> taskResource = tasksResourceAssembler.toResource(taskAppointed);
		String jsonTaskResource = getJsonServiceUtils().workshopEntityObjectsToJson(taskResource);
		return ResponseEntity.ok(jsonTaskResource);
	}
	
	/**
	 * Receives an existing Task, merge its changes to the DataBase, appoints to the existing Employee and returns
	 * that updated Task.
	 */
	@PutMapping(path = "/{id}/appointed-tasks")
	public ResponseEntity<String> putAppointedTask(
		@PathVariable(name = "id") Long id,
		@Validated(UpdateValidation.class) @RequestBody Task task,
		BindingResult bindingResult) {
		
		return postAppointedTask(id, task, bindingResult);
	}
	
	/**
	 * Just removes a Task from being appointed to the Employee.
	 */
	@DeleteMapping(path = "/{id}/appointed-tasks/{taskId}")
	public ResponseEntity<String> deleteAppointedTask(@PathVariable(name = "id") Long id,
													  @PathVariable(name = "taskId") Long taskId) {
		
		Task task = tasksService.findById(taskId);
		if (task.getAppointedTo() != null && task.getAppointedTo().getIdentifier().equals(id)) {
			task.setAppointedTo(null);
			Task mergedTask = tasksService.mergeEntity(task);
			Resource<Task> taskResource = tasksResourceAssembler.toResource(mergedTask);
			String jsonTaskResource = getJsonServiceUtils().workshopEntityObjectsToJson(taskResource);
			return ResponseEntity.ok(jsonTaskResource);
		} else {
			return getResponseEntityWithErrorMessage(HttpStatus.NOT_FOUND, getMessageSource().getMessage(
				"httpStatus.notFound(2)",
				new Object[]{"Task.ID" + taskId, getWorkshopEntityClassName() + ".ID=" + id},
				LocaleContextHolder.getLocale()));
		}
	}
	
	@GetMapping(path = "/{id}/tasks-modified-by")
	public ResponseEntity<String> getTasksModifiedBy(
		@PathVariable(name = "id") Long id,
		@RequestParam(value = "pageSize", required = false, defaultValue = "${page.size.default}") Integer pageSize,
		@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
		@RequestParam(name = "order-by", required = false, defaultValue = "${default.orderBy}") String orderBy,
		@RequestParam(name = "order", required = false, defaultValue = "${default.order}") String order) {
		
		Pageable pageable = super.getPageable(pageSize, pageNum, orderBy, order);
		Page<Task> tasksModifiedByEmployeePage = tasksService.findAllTasksModifiedByEmployee(pageable, id);
		Resources<Resource<Task>> tasksModifiedPagedResources =
			tasksResourceAssembler.toPagedSubResources(tasksModifiedByEmployeePage, id, TASKS_MODIFIED_BY_METHOD_NAME);
		String jsonPagedResources = getJsonServiceUtils().workshopEntityObjectsToJson(tasksModifiedPagedResources);
		return ResponseEntity.ok(jsonPagedResources);
	}
	
	/**
	 * @return ErrorMessage about the fact that 'modifiedBy' property is filled in automatically only.
	 */
	@Secured({"Administrator"})
	@RequestMapping(path = "/{id}/tasks-modified-by/{taskId}",
		method = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
	public ResponseEntity<String> notAllowedTaskModifiedBy(@PathVariable(name = "id") Long id,
														   @PathVariable(name = "taskId") Long taskId,
														   HttpServletRequest request) {
		
		String notAllowedMessage = getMessageSource().getMessage(
			"httpStatus.methodNotAllowed(2)",
			new Object[]{request.getMethod(), "As this is being applied automatically only."},
			LocaleContextHolder.getLocale());
		return getResponseEntityWithErrorMessage(HttpStatus.METHOD_NOT_ALLOWED, notAllowedMessage);
	}
	
	@GetMapping(path = "/{id}/tasks-created-by")
	public ResponseEntity<String> getTasksCreatedBy(
		@PathVariable(name = "id") Long id,
		@RequestParam(value = "pageSize", required = false, defaultValue = "${page.size.default}") Integer pageSize,
		@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
		@RequestParam(name = "order-by", required = false, defaultValue = "${default.orderBy}") String orderBy,
		@RequestParam(name = "order", required = false, defaultValue = "${default.order}") String order) {
		
		Pageable pageable = super.getPageable(pageSize, pageNum, orderBy, order);
		Page<Task> tasksCreatedByEmployee = tasksService.findAllTasksCreatedByEmployee(pageable, id);
		Resources<Resource<Task>> tasksCreatedByResources =
			tasksResourceAssembler.toPagedSubResources(tasksCreatedByEmployee, id, TASKS_CREATED_BY_METHOD_NAME);
		String jsonTasksCreatedBy = getJsonServiceUtils().workshopEntityObjectsToJson(tasksCreatedByResources);
		return ResponseEntity.ok(jsonTasksCreatedBy);
	}
	
	/**
	 * Receives a new Task, persist it and sets as 'createdBy' a given Employee.
	 *
	 * @return Created Task with the 'createdBy' set.
	 */
	@PostMapping(path = "/{id}/tasks-created-by")
	public ResponseEntity<String> postTaskCreatedBy(
		@PathVariable(name = "id") Long id,
		@Validated(PersistenceValidation.class) @RequestBody Task task,
		BindingResult bindingResult) {
		
		super.validateBindingResult(bindingResult);
		Employee employee = getWorkshopEntitiesService().findById(id);
		task.setCreatedBy(employee);
		Task persistedCreatedByTask = tasksService.persistEntity(task);
		Resource<Task> taskResource = tasksResourceAssembler.toResource(persistedCreatedByTask);
		String jsonTaskResource = getJsonServiceUtils().workshopEntityObjectsToJson(taskResource);
		return ResponseEntity.ok(jsonTaskResource);
	}
	
	/**
	 * Receives an existing Task and updates it with the given Employee as 'createdBy'.
	 *
	 * @return An updated Task with the new 'createdBy'.
	 */
	@PutMapping(path = "/{id}/tasks-created-by")
	public ResponseEntity<String> putTaskCreatedBy(
		@PathVariable(name = "id") Long id,
		@Validated(UpdateValidation.class) @RequestBody Task task,
		BindingResult bindingResult) {
		
		super.validateBindingResult(bindingResult);
		Employee employee = getWorkshopEntitiesService().findById(id);
		task.setCreatedBy(employee);
		Task mergedCreatedByTask = tasksService.mergeEntity(task);
		Resource<Task> taskResource = tasksResourceAssembler.toResource(mergedCreatedByTask);
		String jsonTaskResource = getJsonServiceUtils().workshopEntityObjectsToJson(taskResource);
		return ResponseEntity.ok(jsonTaskResource);
	}
	
	/**
	 * Just deletes the 'createdBy' property from a given Task
	 *
	 * @param id     Employee id to be deleted from Task's 'createdBy' property.
	 * @param taskId The Task that needs the deletion of 'createdBy' property.
	 * @return The renewed Task without 'createdBy'.
	 */
	@Secured("Administrator")
	@DeleteMapping(path = "/{id}/tasks-created-by/{taskId}")
	public ResponseEntity<String> deleteTaskCreatedBy(@PathVariable(name = "id") Long id,
													  @PathVariable(name = "taskId") Long taskId) {
		
		Task task = tasksService.findById(taskId);
		if (task.getCreatedBy() != null && task.getCreatedBy().getIdentifier().equals(id)) {
			task.setCreatedBy(null);
			Task taskWithoutCreatedBy = tasksService.mergeEntity(task);
			Resource<Task> taskWithoutCreatedByResource = tasksResourceAssembler.toResource(taskWithoutCreatedBy);
			String jsonTaskWithoutCreatedBy = getJsonServiceUtils().workshopEntityObjectsToJson(taskWithoutCreatedByResource);
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(jsonTaskWithoutCreatedBy);
		} else {
			String errorMessage = getMessageSource().getMessage(
				"httpStatus.notFound(2)",
				new Object[]{"Employee.ID=" + id, "Task.ID=" + taskId},
				LocaleContextHolder.getLocale());
			return getResponseEntityWithErrorMessage(HttpStatus.NOT_FOUND, errorMessage);
		}
	}
	
	@GetMapping(path = "/{id}/orders-modified-by")
	public ResponseEntity<String> getOrdersModifiedBy(
		@PathVariable(name = "id") Long id,
		@RequestParam(value = "pageSize", required = false, defaultValue = "${page.size.default}") Integer pageSize,
		@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
		@RequestParam(name = "order-by", required = false, defaultValue = "${default.orderBy}") String orderBy,
		@RequestParam(name = "order", required = false, defaultValue = "${default.order}") String order) {
		
		Pageable pageable = super.getPageable(pageSize, pageNum, orderBy, order);
		Page<Order> ordersModifiedByEmployeePage = ordersService.findAllOrdersModifiedByEmployee(pageable, id);
		Resources<Resource<Order>> ordersModifiedByResources =
			ordersResourceAssembler.toPagedSubResources(ordersModifiedByEmployeePage, id, ORDERS_MODIFIED_BY_METHOD_NAME);
		String jsonOrdersModifiedByResources = getJsonServiceUtils().workshopEntityObjectsToJson(ordersModifiedByResources);
		return ResponseEntity.ok(jsonOrdersModifiedByResources);
	}
	
	@GetMapping(path = "/{id}/orders-created-by")
	public ResponseEntity<String> getOrdersCreatedBy(
		@PathVariable(name = "id") Long id,
		@RequestParam(value = "pageSize", required = false, defaultValue = "${page.size.default}") Integer pageSize,
		@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
		@RequestParam(name = "order-by", required = false, defaultValue = "${default.orderBy}") String orderBy,
		@RequestParam(name = "order", required = false, defaultValue = "${default.order}") String order) {
		
		Pageable pageable = super.getPageable(pageSize, pageNum, orderBy, order);
		Page<Order> ordersCreatedByEmployeePage = ordersService.findAllOrdersCreatedByEmployee(pageable, id);
		Resources<Resource<Order>> ordersCreatedByResources =
			ordersResourceAssembler.toPagedSubResources(ordersCreatedByEmployeePage, id, ORDERS_CREATED_BY_METHOD_NAME);
		String jsonOrdersCreatedByResources = getJsonServiceUtils().workshopEntityObjectsToJson(ordersCreatedByResources);
		return ResponseEntity.ok(jsonOrdersCreatedByResources);
	}
	
	public ResponseEntity<String> photo() {
		
		//TODO: to complete
		
		return null;
	}
}
