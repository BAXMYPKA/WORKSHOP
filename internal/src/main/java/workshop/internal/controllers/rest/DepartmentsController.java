package workshop.internal.controllers.rest;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import workshop.internal.controllers.WorkshopControllerAbstract;
import workshop.internal.entities.Department;
import workshop.internal.entities.Position;
import workshop.internal.entities.hibernateValidation.PersistenceValidation;
import workshop.internal.entities.hibernateValidation.MergingValidation;
import workshop.internal.exceptions.EntityNotFoundException;
import workshop.internal.hateoasResources.DepartmentsResourceAssembler;
import workshop.internal.hateoasResources.PositionsResourceAssembler;
import workshop.internal.hateoasResources.WorkshopEntitiesResourceAssemblerAbstract;
import workshop.internal.services.DepartmentsService;
import workshop.internal.services.PositionsService;
import workshop.internal.services.WorkshopEntitiesServiceAbstract;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/internal/departments", produces = {MediaTypes.HAL_JSON_UTF8_VALUE})
@ExposesResourceFor(Department.class)
public class DepartmentsController extends WorkshopControllerAbstract<Department> {
	
	public static final String GET_POSITIONS_METHOD_NAME = "getPositions";
	
	@Autowired
	private PositionsResourceAssembler positionsResourceAssembler;
	
	@Autowired
	private PositionsService positionsService;
	
	/**
	 * @see WorkshopControllerAbstract#WorkshopControllerAbstract(WorkshopEntitiesServiceAbstract, WorkshopEntitiesResourceAssemblerAbstract) )
	 */
	public DepartmentsController(DepartmentsService departmentsService, DepartmentsResourceAssembler departmentsResourceAssembler) {
		super(departmentsService, departmentsResourceAssembler);
	}
	
	/**
	 * @param id       Department id
	 * @param pageSize
	 * @param pageNum
	 * @param orderBy
	 * @param order
	 * @return
	 */
	@GetMapping(path = "/{id}/positions")
	@PreAuthorize("hasPermission('Position', 'get')")
	public ResponseEntity<String> getPositions(
		@PathVariable(name = "id") long id,
		@RequestParam(value = "pageSize", required = false, defaultValue = "${page.size.default}") Integer pageSize,
		@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
		@RequestParam(name = "order-by", required = false, defaultValue = "${default.orderBy}") String orderBy,
		@RequestParam(name = "order", required = false, defaultValue = "${default.order}") String order) {
		
		if (!getWorkshopEntitiesService().isExist(id)) {
			return new ResponseEntity<>(getMessageSource().getMessage(
				"httpStatus.notAcceptable.identifier(1)", new Object[]{id}, LocaleContextHolder.getLocale()),
				HttpStatus.NOT_FOUND);
		}
		Pageable pageablePositions = super.getPageable(pageSize, pageNum, orderBy, order);
		Page<Position> positionsByDepartmentPage = positionsService.findPositionsByDepartment(pageablePositions, id);
		Resources<Resource<Position>> departmentPositionsPagedResources =
			positionsResourceAssembler.toPagedSubResources(positionsByDepartmentPage, id, GET_POSITIONS_METHOD_NAME);
		String jsonDepartmentPositionsResources =
			getJsonServiceUtils().workshopEntityObjectsToJson(departmentPositionsPagedResources);
		
		return ResponseEntity.ok(jsonDepartmentPositionsResources);
	}
	
	/**
	 * Sets this Department to the given Position and persist it as new.
	 *
	 * @param id       ID of this Department
	 * @param position New Position to be saved and to which this Department will be set.
	 */
	@PostMapping(path = "/{id}/positions",
				 consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	@PreAuthorize("hasPermission('Position', 'post')")
	public ResponseEntity<String> postPosition(@PathVariable(name = "id") long id,
		@Validated(PersistenceValidation.class) @RequestBody Position position,
		BindingResult bindingResult) {
		
		super.validateBindingResult(bindingResult);
		Department department = getWorkshopEntitiesService().findById(id);
		position.setDepartment(department);
		positionsService.persistEntity(position);
		Resource<Position> positionResource = positionsResourceAssembler.toResource(position);
		String jsonPositionResource = getJsonServiceUtils().workshopEntityObjectsToJson(positionResource);
		return ResponseEntity.ok(jsonPositionResource);
	}
	
	@PutMapping(path = "/{id}/positions",
				consumes = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
	@PreAuthorize("hasPermission('Position', 'put')")
	public ResponseEntity<String> putPosition(@PathVariable(name = "id") long id,
		@Validated(MergingValidation.class) @RequestBody Position position,
		BindingResult bindingResult) {
		
		super.validateBindingResult(bindingResult);
		Position updatedPosition = positionsService.updatePositionDepartment(position, id);
		Resource<Position> positionResource = positionsResourceAssembler.toResource(updatedPosition);
		String jsonPositionResource = getJsonServiceUtils().workshopEntityObjectsToJson(positionResource);
		return ResponseEntity.ok(jsonPositionResource);
	}
	
	/**
	 * This method deletes the given Position itself as Position entity cannot exist without Department.
	 *
	 * @param id         The ID of the Department to derive Position from.
	 * @param positionId The ID of the Position to be deleted
	 */
	@DeleteMapping(path = "/{id}/positions/{positionId}")
	@PreAuthorize("hasPermission('Position', 'delete')")
	public ResponseEntity<String> deletePosition(@PathVariable(name = "id") long id,
		@PathVariable(name = "positionId") Long positionId) {
		
		Department department = getWorkshopEntitiesService().findById(id);
		if (department.getPositions().stream().noneMatch(position -> position.getIdentifier().equals(positionId))) {
			throw new EntityNotFoundException("No Position.id=" + positionId + " in the Department.id=" + id,
				HttpStatus.NOT_FOUND, getMessageSource().getMessage(
				"httpStatus.notFound(2)",
				new Object[]{"Position.ID=" + positionId, "Department.ID=" + id},
				LocaleContextHolder.getLocale()));
		}
		positionsService.removeEntity(positionId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT)
			.body(getDeleteMessageSuccessLocalized("Position.ID=" + positionId));
	}
	
	
	//TODO: TO get Employees from the Departments
}
