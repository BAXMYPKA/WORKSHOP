package internal.controllers;

import internal.entities.Order;
import internal.entities.Task;
import internal.entities.User;
import internal.hateoasResources.OrdersResourceAssembler;
import internal.hateoasResources.TasksResourceAssembler;
import internal.hateoasResources.UsersResourceAssembler;
import internal.services.OrdersService;
import internal.services.TasksService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = "/internal/orders", produces = {MediaTypes.HAL_JSON_UTF8_VALUE})
@ExposesResourceFor(Order.class)
public class OrdersController extends WorkshopControllerAbstract<Order> {
	
	@Autowired
	private TasksService tasksService;
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
	public ResponseEntity<String> getUser(@PathVariable("id") Long id) {
		
		User userById = ((OrdersService) getWorkshopEntitiesService()).findUserByOrder(id);
		Resource<User> userResource = usersResourceAssembler.toResource(userById);
		String jsonUser = getJsonServiceUtils().workshopEntityObjectsToJson(userResource);
		
		return ResponseEntity.ok(jsonUser);
	}
	
	/**
	 * @param id OrderID to get Tasks from.
	 */
	@GetMapping(path = "/{id}/tasks")
	public ResponseEntity<String> getTasks(
		@PathVariable("id") Long id,
		@RequestParam(value = "pageSize", required = false, defaultValue = "${page.size.default}") Integer pageSize,
		@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
		@RequestParam(name = "order-by", required = false, defaultValue = "${default.orderBy}") String orderBy,
		@RequestParam(name = "order", required = false, defaultValue = "${default.order}") String order) {
		
		Pageable pageableTasks = getPageable(pageSize, pageNum, orderBy, order);
		
		Page<Task> tasksByOrderPage = tasksService.findAllTasksByOrder(pageableTasks, id);
		
		Resources<Resource<Task>> tasksPagedResources = tasksResourceAssembler.toPagedSubResources(tasksByOrderPage, id);
		
		String jsonTasksPagedResources = getJsonServiceUtils().workshopEntityObjectsToJson(tasksPagedResources);
		
		return ResponseEntity.ok(jsonTasksPagedResources);
	}
	
}
