package workshop.controllers.internal.rest;

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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import workshop.internal.entities.*;
import workshop.internal.entities.hibernateValidation.PersistenceValidation;
import workshop.internal.entities.hibernateValidation.MergingValidation;
import workshop.internal.hateoasResources.*;
import workshop.internal.services.*;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Getter
@RestController
@RequestMapping(path = "/internal/employees", produces = {MediaTypes.HAL_JSON_UTF8_VALUE})
@ExposesResourceFor(Employee.class)
public class EmployeesRestController extends WorkshopRestControllerAbstract<Employee> {
	
	public static final String GET_APPOINTED_TASKS_METHOD_NAME = "getAppointedTasks";
	public static final String GET_TASKS_MODIFIED_BY_METHOD_NAME = "getTasksModifiedBy";
	public static final String GET_TASKS_CREATED_BY_METHOD_NAME = "getTasksCreatedBy";
	public static final String GET_ORDERS_MODIFIED_BY_METHOD_NAME = "getOrdersModifiedBy";
	public static final String GET_ORDERS_CREATED_BY_METHOD_NAME = "getOrdersCreatedBy";
	public static final String GET_PHONES_METHOD_NAME = "getPhones";
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
	public EmployeesRestController(EmployeesService employeesService, EmployeesResourceAssembler employeesResourceAssembler) {
		super(employeesService, employeesResourceAssembler);
	}
	
