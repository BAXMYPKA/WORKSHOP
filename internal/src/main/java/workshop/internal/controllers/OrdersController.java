package workshop.internal.controllers;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import workshop.internal.entities.Order;
import workshop.internal.entities.Task;
import workshop.internal.entities.User;
import workshop.internal.entities.hibernateValidation.PersistenceValidation;
import workshop.internal.entities.hibernateValidation.MergingValidation;
import workshop.internal.hateoasResources.OrdersResourceAssembler;
import workshop.internal.hateoasResources.TasksResourceAssembler;
import workshop.internal.hateoasResources.UsersResourceAssembler;
import workshop.internal.services.OrdersService;
import workshop.internal.services.TasksService;
import workshop.internal.services.UsersService;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = "/internal/orders", produces = {MediaTypes.HAL_JSON_UTF8_VALUE})
@ExposesResourceFor(Order.class)
public class OrdersController extends WorkshopControllerAbstract<Order> {
	
	public static final String GET_ORDER_TASKS_METHOD_NAME = "getOrderTasks";
	@Autowired
	private TasksService tasksService;
	@Autowired
	private UsersService usersService;
	@Autowired
	private UsersResourceAssembler usersResourceAssembler;
	@Autowired
	private TasksResourceAssembler tasksResourceAssembler;
	@Value("${page.size.default}")
	private int DEFAULT_PAGE_SIZE;
	@Value("${page.max_num}")
	private int MAX_PAGE_NUM;
	
	public OrdersController(OrdersService ordersService, OrdersResourceAssembler ordersResourceAssembler) {
		super(ordersService, ordersResourceAssembler);
	}
	
	/**
	 * @param id OrderID to get the User from.
	 */
	@GetMapping(path = "/{id}/user")
	@PreAuthorize("hasPermission('User', 'get')")
	public ResponseEntity<String> getUserCreatedFor(@PathVariable("id") Long id) {
		
		User userById = ((OrdersService) getWorkshopEntitiesService()).findUserByOrder(id);
		Resource<User> userResource = usersResourceAssembler.toResource(userById);
		String jsonUser = getJsonServiceUtils().workshopEntityObjectsToJson(userResource);
		return ResponseEntity.ok(jsonUser);
	}
	
