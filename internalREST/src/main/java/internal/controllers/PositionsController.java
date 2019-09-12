package internal.controllers;

import internal.entities.Department;
import internal.entities.Employee;
import internal.entities.Position;
import internal.entities.hibernateValidation.PersistenceValidation;
import internal.entities.hibernateValidation.UpdateValidation;
import internal.hateoasResources.DepartmentsResourceAssembler;
import internal.hateoasResources.EmployeesResourceAssembler;
import internal.hateoasResources.PositionsResourceAssembler;
import internal.services.DepartmentsService;
import internal.services.EmployeesService;
import internal.services.PositionsService;
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
import org.springframework.validation.BindingResult;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(path = "/internal/positions", produces = {MediaTypes.HAL_JSON_UTF8_VALUE})
@ExposesResourceFor(Position.class)
public class PositionsController extends WorkshopControllerAbstract<Position> {
	
	public static final String GET_EMPLOYEES_METHOD_NAME = "getEmployees";
	
	@Autowired
	private SmartValidator smartValidator;
	@Autowired
	private DepartmentsService departmentsService;
	@Autowired
	private EmployeesService employeesService;
	@Autowired
	private DepartmentsResourceAssembler departmentsResourceAssembler;
	@Autowired
	private EmployeesResourceAssembler employeesResourceAssembler;
	
	/**
	 * @param positionsService By this instance we set the concrete instance of WorkshopServiceAbstract
	 *                         and through it set the concrete type of WorkshopEntity as {@link #getWorkshopEntityClass()}
	 *                         to operate with.
	 */
	public PositionsController(PositionsService positionsService, PositionsResourceAssembler positionsResourceAssembler) {
		super(positionsService, positionsResourceAssembler);
	}
	
	@GetMapping(path = "/{id}/department")
	public ResponseEntity<String> getPositionDepartment(@PathVariable("id") Long id) {
		
		Department departmentByPosition = departmentsService.findDepartmentByPosition(id);
		Resource<Department> departmentResource = departmentsResourceAssembler.toResource(departmentByPosition);
		String jsonDepartmentResource = getJsonServiceUtils().workshopEntityObjectsToJson(departmentResource);
		return ResponseEntity.ok(jsonDepartmentResource);
	}
	
	@RequestMapping(path = {"/{id}/department/{departmentId}", "/{id}/department/"},
		method = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
	public ResponseEntity<String> positionDepartmentForbiddenMethods(
		@PathVariable(name = "id") Long id,
		@PathVariable(name = "departmentId", required = false) Long departmentId,
		@RequestBody(required = false) Department department,
		HttpServletRequest request) {
		
		return getResponseEntityWithErrorMessage(
			HttpStatus.FORBIDDEN,
			getMessageSource().getMessage(
				"httpStatus.forbidden.withDescription(2)",
				new Object[]{request.getMethod(), " Please, use strict Department link for such purposes!"},
				LocaleContextHolder.getLocale()));
	}
	
	@GetMapping(path = "/{id}/employees")
	public ResponseEntity<String> getPositionEmployees(
		@PathVariable("id") Long id,
		@RequestParam(value = "pageSize", required = false, defaultValue = "${page.size.default}") Integer pageSize,
		@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
		@RequestParam(name = "order-by", required = false, defaultValue = "${default.orderBy}") String orderBy,
		@RequestParam(name = "order", required = false, defaultValue = "${default.order}") String order) {
		
		Pageable pageableEmployees = super.getPageable(pageSize, pageNum, orderBy, order);
		Page<Employee> employeesByPositionPage = employeesService.findEmployeesByPosition(pageableEmployees, id);
		Resources<Resource<Employee>> positionEmployeesPagedResources =
			employeesResourceAssembler.toPagedSubResources(employeesByPositionPage, id, GET_EMPLOYEES_METHOD_NAME);
		String jsonPagedEmployeesResources = getJsonServiceUtils().workshopEntityObjectsToJson(positionEmployeesPagedResources);
		return ResponseEntity.ok(jsonPagedEmployeesResources);
	}
	
	/**
	 * @param id       Position.ID to set a new Employee to.
	 * @param employee Employee to be set to this Position
	 * @return The persisted Employee with this Position set.
	 */
	@PostMapping(path = "/{id}/employees")
	public ResponseEntity<String> postPositionEmployee(@PathVariable(name = "id") Long id,
													   @RequestBody Employee employee,
													   BindingResult bindingResult) {
		
		Position position = getWorkshopEntitiesService().findById(id);
		employee.setPosition(position);
		smartValidator.validate(employee, bindingResult, PersistenceValidation.class);
		super.validateBindingResult(bindingResult);
		employeesService.persistEntity(employee);
		Resource<Employee> employeeResource = employeesResourceAssembler.toResource(employee);
		String jsonEmployeeResource = getJsonServiceUtils().workshopEntityObjectsToJson(employeeResource);
		return ResponseEntity.status(HttpStatus.CREATED).body(jsonEmployeeResource);
	}
	
	/**
	 * @param id       Another Position.ID to set an existing Employee to.
	 * @param employee Employee to be reset to this Position
	 * @return The updated Employee with this Position set.
	 */
	@PutMapping(path = "/{id}/employees")
	public ResponseEntity<String> putPositionEmployee(@PathVariable(name = "id") Long id,
													  @Validated(UpdateValidation.class) @RequestBody Employee employee,
													  BindingResult bindingResult) {
		super.validateBindingResult(bindingResult);
		Position position = getWorkshopEntitiesService().findById(id);
		employee.setPosition(position);
		employeesService.mergeEntity(employee);
		Resource<Employee> employeeResource = employeesResourceAssembler.toResource(employee);
		String jsonEmployeeResource = getJsonServiceUtils().workshopEntityObjectsToJson(employeeResource);
		return ResponseEntity.status(HttpStatus.CREATED).body(jsonEmployeeResource);
	}
	
	/**
	 * The Employee entity cannot exist without Position set. So it cannot be just removed from Employee.
	 *
	 * @return HttpStatus.FORBIDDEN with a hint to use Post or Put methods instead to replace a Position.
	 */
	@DeleteMapping(path = "/{id}/employees/{employeeId}")
	public ResponseEntity<String> deletePositionEmployeeForbidden(@PathVariable(name = "id") Long id,
																  @PathVariable(name = "employeeId") Long employeeId) {
		return getResponseEntityWithErrorMessage(
			HttpStatus.FORBIDDEN,
			getMessageSource().getMessage(
				"httpStatus.forbidden.removeForbidden(2)",
				new Object[]{"Position from Employee",
					" Employee cannot be left without a Position! Please, use Put method for replacing Position!"},
				LocaleContextHolder.getLocale()));
	}
}