	@GetMapping(path = "/{id}/phones")
	@PreAuthorize("hasPermission('Phone', 'get')")
	public ResponseEntity<String> getPhones(
		@PathVariable("id") Long id,
		@RequestParam(value = "pageSize", required = false, defaultValue = "${page.size.default}") Integer pageSize,
		@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
		@RequestParam(name = "order-by", required = false, defaultValue = "${default.orderBy}") String orderBy,
		@RequestParam(name = "order", required = false, defaultValue = "${default.order}") String order) {
		
		Pageable phonesPage = super.getPageable(pageSize, pageNum, orderBy, order);
		Page<Phone> allPhonesByUserPage = phonesService.findAllPhonesByEmployee(phonesPage, id);
		Resources<Resource<Phone>> employeePhonesPagedResources =
			phonesResourceAssembler.toPagedSubResources(allPhonesByUserPage, id, GET_PHONES_METHOD_NAME);
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
	@PostMapping(path = "/{id}/phones",
		consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	@PreAuthorize("hasPermission('Employee', 'put') and hasPermission('Phone', 'post')")
	public ResponseEntity<String> postPhone(
		@PathVariable(name = "id") long id,
		@Validated(PersistenceValidation.class) @RequestBody Phone phone,
		BindingResult bindingResult) {
		
		super.validateBindingResult(bindingResult);
		Phone phonePersisted = phonesService.addPhoneToEmployee(id, phone);
		Resource<Phone> phoneResource = phonesResourceAssembler.toResource(phonePersisted);
		String jsonPhoneResource = getJsonServiceUtils().workshopEntityObjectsToJson(phoneResource);
		return ResponseEntity.status(HttpStatus.CREATED).body(jsonPhoneResource);
	}
	
	/**
	 * @param id    Employee.ID
	 * @param phone New or existing Phone to bind with existing Employee
	 * @return Updated Phone with the given Employee set.
	 */
	@PutMapping(path = "/{id}/phones",
		consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	@PreAuthorize("hasPermission('Employee', 'put') and hasPermission('Phone', 'put')")
	public ResponseEntity<String> putPhone(
		@PathVariable(name = "id") long id,
		@Validated(MergingValidation.class) @RequestBody Phone phone,
		BindingResult bindingResult) {
		
		super.validateBindingResult(bindingResult);
		Phone phoneUpdated = phonesService.addPhoneToEmployee(id, phone);
		Resource<Phone> phoneResource = phonesResourceAssembler.toResource(phoneUpdated);
		String jsonPhoneResource = getJsonServiceUtils().workshopEntityObjectsToJson(phoneResource);
		return ResponseEntity.accepted().body(jsonPhoneResource);
	}
	
	/**
	 * Deletes the Phone itself from the DataBase.
	 *
	 * @return HttpStatus.NO_CONTENT in case of success
	 */
	@DeleteMapping(path = "/{id}/phones/{phoneId}")
	@PreAuthorize("hasPermission('Employee', 'put') and hasPermission('Phone', 'delete')")
	public ResponseEntity<String> deletePhone(
		@PathVariable(name = "id") long id,
		@PathVariable(name = "phoneId") Long phoneId) {
		
		phonesService.deletePhoneFromEmployee(id, phoneId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT)
			.body(getDeleteMessageSuccessLocalized("Phone.ID" + phoneId));
	}
	
	@GetMapping(path = "/{id}/position")
	@PreAuthorize("hasPermission('Position', 'get')")
	public ResponseEntity<String> getPosition(@PathVariable("id") Long id) {
		Employee employeeById = getWorkshopEntitiesService().findById(id);
		Position employeePosition = employeeById.getPosition();
		Resource<Position> positionResource = positionsResourceAssembler.toResource(employeePosition);
		String jsonPositionResource = getJsonServiceUtils().workshopEntityObjectsToJson(positionResource);
		return ResponseEntity.ok(jsonPositionResource);
	}
	
	@PostMapping(path = "/{id}/position",
		consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	@PreAuthorize("hasPermission('Employee', 'put') and hasPermission('Position', 'post')")
	public ResponseEntity<String> postPosition(
		@PathVariable(name = "id") Long id,
		@Validated(PersistenceValidation.class) @RequestBody Position position,
		BindingResult bindingResult) {
		
		super.validateBindingResult(bindingResult);
		Position persistedPosition = positionsService.addPositionToEmployee(id, position);
		Resource<Position> positionResource = positionsResourceAssembler.toResource(persistedPosition);
		String jsonPositionResource = getJsonServiceUtils().workshopEntityObjectsToJson(positionResource);
		return ResponseEntity.status(HttpStatus.CREATED).body(jsonPositionResource);
	}
	
	@PutMapping(path = "/{id}/position",
		consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	@PreAuthorize("hasPermission('Employee', 'put') and hasPermission('Position', 'put')")
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
	@DeleteMapping(path = "/{id}/position/{positionId}")
	@PreAuthorize("hasPermission('Employee', 'put') and hasPermission('Position', 'put')")
	public ResponseEntity<String> deletePositionForbidden(
		@PathVariable(name = "id") Long id,
		@PathVariable(name = "positionId") Long positionId) {
		
		Position position = positionsService.findById(positionId);
		Resource<Position> positionResource = positionsResourceAssembler.toResource(position);
		String jsonPositionResource = getJsonServiceUtils().workshopEntityObjectsToJson(positionResource);
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(jsonPositionResource);
	}
	
	@GetMapping(path = "/{id}/appointed-tasks")
	@PreAuthorize("hasPermission('Task', 'get')")
	public ResponseEntity<String> getAppointedTasks(
		@PathVariable(name = "id") Long id,
		@RequestParam(value = "pageSize", required = false, defaultValue = "${page.size.default}") Integer pageSize,
		@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
		@RequestParam(name = "order-by", required = false, defaultValue = "${default.orderBy}") String orderBy,
		@RequestParam(name = "order", required = false, defaultValue = "${default.order}") String order) {
		
		Pageable pageable = super.getPageable(pageSize, pageNum, orderBy, order);
		Page<Task> tasksAppointedToEmployeePage = tasksService.findTasksAppointedToEmployee(pageable, id);
		Resources<Resource<Task>> employeeAppointedTasksResources =
			tasksResourceAssembler.toPagedSubResources(tasksAppointedToEmployeePage, id, GET_APPOINTED_TASKS_METHOD_NAME);
		String jsonEmployeeAppointedTasksResources =
			getJsonServiceUtils().workshopEntityObjectsToJson(employeeAppointedTasksResources);
		return ResponseEntity.ok(jsonEmployeeAppointedTasksResources);
	}
	
	/**
	 * Receives a new Task, persists it and appoints to the existing Employee.
	 */
	@PostMapping(path = "/{id}/appointed-tasks",
		consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	@PreAuthorize("hasPermission('Task', 'post')")
	public ResponseEntity<String> postAppointedTask(
		@PathVariable(name = "id") Long id,
		@Validated(PersistenceValidation.class) @RequestBody Task task,
		BindingResult bindingResult) {
		
		super.validateBindingResult(bindingResult);
		Task taskAppointed = tasksService.appointTaskToEmployee(id, task);
		Resource<Task> taskResource = tasksResourceAssembler.toResource(taskAppointed);
		String jsonTaskResource = getJsonServiceUtils().workshopEntityObjectsToJson(taskResource);
		return ResponseEntity.status(HttpStatus.CREATED).body(jsonTaskResource);
	}
	
	/**
	 * Receives an existing Task, merge its changes to the DataBase, appoints to the existing Employee and returns
	 * that updated Task.
	 */
	@PutMapping(path = "/{id}/appointed-tasks",
		consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	@PreAuthorize("hasPermission('Task', 'put')")
	public ResponseEntity<String> putAppointedTask(
		@PathVariable(name = "id") Long id,
		@Validated(MergingValidation.class) @RequestBody Task task,
		BindingResult bindingResult) {
		
		return postAppointedTask(id, task, bindingResult);
	}
	
	/**
	 * Just removes a Task from being appointed to the Employee.
	 */
	@DeleteMapping(path = "/{id}/appointed-tasks/{taskId}")
	@PreAuthorize("hasPermission('Task', 'put')")
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
	@PreAuthorize("hasPermission('Task', 'get')")
	public ResponseEntity<String> getTasksModifiedBy(
		@PathVariable(name = "id") Long id,
		@RequestParam(value = "pageSize", required = false, defaultValue = "${page.size.default}") Integer pageSize,
		@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
		@RequestParam(name = "order-by", required = false, defaultValue = "${default.orderBy}") String orderBy,
		@RequestParam(name = "order", required = false, defaultValue = "${default.order}") String order) {
		
		Pageable pageable = super.getPageable(pageSize, pageNum, orderBy, order);
		Page<Task> tasksModifiedByEmployeePage = tasksService.findTasksModifiedByEmployee(pageable, id);
		Resources<Resource<Task>> tasksModifiedPagedResources =
			tasksResourceAssembler.toPagedSubResources(tasksModifiedByEmployeePage, id, GET_TASKS_MODIFIED_BY_METHOD_NAME);
		String jsonPagedResources = getJsonServiceUtils().workshopEntityObjectsToJson(tasksModifiedPagedResources);
		return ResponseEntity.ok(jsonPagedResources);
	}
	
	/**
	 * @return ErrorMessage about the fact that 'modifiedBy' property is filled in automatically only.
	 */
	@RequestMapping(path = "/{id}/tasks-modified-by/{taskId}",
		method = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE},
		consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	@PreAuthorize("hasPermission('Task', 'put') or hasPermission('Task', 'post') or hasPermission('Task', 'delete')")
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
	@PreAuthorize("hasPermission('Task', 'get')")
	public ResponseEntity<String> getTasksCreatedBy(
		@PathVariable(name = "id") Long id,
		@RequestParam(value = "pageSize", required = false, defaultValue = "${page.size.default}") Integer pageSize,
		@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
		@RequestParam(name = "order-by", required = false, defaultValue = "${default.orderBy}") String orderBy,
		@RequestParam(name = "order", required = false, defaultValue = "${default.order}") String order) {
		
		Pageable pageable = super.getPageable(pageSize, pageNum, orderBy, order);
		Page<Task> tasksCreatedByEmployee = tasksService.findTasksCreatedByEmployee(pageable, id);
		Resources<Resource<Task>> tasksCreatedByResources =
			tasksResourceAssembler.toPagedSubResources(tasksCreatedByEmployee, id, GET_TASKS_CREATED_BY_METHOD_NAME);
		String jsonTasksCreatedBy = getJsonServiceUtils().workshopEntityObjectsToJson(tasksCreatedByResources);
		return ResponseEntity.ok(jsonTasksCreatedBy);
	}
	
	/**
	 * Receives a new Task, persist it and sets as 'createdBy' a given Employee.
	 *
	 * @return Created Task with the 'createdBy' set.
	 */
	@PostMapping(path = "/{id}/tasks-created-by",
		consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	@PreAuthorize("hasPermission('Task', 'post')")
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
		return ResponseEntity.status(HttpStatus.CREATED).body(jsonTaskResource);
	}
	
	/**
	 * Receives an existing Task and updates it with the given Employee as 'createdBy'.
	 *
	 * @return An updated Task with the new 'createdBy'.
	 */
	@PutMapping(path = "/{id}/tasks-created-by",
		consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	@PreAuthorize("hasPermission('Task', 'put')")
	public ResponseEntity<String> putTaskCreatedBy(
		@PathVariable(name = "id") Long id,
		@Validated(MergingValidation.class) @RequestBody Task task,
		BindingResult bindingResult) {
		
		super.validateBindingResult(bindingResult);
		Employee employee = getWorkshopEntitiesService().findById(id);
		task.setCreatedBy(employee);
		Task mergedCreatedByTask = tasksService.mergeEntity(task);
		Resource<Task> taskResource = tasksResourceAssembler.toResource(mergedCreatedByTask);
		String jsonTaskResource = getJsonServiceUtils().workshopEntityObjectsToJson(taskResource);
		return ResponseEntity.accepted().body(jsonTaskResource);
	}
	
	/**
	 * Just deletes the 'createdBy' property from a given Task
	 *
	 * @param id     Employee id to be deleted from Task's 'createdBy' property.
	 * @param taskId The Task that needs the deletion of 'createdBy' property.
	 * @return The renewed Task without 'createdBy'.
	 */
	@DeleteMapping(path = "/{id}/tasks-created-by/{taskId}")
	@PreAuthorize("hasPermission('Employee', 'put') and hasPermission('Task', 'put')")
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
	@PreAuthorize("hasPermission('Order', 'get')")
	public ResponseEntity<String> getOrdersModifiedBy(
		@PathVariable(name = "id") Long id,
		@RequestParam(value = "pageSize", required = false, defaultValue = "${page.size.default}") Integer pageSize,
		@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
		@RequestParam(name = "order-by", required = false, defaultValue = "${default.orderBy}") String orderBy,
		@RequestParam(name = "order", required = false, defaultValue = "${default.order}") String order) {
		
		Pageable pageable = super.getPageable(pageSize, pageNum, orderBy, order);
		Page<Order> ordersModifiedByEmployeePage = ordersService.findAllOrdersModifiedByEmployee(pageable, id);
		Resources<Resource<Order>> ordersModifiedByResources =
			ordersResourceAssembler.toPagedSubResources(ordersModifiedByEmployeePage, id, GET_ORDERS_MODIFIED_BY_METHOD_NAME);
		String jsonOrdersModifiedByResources = getJsonServiceUtils().workshopEntityObjectsToJson(ordersModifiedByResources);
		return ResponseEntity.ok(jsonOrdersModifiedByResources);
	}
	
	/**
	 * @return ErrorMessage about the fact that 'modifiedBy' property is filled in automatically only.
	 */
	@RequestMapping(path = "/{id}/orders-modified-by/{orderId}",
		method = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE},
		consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	@PreAuthorize("hasPermission('Order', 'put') or hasPermission('Order', 'post') or hasPermission('Order', 'delete')")
	public ResponseEntity<String> notAllowedOrderModifiedBy(@PathVariable(name = "id") Long id,
															@PathVariable(name = "orderId") Long orderId,
															HttpServletRequest request) {
		
		String notAllowedMessage = getMessageSource().getMessage(
			"httpStatus.methodNotAllowed(2)",
			new Object[]{request.getMethod(), "As this is being applied automatically only."},
			LocaleContextHolder.getLocale());
		return getResponseEntityWithErrorMessage(HttpStatus.METHOD_NOT_ALLOWED, notAllowedMessage);
	}
	
	@GetMapping(path = "/{id}/orders-created-by")
	@PreAuthorize("hasPermission('Order', 'get')")
	public ResponseEntity<String> getOrdersCreatedBy(
		@PathVariable(name = "id") Long id,
		@RequestParam(value = "pageSize", required = false, defaultValue = "${page.size.default}") Integer pageSize,
		@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
		@RequestParam(name = "order-by", required = false, defaultValue = "${default.orderBy}") String orderBy,
		@RequestParam(name = "order", required = false, defaultValue = "${default.order}") String order) {
		
		Pageable pageable = super.getPageable(pageSize, pageNum, orderBy, order);
		Page<Order> ordersCreatedByEmployeePage = ordersService.findAllOrdersCreatedByEmployee(pageable, id);
		Resources<Resource<Order>> ordersCreatedByResources =
			ordersResourceAssembler.toPagedSubResources(ordersCreatedByEmployeePage, id, GET_ORDERS_CREATED_BY_METHOD_NAME);
		String jsonOrdersCreatedByResources = getJsonServiceUtils().workshopEntityObjectsToJson(ordersCreatedByResources);
		return ResponseEntity.ok(jsonOrdersCreatedByResources);
	}
	
	/**
	 * Receives a new Order, persist it and sets as 'createdBy' a given Employee.
	 *
	 * @return Created Order with the 'createdBy' set.
	 */
	@PostMapping(path = "/{id}/orders-created-by",
		consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	@PreAuthorize("hasPermission('Order', 'post')")
	public ResponseEntity<String> postOrderCreatedBy(
		@PathVariable(name = "id") Long id,
		@Validated(PersistenceValidation.class) @RequestBody Order order,
		BindingResult bindingResult) {
		
		super.validateBindingResult(bindingResult);
		Employee employee = getWorkshopEntitiesService().findById(id);
		order.setCreatedBy(employee);
		Order persistedCreatedByOrder = ordersService.persistEntity(order);
		Resource<Order> orderResource = ordersResourceAssembler.toResource(persistedCreatedByOrder);
		String jsonOrderResource = getJsonServiceUtils().workshopEntityObjectsToJson(orderResource);
		return ResponseEntity.status(HttpStatus.CREATED).body(jsonOrderResource);
	}
	
	/**
	 * Receives an existing Order and updates it with the given Employee as 'createdBy'.
	 *
	 * @return An updated Order with the new 'createdBy'.
	 */
	@PutMapping(path = "/{id}/orders-created-by",
		consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	@PreAuthorize("hasPermission('Order', 'put')")
	public ResponseEntity<String> putOrderCreatedBy(
		@PathVariable(name = "id") Long id,
		@Validated(MergingValidation.class) @RequestBody Order order,
		BindingResult bindingResult) {
		
		super.validateBindingResult(bindingResult);
		Employee employee = getWorkshopEntitiesService().findById(id);
		order.setCreatedBy(employee);
		Order mergedCreatedByOrder = ordersService.mergeEntity(order);
		Resource<Order> orderResource = ordersResourceAssembler.toResource(mergedCreatedByOrder);
		String jsonOrderResource = getJsonServiceUtils().workshopEntityObjectsToJson(orderResource);
		return ResponseEntity.accepted().body(jsonOrderResource);
	}
	
	/**
	 * Just deletes the 'createdBy' property from a given Order
	 *
	 * @param id      Employee id to be deleted from Order's 'createdBy' property.
	 * @param orderId The Order that needs the deletion of 'createdBy' property.
	 * @return The renewed Order without 'createdBy'.
	 */
	@DeleteMapping(path = "/{id}/orders-created-by/{orderId}")
	@PreAuthorize("hasPermission('Order', 'put')")
	public ResponseEntity<String> deleteOrderCreatedBy(
		@PathVariable(name = "id") Long id,
		@PathVariable(name = "orderId") Long orderId) {
		
		Order order = ordersService.findById(orderId);
		if (order.getCreatedBy() != null && order.getCreatedBy().getIdentifier().equals(id)) {
			order.setCreatedBy(null);
			Order orderWithoutCreatedBy = ordersService.mergeEntity(order);
			Resource<Order> orderWithoutCreatedByResource = ordersResourceAssembler.toResource(orderWithoutCreatedBy);
			String jsonOrderkWithoutCreatedBy = getJsonServiceUtils().workshopEntityObjectsToJson(orderWithoutCreatedByResource);
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(jsonOrderkWithoutCreatedBy);
		} else {
			String errorMessage = getMessageSource().getMessage(
				"httpStatus.notFound(2)",
				new Object[]{"Employee.ID=" + id, "Order.ID=" + orderId},
				LocaleContextHolder.getLocale());
			return getResponseEntityWithErrorMessage(HttpStatus.NOT_FOUND, errorMessage);
		}
	}
	
	@PostMapping(path = "/{id}/photo", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
	public ResponseEntity<String> photo(@RequestParam(name = "photo") MultipartFile photo) {
		
		
		//TODO: to complete
		
		return null;
	}
}