	/**
	 * Receives a new User, persist it and sets as 'createdFor' a given existing Order.
	 *
	 * @return Created User.
	 */
	@PostMapping(path = "/{id}/user",
				 consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	@PreAuthorize("hasPermission('Order', 'put') or hasPermission('User', 'post')")
	public ResponseEntity<String> postUserCreatedFor(
		@PathVariable(name = "id") Long id,
		@Validated(PersistenceValidation.class) @RequestBody User user,
		BindingResult bindingResult) {
		
		super.validateBindingResult(bindingResult);
		Order order = getWorkshopEntitiesService().findById(id);
		user = usersService.persistEntity(user);
		order.setCreatedFor(user);
		getWorkshopEntitiesService().mergeEntity(order);
		Resource<User> userResource = usersResourceAssembler.toResource(user);
		String jsonUserResource = getJsonServiceUtils().workshopEntityObjectsToJson(userResource);
		return ResponseEntity.status(HttpStatus.CREATED).body(jsonUserResource);
	}
	
	/**
	 * Receives an existing User, merge it and sets as 'createdFor' a given existing Order.
	 *
	 * @return A renewed User.
	 */
	@PutMapping(path = "/{id}/user",
				consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	@PreAuthorize("hasPermission('Order', 'put')")
	public ResponseEntity<String> putUserCreatedFor(
		@PathVariable(name = "id") Long id,
		@Validated(MergingValidation.class) @RequestBody User user,
		BindingResult bindingResult) {
		
		super.validateBindingResult(bindingResult);
		Order order = getWorkshopEntitiesService().findById(id);
		user = usersService.mergeEntity(user);
		order.setCreatedFor(user);
		getWorkshopEntitiesService().mergeEntity(order);
		Resource<User> userResource = usersResourceAssembler.toResource(user);
		String jsonUserResource = getJsonServiceUtils().workshopEntityObjectsToJson(userResource);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(jsonUserResource);
	}
	
	/**
	 * Just deletes the given User from Order's 'createdFor' property.
	 * @return HttpStatus.NO_CONTENT in case of success.
	 */
	@DeleteMapping(path = "/{id}/user")
	@PreAuthorize("hasPermission('Order', 'put')")
	public ResponseEntity<String> deleteUserCreatedFor(@PathVariable(name = "id") Long id) {
		
		Order order = getWorkshopEntitiesService().findById(id);
		order.setCreatedFor(null);
		getWorkshopEntitiesService().mergeEntity(order);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
	
	/**
	 * @param id OrderID to get Tasks from.
	 */
	@GetMapping(path = "/{id}/tasks")
	@PreAuthorize("hasPermission('Task', 'get')")
	public ResponseEntity<String> orderTasks(
		@PathVariable("id") Long id,
		@RequestParam(value = "pageSize", required = false, defaultValue = "${page.size.default}") Integer pageSize,
		@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
		@RequestParam(name = "order-by", required = false, defaultValue = "${default.orderBy}") String orderBy,
		@RequestParam(name = "order", required = false, defaultValue = "${default.order}") String order) {
		
		Pageable pageableTasks = getPageable(pageSize, pageNum, orderBy, order);
		Page<Task> tasksByOrderPage = tasksService.findTasksByOrder(pageableTasks, id);
		Resources<Resource<Task>> tasksPagedResources =
			tasksResourceAssembler.toPagedSubResources(tasksByOrderPage, id, GET_ORDER_TASKS_METHOD_NAME);
		String jsonTasksPagedResources = getJsonServiceUtils().workshopEntityObjectsToJson(tasksPagedResources);
		return ResponseEntity.ok(jsonTasksPagedResources);
	}
	
	/**
	 * Receives a new Task, persist it and sets to a given existing Order.
	 *
	 * @return Created Task.
	 */
	@PostMapping(path = "/{id}/tasks",
				 consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	@PreAuthorize("hasPermission('Order', 'put') or hasPermission('Task', 'post')")
	public ResponseEntity<String> postTask(
		@PathVariable(name = "id") Long id,
		@Validated(PersistenceValidation.class) @RequestBody Task task,
		BindingResult bindingResult) {
		
		super.validateBindingResult(bindingResult);
		Order order = getWorkshopEntitiesService().findById(id);
		task.setOrder(order);
		task = tasksService.persistEntity(task);
		order.addTask(task);
		getWorkshopEntitiesService().mergeEntity(order);
		
		Resource<Task> taskResource = tasksResourceAssembler.toResource(task);
		String jsonTaskResource = getJsonServiceUtils().workshopEntityObjectsToJson(taskResource);
		return ResponseEntity.status(HttpStatus.CREATED).body(jsonTaskResource);
	}
	
	/**
	 * Receives an existing Task, sets it to a given existing Order and returns an updated Task.
	 *
	 * @return An updated Task with the Order set..
	 */
	@PutMapping(path = "/{id}/tasks",
				consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	@PreAuthorize("hasPermission('Order', 'put') or hasPermission('Task', 'put')")
	public ResponseEntity<String> putTask(
		@PathVariable(name = "id") Long id,
		@Validated(MergingValidation.class) @RequestBody Task task,
		BindingResult bindingResult) {
		
		super.validateBindingResult(bindingResult);
		Order order = getWorkshopEntitiesService().findById(id);
		task.setOrder(order);
		task = tasksService.mergeEntity(task);
		order.addTask(task);
		getWorkshopEntitiesService().mergeEntity(order);
		
		Resource<Task> taskResource = tasksResourceAssembler.toResource(task);
		String jsonTaskResource = getJsonServiceUtils().workshopEntityObjectsToJson(taskResource);
		return ResponseEntity.status(HttpStatus.CREATED).body(jsonTaskResource);
	}
	
	/**
	 * Deletes the Task itself from the DataBase.
	 *
	 * @param id     Order.ID the given Task to be removed from.
	 * @param taskId Task.ID to be removed.
	 * @return HttpStatus.NO_CONTENT if the Task is successfully removed.
	 */
	@DeleteMapping(path = "/{id}/tasks/{taskId}")
	@PreAuthorize("hasPermission('Task', 'delete')")
	public ResponseEntity<String> deleteTask(@PathVariable(name = "id") Long id,
											 @PathVariable(name = "taskId") Long taskId) {
		
		Order order = getWorkshopEntitiesService().findById(id);
		if (order.getTasks() != null &&
			order.getTasks().stream().anyMatch(task -> task.getIdentifier().equals(taskId))) {
			tasksService.removeEntity(taskId);
			order.getTasks().removeIf(task -> task.getIdentifier().equals(taskId));
			getWorkshopEntitiesService().mergeEntity(order);
			return ResponseEntity.status(HttpStatus.NO_CONTENT)
				.body(getDeleteMessageSuccessLocalized("Task.ID=" + taskId));
		} else {
			return getResponseEntityWithErrorMessage(
				HttpStatus.NOT_FOUND,
				getMessageSource().getMessage(
					"httpStatus.notFound(2)",
					new Object[]{"Task.ID=" + taskId, "Order.ID=" + id},
					LocaleContextHolder.getLocale()));
		}
	}
	
}
