package internal.controllers;

import internal.entities.Employee;
import internal.entities.Order;
import internal.entities.Task;
import internal.entities.User;
import internal.entities.hibernateValidation.PersistenceValidation;
import internal.entities.hibernateValidation.UpdateValidation;
import internal.hateoasResources.OrdersResourceAssembler;
import internal.hateoasResources.TasksResourceAssembler;
import internal.hateoasResources.UsersResourceAssembler;
import internal.services.OrdersService;
import internal.services.TasksService;
import internal.services.UsersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
	@PostMapping(path = "/{id}/user")
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
	@PutMapping(path = "/{id}/user")
	public ResponseEntity<String> putUserCreatedFor(
		@PathVariable(name = "id") Long id,
		@Validated(UpdateValidation.class) @RequestBody User user,
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
	
	@DeleteMapping(path = "/{id}/user")
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
	public ResponseEntity<String> orderTasks(
		@PathVariable("id") Long id,
		@RequestParam(value = "pageSize", required = false, defaultValue = "${page.size.default}") Integer pageSize,
		@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
		@RequestParam(name = "order-by", required = false, defaultValue = "${default.orderBy}") String orderBy,
		@RequestParam(name = "order", required = false, defaultValue = "${default.order}") String order) {
		
		Pageable pageableTasks = getPageable(pageSize, pageNum, orderBy, order);
		Page<Task> tasksByOrderPage = tasksService.findAllTasksByOrder(pageableTasks, id);
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
	@PostMapping(path = "/{id}/tasks")
	public ResponseEntity<String> postTask(
		@PathVariable(name = "id") Long id,
		@Validated(PersistenceValidation.class) @RequestBody Task task,
		BindingResult bindingResult) {
		
		super.validateBindingResult(bindingResult);
		Order order = getWorkshopEntitiesService().findById(id);
		task = tasksService.persistEntity(task);
		
		order.setCreatedFor(task);
		getWorkshopEntitiesService().mergeEntity(order);
		Resource<User> userResource = usersResourceAssembler.toResource(task);
		String jsonUserResource = getJsonServiceUtils().workshopEntityObjectsToJson(userResource);
		return ResponseEntity.status(HttpStatus.CREATED).body(jsonUserResource);
	}
	
}
