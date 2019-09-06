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
import java.util.Arrays;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class SubResourcesCollectionsLinksIT {
	
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
	UsersService usersService;
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
		
		employee.setPhones(new HashSet<>(Arrays.asList(phone1, phone2, phone3)));
		
		
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
		//GIVEN an Employee with 3 appointed Tasks
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
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(
			"GET", URI.create("/internal/employees/" + employeeId+"/appointed_tasks"));
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(request);
		
		//THEN
		resultActions.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Task 1")))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Task 2")))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Task 3")))
			
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"href\":\"http://localhost/internal/tasks/" + persistedTask1.getIdentifier())))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"href\":\"http://localhost/internal/tasks/" + persistedTask2.getIdentifier())))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"href\":\"http://localhost/internal/tasks/" + persistedTask3.getIdentifier())))
			
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"rel\":\"currentPage\",\"href\":\"http://localhost/internal/employees/101/appointed_tasks")));
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"Admin"})
	public void employee_TasksModifiedBy_Should_Be_Pageable_Included_With_Self_And_Navigation_Links() throws Exception {
		//GIVEN an Employee with 3 appointed Tasks
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
		task1.setModifiedBy(employeePersisted);
		task1.setOrder(persistedOrder);
		Task persistedTask1 = tasksService.persistEntity(task1);
		
		Task task2 = new Task();
		task2.setName("Task 2");
		task2.setAppointedTo(employeePersisted);
		task2.setModifiedBy(employeePersisted);
		task2.setOrder(persistedOrder);
		Task persistedTask2 = tasksService.persistEntity(task2);
		
		Task task3 = new Task();
		task3.setName("Task 3");
		task3.setAppointedTo(employeePersisted);
		task3.setModifiedBy(employeePersisted);
		task3.setOrder(persistedOrder);
		Task persistedTask3 = tasksService.persistEntity(task3);
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(
			"GET", URI.create("/internal/employees/" + employeeId+"/tasks_modified_by"));
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(request);
		
		//THEN
		resultActions.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Task 1")))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Task 2")))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Task 3")))
			
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"href\":\"http://localhost/internal/tasks/" + persistedTask1.getIdentifier())))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"href\":\"http://localhost/internal/tasks/" + persistedTask2.getIdentifier())))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"href\":\"http://localhost/internal/tasks/" + persistedTask3.getIdentifier())))
			
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"rel\":\"currentPage\",\"href\":\"http://localhost/internal/employees/101/tasks_modified_by")));
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"Admin"})
	public void employee_TasksCreatedBy_Should_Be_Pageable_Included_With_Self_And_Navigation_Links() throws Exception {
		//GIVEN an Employee with 3 appointed Tasks
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
		task1.setCreatedBy(employeePersisted);
		task1.setOrder(persistedOrder);
		Task persistedTask1 = tasksService.persistEntity(task1);
		
		Task task2 = new Task();
		task2.setName("Task 2");
		task2.setCreatedBy(employeePersisted);
		task2.setOrder(persistedOrder);
		Task persistedTask2 = tasksService.persistEntity(task2);
		
		Task task3 = new Task();
		task3.setName("Task 3");
		task3.setCreatedBy(employeePersisted);
		task3.setOrder(persistedOrder);
		Task persistedTask3 = tasksService.persistEntity(task3);
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(
			"GET", URI.create("/internal/employees/" + employeeId+"/tasks_created_by"));
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(request);
		
		//THEN
		resultActions.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Task 1")))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Task 2")))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Task 3")))
			
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"href\":\"http://localhost/internal/tasks/" + persistedTask1.getIdentifier())))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"href\":\"http://localhost/internal/tasks/" + persistedTask2.getIdentifier())))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"href\":\"http://localhost/internal/tasks/" + persistedTask3.getIdentifier())))
			
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"rel\":\"currentPage\",\"href\":\"http://localhost/internal/employees/101/tasks_created_by")));
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"Admin"})
	public void employee_OrdersModifiedBy_Should_Be_Pageable_Included_With_Self_And_Navigation_Links() throws Exception {
		//GIVEN an Employee with 3 appointed Tasks
		Department department = new Department("Department 1");
		Department departmentPersisted = departmentsService.persistEntity(department);
		
		Position position = new Position("Position 1", departmentPersisted);
		Position positionPersisted = positionsService.persistEntity(position);
		
		Employee employee = new Employee("FN", "LN", "12345", "emp@workshop.pro",
			LocalDate.now().minusYears(55), positionPersisted);
		
		Employee employeePersisted = employeesService.persistEntity(employee);
		long employeeId = employeePersisted.getIdentifier();
		
		Order order1 = new Order();
		order1.setDescription("Order 1");
		order1.setModifiedBy(employee);
		Order persistedOrder1 = ordersService.persistEntity(order1);
		
		Order order2 = new Order();
		order2.setModifiedBy(employee);
		order2.setDescription("Order 2");
		Order persistedOrder2 = ordersService.persistEntity(order2);
		
		Order order3 = new Order();
		order3.setDescription("Order 3");
		order3.setModifiedBy(employee);
		Order persistedOrder3 = ordersService.persistEntity(order3);
		
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(
			"GET", URI.create("/internal/employees/" + employeeId+"/orders_modified_by"));
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(request);
		
		//THEN
		resultActions.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Order 1")))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Order 2")))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Order 3")))
			
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"href\":\"http://localhost/internal/orders/" + persistedOrder1.getIdentifier())))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"href\":\"http://localhost/internal/orders/" + persistedOrder2.getIdentifier())))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"href\":\"http://localhost/internal/orders/" + persistedOrder3.getIdentifier())))
			
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"rel\":\"currentPage\",\"href\":\"http://localhost/internal/employees/101/orders_modified_by")));
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"Admin"})
	public void employee_OrdersCreatedBy_Should_Be_Pageable_Included_With_Self_And_Navigation_Links() throws Exception {
		//GIVEN an Employee with 3 appointed Tasks
		Department department = new Department("Department 1");
		Department departmentPersisted = departmentsService.persistEntity(department);
		
		Position position = new Position("Position 1", departmentPersisted);
		Position positionPersisted = positionsService.persistEntity(position);
		
		Employee employee = new Employee("FN", "LN", "12345", "emp@workshop.pro",
			LocalDate.now().minusYears(55), positionPersisted);
		
		Employee employeePersisted = employeesService.persistEntity(employee);
		long employeeId = employeePersisted.getIdentifier();
		
		Order order1 = new Order();
		order1.setDescription("Order 1");
		order1.setCreatedBy(employee);
		Order persistedOrder1 = ordersService.persistEntity(order1);
		
		Order order2 = new Order();
		order2.setCreatedBy(employee);
		order2.setDescription("Order 2");
		Order persistedOrder2 = ordersService.persistEntity(order2);
		
		Order order3 = new Order();
		order3.setDescription("Order 3");
		order3.setCreatedBy(employee);
		Order persistedOrder3 = ordersService.persistEntity(order3);
		
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(
			"GET", URI.create("/internal/employees/" + employeeId+"/orders_created_by"));
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(request);
		
		//THEN
		resultActions.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Order 1")))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Order 2")))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Order 3")))
			
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"href\":\"http://localhost/internal/orders/" + persistedOrder1.getIdentifier())))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"href\":\"http://localhost/internal/orders/" + persistedOrder2.getIdentifier())))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"href\":\"http://localhost/internal/orders/" + persistedOrder3.getIdentifier())))
			
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"rel\":\"currentPage\",\"href\":\"http://localhost/internal/employees/101/orders_created_by")));
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"Admin"})
	public void task_Classifiers_Should_Be_Pageable_Included_With_Self_And_Navigation_Links() throws Exception {
		//GIVEN
		Department department = new Department("Department 1");
		Department departmentPersisted = departmentsService.persistEntity(department);
		
		Position position = new Position("Position 1", departmentPersisted);
		Position positionPersisted = positionsService.persistEntity(position);
		
		Employee employee = new Employee("FN", "LN", "12345", "emp@workshop.pro",
			LocalDate.now().minusYears(55), positionPersisted);
		
		Employee employeePersisted = employeesService.persistEntity(employee);
		
		Order order = new Order();
		order.setDescription("Order 1");
		Order persistedOrder = ordersService.persistEntity(order);
		
		Task task1 = new Task();
		task1.setName("Task 1");
		task1.setCreatedBy(employeePersisted);
		task1.setOrder(persistedOrder);
		
		Classifier classifier1 = new Classifier();
		classifier1.setName("Classifier 1");
		classifier1.addTask(task1);
		
		Classifier classifier2 = new Classifier();
		classifier2.setName("Classifier 2");
		classifier2.addTask(task1);
		
		Classifier classifier3 = new Classifier();
		classifier3.setName("Classifier 3");
		classifier3.addTask(task1);
		
		Classifier persistedClassifier1 = classifiersService.persistEntity(classifier1);
		Classifier persistedClassifier2 = classifiersService.persistEntity(classifier2);
		Classifier persistedClassifier3 = classifiersService.persistEntity(classifier3);
		
		task1.setClassifiers(new HashSet<>(Arrays.asList(persistedClassifier1, persistedClassifier2, persistedClassifier3)));
		Task persistedTask1 = tasksService.persistEntity(task1);
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(
			"GET", URI.create("/internal/tasks/" + task1.getIdentifier()+"/classifiers"));
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(request);
		
		//THEN
		resultActions.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Classifier 1")))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Classifier 2")))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Classifier 3")))
			
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"href\":\"http://localhost/internal/classifiers/" + persistedClassifier1.getIdentifier())))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"href\":\"http://localhost/internal/classifiers/" + persistedClassifier2.getIdentifier())))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"href\":\"http://localhost/internal/classifiers/" + persistedClassifier3.getIdentifier())))
			
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"rel\":\"currentPage\",\"href\":\"http://localhost/internal/tasks/"+task1.getIdentifier()+"/classifiers")));
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"Admin"})
	public void user_OrdersCreatedFor_Should_Be_Pageable_Included_With_Self_And_Navigation_Links() throws Exception {
		//GIVEN
		Department department = new Department("Department 1");
		Department departmentPersisted = departmentsService.persistEntity(department);
		
		Position position = new Position("Position 1", departmentPersisted);
		Position positionPersisted = positionsService.persistEntity(position);
		
		Employee employee = new Employee("FN", "LN", "12345", "emp@workshop.pro",
			LocalDate.now().minusYears(55), positionPersisted);
		
		Employee employeePersisted = employeesService.persistEntity(employee);
		
		User user = new User();
		user.setFirstName("UserFN");
		user.setEmail("user@workshop.pro");
		User userPersisted = usersService.persistEntity(user);
		
		Order order = new Order();
		order.setDescription("Order 1");
		order.setCreatedFor(userPersisted);
		Order persistedOrder = ordersService.persistEntity(order);
		
		Order order2 = new Order();
		order2.setDescription("Order 2");
		order2.setCreatedFor(userPersisted);
		Order persistedOrder2 = ordersService.persistEntity(order2);
		
		Task task1 = new Task();
		task1.setName("Task 1");
		task1.setCreatedBy(employeePersisted);
		task1.setOrder(persistedOrder);
		
		Classifier classifier1 = new Classifier();
		classifier1.setName("Classifier 1");
		classifier1.addTask(task1);
		
		Classifier classifier2 = new Classifier();
		classifier2.setName("Classifier 2");
		classifier2.addTask(task1);
		
		Classifier classifier3 = new Classifier();
		classifier3.setName("Classifier 3");
		classifier3.addTask(task1);
		
		Classifier persistedClassifier1 = classifiersService.persistEntity(classifier1);
		Classifier persistedClassifier2 = classifiersService.persistEntity(classifier2);
		Classifier persistedClassifier3 = classifiersService.persistEntity(classifier3);
		
		task1.setClassifiers(new HashSet<>(Arrays.asList(persistedClassifier1, persistedClassifier2, persistedClassifier3)));
		Task persistedTask1 = tasksService.persistEntity(task1);
		
		user.setOrders(Arrays.asList(order, order2));
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(
			"GET", URI.create("/internal/users/" + user.getIdentifier()+"/orders"));
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(request);
		
		//THEN
		resultActions.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Order 1")))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Order 2")))
			
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"href\":\"http://localhost/internal/orders/" + persistedOrder.getIdentifier())))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"href\":\"http://localhost/internal/orders/" + persistedOrder2.getIdentifier())))
			
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"rel\":\"currentPage\",\"href\":\"http://localhost/internal/users/"+userPersisted.getIdentifier()+"/orders")));
	}
	
	
}