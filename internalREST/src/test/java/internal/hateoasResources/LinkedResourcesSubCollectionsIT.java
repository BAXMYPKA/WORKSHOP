package internal.hateoasResources;

import internal.controllers.DepartmentsController;
import internal.controllers.OrdersController;
import internal.entities.*;
import internal.services.*;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class LinkedResourcesSubCollectionsIT {
	
	@Autowired
	MockMvc mockMvc;
	@Autowired
	DepartmentsService departmentsService;
	@Autowired
	PositionsService positionsService;
	@Autowired
	ClassifiersService classifiersService;
	@Autowired
	PhonesService phonesService;
	@Autowired
	TasksService tasksService;
	@Autowired
	OrdersService ordersService;
	@Autowired
	EmployeesService employeesService;
	@Autowired
	DepartmentsController departmentsController;
	@Autowired
	OrdersController ordersController;
	
	@Test
	@org.junit.jupiter.api.Order(1)
	public void init() {
		assertNotNull(mockMvc);
	}
	
	@Test
	@Transactional
	public void department_Resource_Should_Contain_Self_Link_And_Its_Positions_Link() throws Exception {
		//GIVEN
		Department department1 = new Department("Department 1");
		Position position1 = new Position("Position 1", department1);
		department1.addPosition(position1);
		
		Department department1Persisted = departmentsService.persistEntity(department1);
		long departmentId = department1Persisted.getIdentifier();
		
		departmentsController.setDEFAULT_PAGE_SIZE(2);
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request("GET",
			URI.create("/internal/departments/" + departmentId));
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(request);
		
		//THEN
		resultActions.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.content().string(
				Matchers.containsString("\"rel\":\"self\"")))
			.andExpect(MockMvcResultMatchers.content().string(
				Matchers.containsString("\"href\":\"http://localhost/internal/departments/" + departmentId)))
			.andExpect(MockMvcResultMatchers.content().string(
				Matchers.containsString("\"rel\":\"Positions\"")))
			.andExpect(MockMvcResultMatchers.content().string(
				Matchers.containsString("\"href\":\"http://localhost/internal/departments/" + departmentId + "/positions")));
	}
	
	@RepeatedTest(3)
	@Transactional
	public void departments_Positions_FirstPage_Should_Be_Returned_As_Sorted_By_Name_Order_Desc_Paged_Collection()
		throws Exception {
		//GIVEN 9 Positions, 2 Items pageSize for 5 pages
		Department department1 = new Department("Department 1");
		Position position1 = new Position("Position 1", department1);
		Position position2 = new Position("Position 2", department1);
		Position position3 = new Position("Position 3", department1);
		Position position4 = new Position("Position 4", department1);
		Position position5 = new Position("Position 5", department1);
		Position position6 = new Position("Position 6", department1);
		Position position7 = new Position("Position 7", department1);
		Position position8 = new Position("Position 8", department1);
		Position position9 = new Position("Position 9", department1);
		department1.addPosition(position1, position2, position3, position4, position5, position6, position7,
			position8, position9);
		
		Department department1Persisted = departmentsService.persistEntity(department1);
		
		long departmentId = department1Persisted.getIdentifier();
		
		MockHttpServletRequestBuilder sortByNameDescRequest = MockMvcRequestBuilders.request("GET",
			URI.create("/internal/departments/" + departmentId + "/positions?order-by=name&order=desc&pageSize=2"));
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(sortByNameDescRequest);
		
		//THEN
		resultActions.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.content().string(
				Matchers.containsString("\"name\":\"Position 9\"")))
			.andExpect(MockMvcResultMatchers.content().string(
				Matchers.containsString("\"name\":\"Position 8\"")))
			
			.andExpect(MockMvcResultMatchers.content().string(Matchers.not(
				Matchers.containsString("\"name\":\"Position 7\""))))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.not(
				Matchers.containsString("\"name\":\"Position 1\""))));
	}
	
	@RepeatedTest(3)
	@Transactional
	public void departments_Positions_SecondPage_Should_Be_Returned_With_Prev_Next_First_Last_Pages_Links()
		throws Exception {
		//GIVEN 9 Positions, 2 Items pageSize for 5 pages
		Department department1 = new Department("Department 1");
		Position position1 = new Position("Position 1", department1);
		Position position2 = new Position("Position 2", department1);
		Position position3 = new Position("Position 3", department1);
		Position position4 = new Position("Position 4", department1);
		Position position5 = new Position("Position 5", department1);
		Position position6 = new Position("Position 6", department1);
		Position position7 = new Position("Position 7", department1);
		Position position8 = new Position("Position 8", department1);
		Position position9 = new Position("Position 9", department1);
		department1.addPosition(position1, position2, position3, position4, position5, position6, position7,
			position8, position9);
		
		Department department1Persisted = departmentsService.persistEntity(department1);
		
		long departmentId = department1Persisted.getIdentifier();
		
		MockHttpServletRequestBuilder sortByNameDescRequest = MockMvcRequestBuilders.request("GET",
			URI.create("/internal/departments/" + departmentId + "/positions?order-by=name&order=desc&pageSize=2&pageNum=2"));
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(sortByNameDescRequest);
		
		//THEN
		resultActions.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.content().string(
				Matchers.containsString("\"name\":\"Position 7\"")))
			.andExpect(MockMvcResultMatchers.content().string(
				Matchers.containsString("\"name\":\"Position 6\"")))
			
			.andExpect(MockMvcResultMatchers.content().string(
				Matchers.containsString("\"rel\":\"previousPage\"")))
			.andExpect(MockMvcResultMatchers.content().string(
				Matchers.containsString("\"href\":\"http://localhost/internal/departments/" + departmentId + "/positions")))
			.andExpect(MockMvcResultMatchers.content().string(
				Matchers.containsString("\"rel\":\"nextPage\"")))
			.andExpect(MockMvcResultMatchers.content().string(
				Matchers.containsString("\"href\":\"http://localhost/internal/departments/" + departmentId + "/positions")))
			.andExpect(MockMvcResultMatchers.content().string(
				Matchers.containsString("\"rel\":\"firstPage\"")))
			.andExpect(MockMvcResultMatchers.content().string(
				Matchers.containsString("\"href\":\"http://localhost/internal/departments/" + departmentId + "/positions")))
			.andExpect(MockMvcResultMatchers.content().string(
				Matchers.containsString("\"rel\":\"lastPage\"")))
			.andExpect(MockMvcResultMatchers.content().string(
				Matchers.containsString("\"href\":\"http://localhost/internal/departments/" + departmentId + "/positions")))
			
			.andExpect(MockMvcResultMatchers.content().string(Matchers.not(
				Matchers.containsString("\"name\":\"Position 9\""))))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.not(
				Matchers.containsString("\"name\":\"Position 1\""))));
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"Admin"})
	public void order_Tasks_Should_Be_Returned() throws Exception {
		//GIVEN
		Order order = new internal.entities.Order();
		order.setDescription("Order 1");
		order.setOverallPrice(BigDecimal.TEN);
		
		order = ordersService.persistEntity(order);
		
		Classifier classifier = new Classifier();
		classifier.setName("Classifier 1");
		
		classifier = classifiersService.persistEntity(classifier);
		
		Task task1 = new Task();
		task1.setName("Task 1");
		task1.setOrder(order);
		task1.addClassifier(classifier);
		
		Task task2 = new Task();
		task2.setName("Task 2");
		task2.setOrder(order);
		task2.addClassifier(classifier);
		
		Task task3 = new Task();
		task3.setName("Task 3");
		task3.setOrder(order);
		task3.addClassifier(classifier);
		
		Task persistedTask1 = tasksService.persistEntity(task1);
		Task persistedTask2 = tasksService.persistEntity(task2);
		Task persistedTask3 = tasksService.persistEntity(task3);
		long task3Identifier = persistedTask3.getIdentifier();
		
		
		long orderId = order.getIdentifier();
		//PageSize=2 of 3 Tasks total
		MockHttpServletRequestBuilder sortByNameDescRequest = MockMvcRequestBuilders.request("GET",
			URI.create("/internal/orders/" + orderId + "/tasks?order-by=name&order=asc&pageSize=2&pageNum=1"));
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(sortByNameDescRequest);
		
		//THEN
		resultActions.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.content().string(
				Matchers.containsString("\"name\":\"Task 1\"")))
			.andExpect(MockMvcResultMatchers.content().string(
				Matchers.containsString("\"name\":\"Task 2\"")))
			
			.andExpect(MockMvcResultMatchers.content().string(
				Matchers.not(Matchers.containsString("\"identifier\":\"" + task3Identifier + "\""))));
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"Admin"})
	public void employee_Phones_Should_Be_Included_With_Self_Links() throws Exception {
		//GIVEN an Employee with Phones
		Department department = new Department("Department 1");
		Department departmentPersisted = departmentsService.persistEntity(department);
		
		Position position = new Position("Position 1", departmentPersisted);
		Position positionPersisted = positionsService.persistEntity(position);
		
		Phone phone1 = new Phone("Home", "8-911-111-11-11");
		Phone phone2 = new Phone("Home2", "8-911-111-11-12");
		Phone phone3 = new Phone("Work", "8-911-111-11-13");
		
		Employee employee = new Employee("FN", "LN", "12345", "emp@workshop.pro",
			LocalDate.now().minusYears(55), positionPersisted);
		
		Employee employeePersisted = employeesService.persistEntity(employee);
		long employeeId = employeePersisted.getIdentifier();
		
		phone1.setEmployee(employee);
		phone2.setEmployee(employee);
		phone3.setEmployee(employee);
		long phone1Id = phonesService.persistEntity(phone1).getIdentifier();
		long phone2Id = phonesService.persistEntity(phone2).getIdentifier();
		long phone3Id = phonesService.persistEntity(phone3).getIdentifier();
		
		employee.setPhones(Arrays.asList(phone1, phone2, phone3));
		
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(
			"GET", URI.create("/internal/employees/" + employeeId));
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(request);
		
		//THEN
		resultActions.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("8-911-111-11-11")))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("8-911-111-11-12")))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("8-911-111-11-13")))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"href\":\"http://localhost/internal/phones/" + phone1Id)))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"href\":\"http://localhost/internal/phones/" + phone2Id)))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"href\":\"http://localhost/internal/phones/" + phone3Id)));
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"Admin"})
	public void employee_AppointedTasks_Should_Be_Pageable_Included_With_Self_And_Navigation_Links() throws Exception {
		//GIVEN an Employee with Phones
		Department department = new Department("Department 1");
		Department departmentPersisted = departmentsService.persistEntity(department);
		
		Position position = new Position("Position 1", departmentPersisted);
		Position positionPersisted = positionsService.persistEntity(position);
		
		Employee employee = new Employee("FN", "LN", "12345", "emp@workshop.pro",
			LocalDate.now().minusYears(55), positionPersisted);
		
		Employee employeePersisted = employeesService.persistEntity(employee);
		long employeeId = employeePersisted.getIdentifier();
		
		Order order = new Order();
		order.setDescription("Order 1");
		Order persistedOrder = ordersService.persistEntity(order);
		
		Task task1 = new Task();
		task1.setName("Task 1");
		task1.setAppointedTo(employeePersisted);
		task1.setOrder(persistedOrder);
		Task persistedTask1 = tasksService.persistEntity(task1);
		
		Task task2 = new Task();
		task2.setName("Task 2");
		task2.setAppointedTo(employeePersisted);
		task2.setOrder(persistedOrder);
		Task persistedTask2 = tasksService.persistEntity(task2);
		
		Task task3 = new Task();
		task3.setName("Task 3");
		task3.setAppointedTo(employeePersisted);
		task3.setOrder(persistedOrder);
		Task persistedTask3 = tasksService.persistEntity(task3);
		
		Employee employeeMerged = employeesService.mergeEntity(employeePersisted);
		Order orderMerged = ordersService.mergeEntity(persistedOrder);
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(
			"GET", URI.create("/internal/employees/" + employeeId+"/appointed_tasks"));
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(request);
		
		//THEN
		resultActions.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("8-911-111-11-11")))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("8-911-111-11-12")))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("8-911-111-11-13")));
//			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
//				"\"href\":\"http://localhost/internal/phones/" + phone1Id)))
//			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
//				"\"href\":\"http://localhost/internal/phones/" + phone2Id)))
//			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
//				"\"href\":\"http://localhost/internal/phones/" + phone3Id)));
	}
	
}