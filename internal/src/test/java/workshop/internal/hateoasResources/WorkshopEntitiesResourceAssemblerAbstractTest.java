package workshop.internal.hateoasResources;

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
import workshop.internal.controllers.EmployeesController;
import workshop.internal.dao.EmployeesDao;
import workshop.internal.entities.Department;
import workshop.internal.entities.Employee;
import workshop.internal.entities.Position;
import workshop.internal.hateoasResources.EmployeesResourceAssembler;
import workshop.internal.services.EmployeesService;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@ExtendWith(MockitoExtension.class)
class WorkshopEntitiesResourceAssemblerAbstractTest {
	
	@Mock
	private EmployeesService employeesService;
	@Mock
	private EmployeesDao employeesDao;
	@Mock
	private EntityLinks entityLinks;
	private EmployeesResourceAssembler employeesResourceAssembler;
	private EmployeesController employeesController;
	
	@BeforeEach
	public void beforeEach() {
		Mockito.when(employeesDao.getEntityClass()).thenReturn(Employee.class);
		employeesService = new EmployeesService(employeesDao);
		employeesResourceAssembler = new EmployeesResourceAssembler();
		employeesResourceAssembler.setEntityLinks(entityLinks);
		employeesController = new EmployeesController(employeesService, employeesResourceAssembler);
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
		
		assertTrue(employeeResource.hasLink("Position"));
		assertTrue(employeeResource.getLink("Position").getHref().contains("/3/position"));
		assertTrue(employeeResource.getLink("Position").getTitle().contains("[/{id}/position]"));
		
		assertTrue(employeeResource.hasLink("Phones"));
		assertTrue(employeeResource.getLink("Phones").getHref().contains("/3/phones"));
		assertTrue(employeeResource.getLink("Phones").getTitle().contains("[/{id}/phones]"));
		
		assertTrue(employeeResource.hasLink("AppointedTasks"));
		assertTrue(employeeResource.getLink("AppointedTasks").getHref().contains("/3/appointed_tasks"));
		assertTrue(employeeResource.getLink("AppointedTasks").getTitle().contains("[/{id}/appointed_tasks]"));
		
		assertTrue(employeeResource.hasLink("OrdersCreatedBy"));
		assertTrue(employeeResource.getLink("OrdersCreatedBy").getHref().contains("/3/orders_created_by"));
		assertTrue(employeeResource.getLink("OrdersCreatedBy").getTitle().contains("[/{id}/orders_created_by]"));
	}
	
	
}