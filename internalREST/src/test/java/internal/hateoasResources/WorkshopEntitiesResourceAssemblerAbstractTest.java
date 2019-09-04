package internal.hateoasResources;

import internal.controllers.EmployeesController;
import internal.dao.EmployeesDao;
import internal.entities.Department;
import internal.entities.Employee;
import internal.entities.Phone;
import internal.entities.Position;
import internal.http.ResponseHeadersInternalFilter;
import internal.services.EmployeesService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;

import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@ExtendWith(MockitoExtension.class)
class WorkshopEntitiesResourceAssemblerAbstractTest {
	
	@Mock
	EmployeesService employeesService;
	@Mock
	EmployeesDao employeesDao;
	@Mock
	EntityLinks entityLinks;
	EmployeesResourceAssembler employeesResourceAssembler;
	EmployeesController employeesController;
	ResponseHeadersInternalFilter responseHeadersInternalFilter;
	
	@BeforeEach
	public void beforeEach() {
		Mockito.when(employeesService.getEntityClass()).thenReturn(Employee.class);
		employeesService.setEntityClassSimpleName(Employee.class.getSimpleName());
		employeesService.setWorkshopEntitiesDaoAbstract(employeesDao);
		employeesResourceAssembler = new EmployeesResourceAssembler();
		employeesResourceAssembler.setEntityLinks(entityLinks);
		employeesController = new EmployeesController(employeesService, employeesResourceAssembler);
		responseHeadersInternalFilter = new ResponseHeadersInternalFilter();
	}
	
	@Test
	public void getMapped_Controller_Methods_Should_Be_Converted_to_SubResources_Links() {
		//GIVEN
		Department department = new Department("Department 1");
		department.setIdentifier(1L);
		
		Position position = new Position("Position 1", department);
		position.setIdentifier(2);
		
		
		Employee employee = new Employee(
			"FN", "LN", "12345", "employee@workshop.pro", LocalDate.now().minusYears(55), position);
		employee.setIdentifier(3);
		
		Mockito.when(entityLinks.linkForSingleResource(Employee.class, employee.getIdentifier()))
			.thenReturn(ControllerLinkBuilder.linkTo(EmployeesController.class));
		
		//WHEN
		Resource<Employee> employeeResource = employeesResourceAssembler.toResource(employee);
		
		//THEN
		log.info(employeeResource.toString());
		
		assertTrue(employeeResource.hasLink("self"));
		
		assertTrue(employeeResource.hasLink("position"));
		assertTrue(employeeResource.getLink("position").getHref().contains("/3/position"));
		assertTrue(employeeResource.getLink("position").getTitle().contains("[/{id}/position]"));
		
		assertTrue(employeeResource.hasLink("phones"));
		assertTrue(employeeResource.getLink("phones").getHref().contains("/3/phones"));
		assertTrue(employeeResource.getLink("phones").getTitle().contains("[/{id}/phones]"));
		
		assertTrue(employeeResource.hasLink("appointedTasks"));
		assertTrue(employeeResource.getLink("appointedTasks").getHref().contains("/3/appointed_tasks"));
		assertTrue(employeeResource.getLink("appointedTasks").getTitle().contains("[/{id}/appointed_tasks]"));
		
		assertTrue(employeeResource.hasLink("ordersCreatedBy"));
		assertTrue(employeeResource.getLink("ordersCreatedBy").getHref().contains("/3/orders_created_by"));
		assertTrue(employeeResource.getLink("ordersCreatedBy").getTitle().contains("[/{id}/orders_created_by]"));
	}
	
	
}