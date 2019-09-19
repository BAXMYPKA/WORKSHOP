package workshop.internal.controllers;

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
import org.springframework.validation.BindingResult;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import workshop.internal.entities.*;
import workshop.internal.entities.hibernateValidation.PersistenceValidation;
import workshop.internal.entities.hibernateValidation.UpdateValidation;
import workshop.internal.exceptions.EntityNotFoundException;
import workshop.internal.hateoasResources.DepartmentsResourceAssembler;
import workshop.internal.hateoasResources.EmployeesResourceAssembler;
import workshop.internal.hateoasResources.InternalAuthoritiesResourceAssembler;
import workshop.internal.hateoasResources.PositionsResourceAssembler;
import workshop.internal.services.DepartmentsService;
import workshop.internal.services.EmployeesService;
import workshop.internal.services.InternalAuthoritiesService;
import workshop.internal.services.PositionsService;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashSet;

@RestController
@RequestMapping(path = "/internal/positions", produces = {MediaTypes.HAL_JSON_UTF8_VALUE})
@ExposesResourceFor(Position.class)
public class PositionsController extends WorkshopControllerAbstract<Position> {
	
	public static final String GET_POSITION_EMPLOYEES_METHOD_NAME = "getPositionEmployees";
	public static final String GET_POSITION_INTERNAL_AUTHORITIES_METHOD_NAME = "getPositionInternalAuthorities";
	
