package internal.controllers;

import internal.entities.Department;
import internal.entities.Position;
import internal.entities.hateoasResources.DepartmentResourceAssembler;
import internal.entities.hateoasResources.EmployeeResourceAssembler;
import internal.services.DepartmentsService;
import internal.services.PositionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/internal/positions", produces = {MediaTypes.HAL_JSON_UTF8_VALUE})
@ExposesResourceFor(Position.class)
public class PositionsController extends WorkshopControllerAbstract<Position> {
	
	@Autowired
	private DepartmentsService departmentsService;
	@Autowired
	private DepartmentResourceAssembler departmentResourceAssembler;
	@Autowired
	private EmployeeResourceAssembler employeeResourceAssembler;
	
	/**
	 * @param positionsService By this instance we set the concrete instance of WorkshopServiceAbstract
	 *                         and through it set the concrete type of WorkshopEntity as {@link #getWorkshopEntityClass()}
	 *                         to operate with.
	 */
	public PositionsController(PositionsService positionsService) {
		super(positionsService);
	}
	
	@GetMapping(path = "/{id}/department")
	public ResponseEntity<String> getDepartment(@PathVariable("id") Long id) {
		Department departmentByPosition = departmentsService.findDepartmentByPosition(id);
		Resource<Department> departmentResource = departmentResourceAssembler.toResource(departmentByPosition);
		String jsonDepartmentResource = getJsonServiceUtils().workshopEntityObjectsToJson(departmentResource);
		return ResponseEntity.ok(jsonDepartmentResource);
	}
	
	@GetMapping(path = "/{id}/employees")
	public ResponseEntity<String> getEmployees(
		@PathVariable("id") Long id,
		@RequestParam(value = "pageSize", required = false, defaultValue = "${page.size.default}") Integer pageSize,
		@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
		@RequestParam(name = "order-by", required = false, defaultValue = "${default.orderBy}") String orderBy,
		@RequestParam(name = "order", required = false, defaultValue = "${default.order}") String order) {
		
		Pageable pageableEmployees = super.getPageable(pageSize, pageNum, orderBy, order);
		super.
		
//		getWorkshopEntitiesService().
		
		return null;
	}
}
