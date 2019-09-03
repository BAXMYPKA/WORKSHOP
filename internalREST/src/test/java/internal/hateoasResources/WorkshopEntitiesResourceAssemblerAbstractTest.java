package internal.hateoasResources;

import internal.controllers.EmployeesController;
import internal.dao.EmployeesDao;
import internal.entities.Employee;
import internal.services.EmployeesService;
import internal.services.OrdersService;
import internal.services.WorkshopEntitiesServiceAbstract;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.web.bind.annotation.GetMapping;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class WorkshopEntitiesResourceAssemblerAbstractTest {
	
	@Mock
	EmployeesService employeesService;
	@Mock
	EmployeesResourceAssembler employeesResourceAssembler;
	@Mock
	EmployeesDao employeesDao;
	EmployeesController employeesController;
	
	@BeforeEach
	public void beforeEach() {
		Mockito.when(employeesService.getEntityClass()).thenReturn(Employee.class);
		employeesService.setEntityClassSimpleName(Employee.class.getSimpleName());
		employeesService.setWorkshopEntitiesDaoAbstract(employeesDao);
		
		employeesResourceAssembler.setWorkshopControllerAbstractClass(EmployeesController.class);
		employeesResourceAssembler.setWorkshopEntityClass(Employee.class);
		
		employeesController = new EmployeesController(employeesService, employeesResourceAssembler);
	}
	
	@Test
	public void getMapped_Controller_Mehtods_Should_Be_Converted_to_Links() {
		//GIVEN
//		EmployeesController employeesController = new EmployeesController(employeesService, employeesResourceAssembler);
		long ownerId = 5L;
		
		//WHEN
		WorkshopEntitiesServiceAbstract<Employee> workshopEntitiesService = employeesController.getWorkshopEntitiesService();
		
		Method[] methods = employeesController.getClass().getDeclaredMethods();
		List<Method> methodsGetMapped = Arrays.stream(methods)
			.filter(method -> method.isAnnotationPresent(GetMapping.class))
			.collect(Collectors.toList());
		
		Method testMethod = methodsGetMapped.stream()
			.filter(method -> method.getName().equalsIgnoreCase("position"))
			.findFirst().get();
		
		Link testLink = ControllerLinkBuilder.linkTo(
			EmployeesController.class, testMethod, ownerId)
			.withRel(testMethod.getName());
		
		//THEN
		System.out.println(methodsGetMapped);
		System.out.println(methodsGetMapped.size());
		System.out.println(testLink);
		assertFalse(methodsGetMapped.isEmpty());
	}
	
}