	@Autowired
	private SmartValidator smartValidator;
	@Autowired
	private DepartmentsService departmentsService;
	@Autowired
	private EmployeesService employeesService;
	@Autowired
	private InternalAuthoritiesService internalAuthoritiesService;
	@Autowired
	private DepartmentsResourceAssembler departmentsResourceAssembler;
	@Autowired
	private EmployeesResourceAssembler employeesResourceAssembler;
	@Autowired
	private InternalAuthoritiesResourceAssembler internalAuthoritiesResourceAssembler;
	
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
					method = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE},
					consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
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
			employeesResourceAssembler.toPagedSubResources(employeesByPositionPage, id, GET_POSITION_EMPLOYEES_METHOD_NAME);
		String jsonPagedEmployeesResources = getJsonServiceUtils().workshopEntityObjectsToJson(positionEmployeesPagedResources);
		return ResponseEntity.ok(jsonPagedEmployeesResources);
	}
	
	/**
	 * @param id       Position.ID to set a new Employee to.
	 * @param employee Employee to be set to this Position
	 * @return The persisted Employee with this Position set.
	 */
	@PostMapping(path = "/{id}/employees",
				 consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
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
	@PutMapping(path = "/{id}/employees",
				consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
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
	
	@GetMapping(path = "/{id}/internal_-uthorities")
	public ResponseEntity<String> getPositionInternalAuthorities(
		@PathVariable(name = "id") Long id,
		@RequestParam(value = "pageSize", required = false, defaultValue = "${page.size.default}") Integer pageSize,
		@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
		@RequestParam(name = "order-by", required = false, defaultValue = "${default.orderBy}") String orderBy,
		@RequestParam(name = "order", required = false, defaultValue = "${default.order}") String order) {
		
		Pageable pageable = super.getPageable(pageSize, pageNum, orderBy, order);
		Page<InternalAuthority> internalAuthoritiesByPositionPage =
			internalAuthoritiesService.findInternalAuthoritiesByPosition(pageable, id);
		Resources<Resource<InternalAuthority>> positionInternalAuthoritiesResources =
			internalAuthoritiesResourceAssembler.toPagedSubResources(
				internalAuthoritiesByPositionPage, id, GET_POSITION_INTERNAL_AUTHORITIES_METHOD_NAME);
		String jsonPositionInternalAuthoritiesResources =
			getJsonServiceUtils().workshopEntityObjectsToJson(positionInternalAuthoritiesResources);
		return ResponseEntity.ok(jsonPositionInternalAuthoritiesResources);
	}
	
	/**
	 * New InternalAuthorities have to be persisted with their dedicated controller!
	 *
	 * @return HttpStatus.FORBIDDEN with a message advises using {@link InternalAuthoritiesController#postOne(WorkshopEntity, BindingResult)}
	 * post method for persisting.
	 */
	@PostMapping(path = "/{id}/internal-authorities",
				 consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	public ResponseEntity<String> postForbiddenMethodPositionInternalAuthority(
		@PathVariable(name = "id") Long id,
		@Validated(PersistenceValidation.class) @RequestBody InternalAuthority internalAuthority,
		BindingResult bindingResult,
		HttpServletRequest request) {
		
		String errorMessage = getMessageSource().getMessage(
			"httpStatus.forbidden.withDescription(2)",
			new Object[]{request.getMethod() + " HttpMethod", " Use direct Link from InternalAuthority instead to do it!"},
			LocaleContextHolder.getLocale());
		return getResponseEntityWithErrorMessage(HttpStatus.FORBIDDEN, errorMessage);
	}
	
	/**
	 * Adds another existing InternalAuthority to the given Position
	 *
	 * @param id                Position.ID to add a new InternalAuthority to.
	 * @param internalAuthority An existing InternalAuthority to be added.
	 * @return HttpStatus.ACCEPTED with the renewed InternalAuthority.
	 */
	@PutMapping(path = "/{id}/internal-authorities",
				consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	public ResponseEntity<String> putPositionInternalAuthority(
		@PathVariable(name = "id") Long id,
		@Validated(UpdateValidation.class) @RequestBody InternalAuthority internalAuthority,
		BindingResult bindingResult) {
		super.validateBindingResult(bindingResult);
		Position position = getWorkshopEntitiesService().findById(id);
		internalAuthority = internalAuthoritiesService.findById(internalAuthority.getIdentifier());
		position.addInternalAuthority(internalAuthority);
		getWorkshopEntitiesService().mergeEntity(position);
		
		if (internalAuthority.getPositions() == null) {
			internalAuthority.setPositions(new HashSet<>(Collections.singletonList(position)));
		} else {
			internalAuthority.getPositions().add(position);
		}
		Resource<InternalAuthority> internalAuthorityResource =
			internalAuthoritiesResourceAssembler.toResource(internalAuthority);
		String jsonInternalAuthorityResource =
			getJsonServiceUtils().workshopEntityObjectsToJson(internalAuthorityResource);
		return ResponseEntity.accepted().body(jsonInternalAuthorityResource);
	}
	
	/**
	 * Just removes an InternalAuthority from a given Position.
	 *
	 * @param id          Position.ID
	 * @param authorityId Authority.ID to be deleted from Position
	 * @return HttpStatus.NO_CONTENT in case of success.
	 */
	@DeleteMapping(path = "{id}/internal-authorities/{authorityId}")
	public ResponseEntity<String> deletePositionInternalAuthority(
		@PathVariable(name = "id") Long id,
		@PathVariable(name = "authorityId") Long authorityId) {
		Position position = getWorkshopEntitiesService().findById(id);
		InternalAuthority internalAuthority = position.getInternalAuthorities().stream()
			.filter(authority -> authority.getIdentifier().equals(authorityId))
			.findFirst()
			.orElseThrow(() -> new EntityNotFoundException(
				"No InternalAuthority for a given Position found!",
				HttpStatus.NOT_FOUND,
				getMessageSource().getMessage(
					"httpStatus.notFound(2)",
					new Object[]{"InternalAuthority.ID=" + authorityId, getWorkshopEntityClassName() + ".ID=" + id},
					LocaleContextHolder.getLocale())));
		position.getInternalAuthorities().remove(internalAuthority);
		getWorkshopEntitiesService().mergeEntity(position);
		return ResponseEntity.noContent().build();
	}
}
