package internal.controllers;

import internal.entities.*;
import internal.entities.hibernateValidation.PersistenceValidation;
import internal.entities.hibernateValidation.UpdateValidation;
import internal.exceptions.InternalServerErrorException;
import internal.hateoasResources.*;
import internal.services.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

import javax.validation.Valid;

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
		@PathVariable(name = "id") long id,
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
		@PathVariable(name = "id") long id,
		@Validated(PersistenceValidation.class) @RequestBody Position position,
		BindingResult bindingResult) {
		return postPosition(id, position, bindingResult);
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
			tasksResourceAssembler.toPagedSubResources(tasksAppointedToEmployeePage, id, APPOINTED_TASKS_METHOD_NAME);
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
			tasksResourceAssembler.toPagedSubResources(tasksModifiedByEmployeePage, id, TASKS_MODIFIED_BY_METHOD_NAME);
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
			tasksResourceAssembler.toPagedSubResources(tasksCreatedByEmployee, id, TASKS_CREATED_BY_METHOD_NAME);
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
		Page<Order> ordersModifiedByEmployeePage = ordersService.findAllOrdersModifiedByEmployee(pageable, id);
		Resources<Resource<Order>> ordersModifiedByResources =
			ordersResourceAssembler.toPagedSubResources(ordersModifiedByEmployeePage, id, ORDERS_MODIFIED_BY_METHOD_NAME);
		String jsonOrdersModifiedByResources = getJsonServiceUtils().workshopEntityObjectsToJson(ordersModifiedByResources);
		return ResponseEntity.ok(jsonOrdersModifiedByResources);
	}
	
	@GetMapping(path = "/{id}/orders_created_by")
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
