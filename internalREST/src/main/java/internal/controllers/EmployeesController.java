package internal.controllers;

import internal.entities.*;
import internal.hateoasResources.*;
import internal.services.EmployeesService;
import internal.services.OrdersService;
import internal.services.PhonesService;
import internal.services.TasksService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Getter
@RestController
@RequestMapping(path = "/internal/employees", produces = {MediaTypes.HAL_JSON_UTF8_VALUE})
@ExposesResourceFor(Employee.class)
public class EmployeesController extends WorkshopControllerAbstract<Employee> {
	
	public static final String APPOINTED_TASKS_METHOD_NAME = "appointedTasks";
	public static final String TASKS_MODIFIED_BY_METHOD_NAME = "tasksModifiedBy";
	public static final String TASKS_CREATED_BY_METHOD_NAME = "tasksCreatedBy";
	public static final String ORDERS_MODIFIED_BY_METHOD_NAME = "ordersModifiedBy";
	public static final String ORDERS_CREATED_BY_METHOD_NAME = "ordersCreatedBy";
	public static final String PHONES_METHOD_NAME = "phones";
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
	
	/**
	 * @param employeesService By this instance we set the concrete instance of WorkshopServiceAbstract
	 *                         and through it set the concrete type of WorkshopEntity as {@link #getWorkshopEntityClass()}
	 *                         to operate with.
	 */
	public EmployeesController(EmployeesService employeesService, EmployeesResourceAssembler employeesResourceAssembler) {
		super(employeesService, employeesResourceAssembler);
	}
	
	//TODO: to include all the following URIs to the every Resource<Employee>
	
	@GetMapping(path = "/{id}/phones")
	public ResponseEntity<String> phones(
		@PathVariable("id") Long id,
		@RequestParam(value = "pageSize", required = false, defaultValue = "${page.size.default}") Integer pageSize,
		@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
		@RequestParam(name = "order-by", required = false, defaultValue = "${default.orderBy}") String orderBy,
		@RequestParam(name = "order", required = false, defaultValue = "${default.order}") String order){
		
		Pageable phonesPage = super.getPageable(pageSize, pageNum, orderBy, order);
		Page<Phone> allPhonesByUserPage = phonesService.findAllPhonesByEmployee(phonesPage, id);
		Resources<Resource<Phone>> employeePhonesPagedResources =
			phonesResourceAssembler.toPagedSubResources(allPhonesByUserPage, id, PHONES_METHOD_NAME);
		String jsonEmployeePhonesPagedResources = getJsonServiceUtils().workshopEntityObjectsToJson(employeePhonesPagedResources);
		return ResponseEntity.ok(jsonEmployeePhonesPagedResources);
	}
	
	
	@GetMapping(path = "/{id}/position")
	public ResponseEntity<String> position(@PathVariable("id") Long id) {
		Employee employeeById = getWorkshopEntitiesService().findById(id);
		Position employeePosition = employeeById.getPosition();
		Resource<Position> positionResource = positionsResourceAssembler.toResource(employeePosition);
		String jsonPositionResource = getJsonServiceUtils().workshopEntityObjectsToJson(positionResource);
		return ResponseEntity.ok(jsonPositionResource);
	}
	
	@GetMapping(path = "/{id}/appointed_tasks")
	public ResponseEntity<String> appointedTasks(
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
	
	@GetMapping(path = "/{id}/tasks_modified_by")
	public ResponseEntity<String> tasksModifiedBy(
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
	
	@GetMapping(path = "/{id}/tasks_created_by")
	public ResponseEntity<String> tasksCreatedBy(
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
	
	@GetMapping(path = "/{id}/orders_modified_by")
	public ResponseEntity<String> ordersModifiedBy(
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
	
	@GetMapping(path = "/{id}/orders_created_by")
	public ResponseEntity<String> ordersCreatedBy(
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
