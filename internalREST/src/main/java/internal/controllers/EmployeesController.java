package internal.controllers;

import internal.entities.Employee;
import internal.entities.Position;
import internal.entities.Task;
import internal.hateoasResources.EmployeesResourceAssembler;
import internal.hateoasResources.PositionsResourceAssembler;
import internal.hateoasResources.TasksResourceAssembler;
import internal.services.EmployeesService;
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
	
	public static final String GET_APPOINTED_TASKS_METHOD_NAME = "getAppointedTasks";
	public static final String GET_TASKS_MODIFIED_BY_METHOD_NAME = "getTasksModifiedBy";
	public static final String GET_TASKS_CREATED_BY_METHOD_NAME = "getTasksCreatedBy";
	public static final String GET_ORDERS_MODIFIED_BY_METHOD_NAME = "getOrdersModifiedBy";
	
	@Autowired
	private PositionsResourceAssembler positionsResourceAssembler;
	@Autowired
	private TasksResourceAssembler tasksResourceAssembler;
	@Autowired
	private TasksService tasksService;
	
	/**
	 * @param employeesService By this instance we set the concrete instance of WorkshopServiceAbstract
	 *                         and through it set the concrete type of WorkshopEntity as {@link #getWorkshopEntityClass()}
	 *                         to operate with.
	 */
	public EmployeesController(EmployeesService employeesService, EmployeesResourceAssembler employeesResourceAssembler) {
		super(employeesService, employeesResourceAssembler);
	}
	
	//TODO: to include all the following URIs to the every Resource<Employee>
	
	@GetMapping(path = "/{id}/position")
	public ResponseEntity<String> getPosition(@PathVariable("id") Long id) {
		Employee employeeById = getWorkshopEntitiesService().findById(id);
		Position employeePosition = employeeById.getPosition();
		Resource<Position> positionResource = positionsResourceAssembler.toResource(employeePosition);
		String jsonPositionResource = getJsonServiceUtils().workshopEntityObjectsToJson(positionResource);
		return ResponseEntity.ok(jsonPositionResource);
	}
	
	@GetMapping(path = "/{id}/appointed_tasks")
	public ResponseEntity<String> getAppointedTasks(
		@PathVariable(name = "id") Long id,
		@RequestParam(value = "pageSize", required = false, defaultValue = "${page.size.default}") Integer pageSize,
		@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
		@RequestParam(name = "order-by", required = false, defaultValue = "${default.orderBy}") String orderBy,
		@RequestParam(name = "order", required = false, defaultValue = "${default.order}") String order) {
		
		Pageable pageable = super.getPageable(pageSize, pageNum, orderBy, order);
		Page<Task> tasksAppointedToEmployeePage = tasksService.findAllTasksAppointedToEmployee(pageable, id);
		Resources<Resource<Task>> employeeAppointedTasksResources =
			tasksResourceAssembler.toPagedSubResources(tasksAppointedToEmployeePage, id, GET_APPOINTED_TASKS_METHOD_NAME);
		String jsonEmployeeAppointedTasksResources =
			getJsonServiceUtils().workshopEntityObjectsToJson(employeeAppointedTasksResources);
		return ResponseEntity.ok(jsonEmployeeAppointedTasksResources);
	}
	
	@GetMapping(path = "/{id}/tasks_modified_by")
	public ResponseEntity<String> getTasksModifiedBy(
		@PathVariable(name = "id") Long id,
		@RequestParam(value = "pageSize", required = false, defaultValue = "${page.size.default}") Integer pageSize,
		@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
		@RequestParam(name = "order-by", required = false, defaultValue = "${default.orderBy}") String orderBy,
		@RequestParam(name = "order", required = false, defaultValue = "${default.order}") String order) {
		
		Pageable pageable = super.getPageable(pageSize, pageNum, orderBy, order);
		Page<Task> tasksModifiedByEmployeePage = tasksService.findAllTasksModifiedByEmployee(pageable, id);
		Resources<Resource<Task>> tasksModifiedPagedResources =
			tasksResourceAssembler.toPagedSubResources(tasksModifiedByEmployeePage, id, GET_TASKS_MODIFIED_BY_METHOD_NAME);
		String jsonPagedResources = getJsonServiceUtils().workshopEntityObjectsToJson(tasksModifiedPagedResources);
		return ResponseEntity.ok(jsonPagedResources);
	}
	
	@GetMapping(path = "/{id}/tasks_created_by")
	public ResponseEntity<String> getTasksCreatedBy(
		@PathVariable(name = "id") Long id,
		@RequestParam(value = "pageSize", required = false, defaultValue = "${page.size.default}") Integer pageSize,
		@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
		@RequestParam(name = "order-by", required = false, defaultValue = "${default.orderBy}") String orderBy,
		@RequestParam(name = "order", required = false, defaultValue = "${default.order}") String order) {
		
		Pageable pageable = super.getPageable(pageSize, pageNum, orderBy, order);
		Page<Task> tasksCreatedByEmployee = tasksService.findAllTasksCreatedByEmployee(pageable, id);
		Resources<Resource<Task>> tasksCreatedByResources =
			tasksResourceAssembler.toPagedSubResources(tasksCreatedByEmployee, id, GET_TASKS_CREATED_BY_METHOD_NAME);
		String jsonTasksCreatedBy = getJsonServiceUtils().workshopEntityObjectsToJson(tasksCreatedByResources);
		return ResponseEntity.ok(jsonTasksCreatedBy);
	}
	
	@GetMapping(path = "/{id}/orders_modified_by")
	public ResponseEntity<String> getOrdersModifiedBy(
		@PathVariable(name = "id") Long id,
		@RequestParam(value = "pageSize", required = false, defaultValue = "${page.size.default}") Integer pageSize,
		@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
		@RequestParam(name = "order-by", required = false, defaultValue = "${default.orderBy}") String orderBy,
		@RequestParam(name = "order", required = false, defaultValue = "${default.order}") String order) {
		
		Pageable pageable = super.getPageable(pageSize, pageNum, orderBy, order);
		//TODO: to complete
		return null;
	}
	
	@GetMapping(path = "/{id}/orders_created_by")
	public ResponseEntity<String> getOrdersCreatedBy(
		@PathVariable(name = "id") Long id,
		@RequestParam(value = "pageSize", required = false, defaultValue = "${page.size.default}") Integer pageSize,
		@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
		@RequestParam(name = "order-by", required = false, defaultValue = "${default.orderBy}") String orderBy,
		@RequestParam(name = "order", required = false, defaultValue = "${default.order}") String order) {
		return null;
	}
	
	
}
