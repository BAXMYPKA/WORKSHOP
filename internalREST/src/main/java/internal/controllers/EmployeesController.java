package internal.controllers;

import internal.entities.Employee;
import internal.entities.Phone;
import internal.entities.Position;
import internal.entities.hibernateValidation.MergingValidation;
import internal.entities.hibernateValidation.PersistenceValidation;
import internal.hateoasResources.EmployeesResourceAssembler;
import internal.hateoasResources.PhonesResourceAssembler;
import internal.hateoasResources.PositionsResourceAssembler;
import internal.services.EmployeesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(path = "/internal/employees", produces = {MediaTypes.HAL_JSON_UTF8_VALUE})
@ExposesResourceFor(Employee.class)
public class EmployeesController extends WorkshopControllerAbstract<Employee> {
	
	@Autowired
	private PhonesResourceAssembler phonesResourceAssembler;
	@Autowired
	private PositionsResourceAssembler positionsResourceAssembler;
	
	/**
	 * @param employeesService By this instance we set the concrete instance of WorkshopServiceAbstract
	 *                         and through it set the concrete type of WorkshopEntity as {@link #getWorkshopEntityClass()}
	 *                         to operate with.
	 */
	public EmployeesController(EmployeesService employeesService, EmployeesResourceAssembler employeesResourceAssembler) {
		super(employeesService, employeesResourceAssembler);
	}
	
	@Override
	@GetMapping
	public ResponseEntity<String> getAll(
		@RequestParam(value = "pageSize", required = false, defaultValue = "${page.size.default}") Integer pageSize,
		@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
		@RequestParam(name = "order-by", required = false, defaultValue = "${default.orderBy}") String orderBy,
		@RequestParam(name = "order", required = false, defaultValue = "${default.order}") String order) {
		
		Pageable pageRequest = getPageable(pageSize, pageNum, orderBy, order);
		Page<Employee> entitiesPage = getWorkshopEntitiesService().findAllEntities(pageRequest);
		
		Resources<Resource<Employee>> entitiesPageResources = getWorkshopEntityResourceAssembler().toPagedResources(entitiesPage);
		//Add to every Resource<Employee> additional Links to its every Phone
		entitiesPageResources.getContent().stream()
			.filter(employeeResource -> employeeResource.getContent().getPhones() != null)
			.forEach(employeeResource -> {
				List<Link> selfPhonesLinks = employeeResource.getContent().getPhones()
					.stream()
					.map(phone -> phonesResourceAssembler.toResource(phone).getLink("self"))
					.collect(Collectors.toList());
				employeeResource.add(selfPhonesLinks);
			});
		
		String pagedResourcesToJson = getJsonServiceUtils().workshopEntityObjectsToJson(entitiesPageResources);
		log.debug("{}s Page with pageNumber={} and pageSize={} has been written as JSON",
			getWorkshopEntityClassName(), entitiesPage.getNumber(), entitiesPage.getSize());
		return new ResponseEntity<>(pagedResourcesToJson, HttpStatus.OK);
	}
	
	@Override
	@GetMapping(path = "/{id}")
	public ResponseEntity<String> getOne(@PathVariable(name = "id") long id) {
		Employee employee = getWorkshopEntitiesService().findById(id);
		
		Resource<Employee> employeeResource = getWorkshopEntityResourceAssembler().toResource(employee);
		//Add Links to the every Phone of Employee
		if (employee.getPhones() != null) {
			List<Link> phonesSerfLinks = employee.getPhones()
				.stream()
				.map(phone -> phonesResourceAssembler.toResource(phone).getLink("self"))
				.collect(Collectors.toList());
			employeeResource.add(phonesSerfLinks);
		}
		String jsonEmployeeResource = getJsonServiceUtils().workshopEntityObjectsToJson(employeeResource);
		return ResponseEntity.ok(jsonEmployeeResource);
	}
	
	@GetMapping(path = "/{id}/position")
	public ResponseEntity<String> getPosition(@PathVariable("id") Long id) {
		Employee employeeById = getWorkshopEntitiesService().findById(id);
		Position employeePosition = employeeById.getPosition();
		Resource<Position> positionResource = positionsResourceAssembler.toResource(employeePosition);
		String jsonPositionResource = getJsonServiceUtils().workshopEntityObjectsToJson(positionResource);
		return ResponseEntity.ok(jsonPositionResource);
	}
}
