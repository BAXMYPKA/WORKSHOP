package internal.controllers;

import internal.entities.Department;
import internal.entities.Position;
import internal.hateoasResources.DepartmentsResourceAssembler;
import internal.hateoasResources.PositionsResourceAssembler;
import internal.hateoasResources.WorkshopEntitiesResourceAssemblerAbstract;
import internal.services.DepartmentsService;
import internal.services.PositionsService;
import internal.services.WorkshopEntitiesServiceAbstract;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/internal/departments", produces = {MediaTypes.HAL_JSON_UTF8_VALUE})
@ExposesResourceFor(Department.class)
public class DepartmentsController extends WorkshopControllerAbstract<Department> {
	
	public static final String POSITIONS_METHOD_NAME = "positions";
	
	@Autowired
	private DepartmentsResourceAssembler departmentsResourceAssembler;
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
	public ResponseEntity<String> positions(
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
			positionsResourceAssembler.toPagedSubResources(positionsByDepartmentPage, id, POSITIONS_METHOD_NAME);
		String jsonDepartmentPositionsResources =
			getJsonServiceUtils().workshopEntityObjectsToJson(departmentPositionsPagedResources);
		
		return ResponseEntity.ok(jsonDepartmentPositionsResources);
	}
	
	//TODO: TO get Employees from the Departments
}
