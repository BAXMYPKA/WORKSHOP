package workshop.internal.hateoasResources;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;
import workshop.controllers.internal.rest.DepartmentsRestController;
import workshop.controllers.internal.rest.OrdersRestController;
import workshop.internal.entities.*;
import workshop.internal.services.*;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
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
	WorkshopEntityTypesService workshopEntityTypesService;
	@Autowired
	DepartmentsRestController departmentsController;
	@Autowired
	OrdersRestController ordersController;
	
	@Test
	@org.junit.jupiter.api.Order(1)
	public void init() {
		assertNotNull(mockMvc);
	}
	
	@Test
	@WithMockUser(username = "employee@workshop.pro", authorities = {"Admin", "ADMIN_FULL"})
	public void department_Resource_Should_Contain_Self_Link_And_Its_Positions_Link() throws Exception {
		//GIVEN
		List<Department> allEntities1 = departmentsService.findAllEntities(100, 0, null, null);
		List<WorkshopEntityType> allEntities = workshopEntityTypesService.findAllEntities(100, 0, null, null);
		
		Department department = new Department("Department res 1");
		departmentsService.persistEntity(department);
		
		Position position = new Position("Position res 1", department);
		positionsService.persistEntity(position);
		
		long departmentId = department.getIdentifier();
		
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
	
	@Test
	@WithMockUser(username = "employee@workshop.pro", authorities = {"Admin", "ADMIN_FULL"})
	public void departments_Positions_FirstPage_Should_Be_Returned_As_Sorted_By_Name_Order_Desc_Paged_Collection()
		throws Exception {
		//GIVEN 9 Positions, 2 Items pageSize for 5 pages
		Department department = new Department("Department firstPage 1");
		departmentsService.persistEntity(department);
		
		Position position1 = new Position("Position firstPage 1", department);
		Position position2 = new Position("Position firstPage 2", department);
		Position position3 = new Position("Position firstPage 3", department);
		Position position4 = new Position("Position firstPage 4", department);
		Position position5 = new Position("Position firstPage 5", department);
		Position position6 = new Position("Position firstPage 6", department);
		Position position7 = new Position("Position firstPage 7", department);
		Position position8 = new Position("Position firstPage 8", department);
		Position position9 = new Position("Position firstPage 9", department);
		positionsService.persistEntities(position1, position2, position3, position4, position5, position6, position7,
			position8, position9);
		
		long departmentId = department.getIdentifier();
		
		MockHttpServletRequestBuilder sortByNameDescRequest = MockMvcRequestBuilders.request("GET",
			URI.create("/internal/departments/" + departmentId + "/positions?order-by=name&order=desc&pageSize=2"));
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(sortByNameDescRequest);
		
		//THEN
		resultActions.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.content().string(
				Matchers.containsString("\"name\":\"Position firstPage 9\"")))
			.andExpect(MockMvcResultMatchers.content().string(
				Matchers.containsString("\"name\":\"Position firstPage 8\"")))
			
			.andExpect(MockMvcResultMatchers.content().string(Matchers.not(
				Matchers.containsString("\"name\":\"Position firstPage 7\""))))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.not(
				Matchers.containsString("\"name\":\"Position firstPage 1\""))));
	}
	
	@Test
	@WithMockUser(username = "employee@workshop.pro", authorities = {"Admin", "ADMIN_FULL"})
	@Transactional
	public void departments_Positions_SecondPage_Should_Be_Returned_With_Prev_Next_First_Last_Pages_Links()
		throws Exception {
		//GIVEN 9 Positions, 2 Items pageSize for 5 pages
		Department department = new Department("Department secPage 1");
		departmentsService.persistEntity(department);
		
		Position position1 = new Position("Position secPage 1", department);
		Position position2 = new Position("Position secPage 2", department);
		Position position3 = new Position("Position secPage 3", department);
		Position position4 = new Position("Position secPage 4", department);
		Position position5 = new Position("Position secPage 5", department);
		Position position6 = new Position("Position secPage 6", department);
		Position position7 = new Position("Position secPage 7", department);
		Position position8 = new Position("Position secPage 8", department);
		Position position9 = new Position("Position secPage 9", department);
		positionsService.persistEntities(position1, position2, position3, position4, position5, position6, position7,
			position8, position9);
		
		long departmentId = department.getIdentifier();
		
		MockHttpServletRequestBuilder sortByNameDescRequest = MockMvcRequestBuilders.request("GET",
			URI.create("/internal/departments/" + departmentId + "/positions?order-by=name&order=desc&pageSize=2&pageNum=2"));
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(sortByNameDescRequest);
		
		//THEN
		resultActions.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.content().string(
				Matchers.containsString("\"name\":\"Position secPage 7\"")))
			.andExpect(MockMvcResultMatchers.content().string(
				Matchers.containsString("\"name\":\"Position secPage 6\"")))
			
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
				Matchers.containsString("\"name\":\"Position secPage 9\""))))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.not(
				Matchers.containsString("\"name\":\"Position secPage 1\""))));
	}
	
	@Test
	@WithMockUser(username = "employee@workshop.pro", authorities = {"Admin", "ADMIN_FULL"})
	public void order_Tasks_Should_Be_Returned() throws Exception {
		//GIVEN
		Order order = new Order();
		order.setDescription("Order 1");
		order.setOverallPrice(BigDecimal.TEN);
		
		order = ordersService.persistEntity(order);
		
		Classifier classifier = new Classifier();
		classifier.setName("Classifier ordTask 1");
		
		classifier = classifiersService.persistEntity(classifier);
		
		Task task1 = new Task();
		task1.setName("Task ordTask 1");
		task1.setOrder(order);
		task1.addClassifier(classifier);
		
		Task task2 = new Task();
		task2.setName("Task ordTask 2");
		task2.setOrder(order);
		task2.addClassifier(classifier);
		
		Task task3 = new Task();
		task3.setName("Task ordTask 3");
		task3.setOrder(order);
		task3.addClassifier(classifier);
		
		tasksService.persistEntities(task1, task2, task3);
		
		long task3Identifier = task3.getIdentifier();
		long orderId = order.getIdentifier();
		
		//PageSize=2 of 3 Tasks total
		MockHttpServletRequestBuilder sortByNameDescRequest = MockMvcRequestBuilders.request("GET",
			URI.create("/internal/orders/" + orderId + "/tasks?order-by=name&order=asc&pageSize=2&pageNum=1"));
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(sortByNameDescRequest);
		
		//THEN
		resultActions.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.content().string(
				Matchers.containsString("\"name\":\"Task ordTask 1\"")))
			.andExpect(MockMvcResultMatchers.content().string(
				Matchers.containsString("\"name\":\"Task ordTask 2\"")))
			
			.andExpect(MockMvcResultMatchers.content().string(
				Matchers.not(Matchers.containsString("\"identifier\":\"" + task3Identifier + "\""))));
	}
	
	@Test
	@WithMockUser(username = "employee@workshop.pro", authorities = {"Admin", "ADMIN_FULL"})
	public void employee_Phones_Should_Be_Included_As_Employee_Phones_Link() throws Exception {
		//GIVEN an Employee with Phones
		Department department = new Department("Department phone 1");
		departmentsService.persistEntity(department);
		
		Position position = new Position("Position phone 1", department);
		positionsService.persistEntity(position);
		
		Phone phone1 = new Phone("Home", "8-911-111-11-113");
		Phone phone2 = new Phone("Home2", "8-911-111-11-123");
		Phone phone3 = new Phone("Work", "8-911-111-11-133");
		
		Employee employee = new Employee("Fname", "LName", "12345", "emp@workshop.pro",
			LocalDate.now().minusYears(55), position);
		long employeeId = employeesService.persistEntity(employee).getIdentifier();
		
		phone1.setEmployee(employee);
		phone2.setEmployee(employee);
		phone3.setEmployee(employee);
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(
			"GET", URI.create("/internal/employees/" + employeeId));
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(request);
		
		//THEN
		resultActions.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"rel\":\"Phones\",\"href\":\"http://localhost/internal/employees/" + employeeId + "/phones\"")));
	}
	
	@Test
	@WithMockUser(username = "employee@workshop.pro", authorities = {"Admin", "ADMIN_FULL"})
	public void employee_AppointedTasks_Should_Be_Pageable_Included_With_Self_And_Navigation_Links() throws Exception {
		//GIVEN an Employee with 3 appointed Tasks
		Department department = new Department("Department appTask 1");
		departmentsService.persistEntity(department);
		
		Position position = new Position("Position appTask 1", department);
		positionsService.persistEntity(position);
		
		Employee employee = new Employee("Fname", "LName", "12345", "emp@workshop.pro",
			LocalDate.now().minusYears(55), position);
		
		long employeeId = employeesService.persistEntity(employee).getIdentifier();
		
		Order order = new Order();
		order.setDescription("Order appTask 1");
		ordersService.persistEntity(order);
		
		Task task1 = new Task("Task appTask 1", order);
		task1.setAppointedTo(employee);
		
		Task task2 = new Task("Task appTask 2", order);
		task2.setAppointedTo(employee);
		
		Task task3 = new Task("Task appTask 3", order);
		task3.setAppointedTo(employee);
		
		tasksService.persistEntities(task1, task2, task3);
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(
			"GET", URI.create("/internal/employees/" + employeeId + "/appointed-tasks"));
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(request);
		
		//THEN
		resultActions.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Task appTask 1")))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Task appTask 2")))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Task appTask 3")))
			
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"href\":\"http://localhost/internal/tasks/" + task1.getIdentifier())))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"href\":\"http://localhost/internal/tasks/" + task2.getIdentifier())))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"href\":\"http://localhost/internal/tasks/" + task3.getIdentifier())))
			
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"rel\":\"currentPage\",\"href\":\"http://localhost/internal/employees/" + employeeId + "/appointed-tasks")));
	}
	
	@Test
	@WithMockUser(username = "employee@workshop.pro", authorities = {"Admin", "ADMIN_FULL"})
	public void employee_TasksModifiedBy_Should_Be_Pageable_Included_With_Self_And_Navigation_Links() throws Exception {
		//GIVEN an Employee with 3 appointed Tasks
		Department department = new Department("Department taskMod 1");
		departmentsService.persistEntity(department);
		
		Position position = new Position("Position taskMod 1", department);
		positionsService.persistEntity(position);
		
		Employee employee = new Employee("Fname", "LName", "12345", "empl@workshop.pro",
			LocalDate.now().minusYears(55), position);
		
		long employeeId = employeesService.persistEntity(employee).getIdentifier();
		
		Order order = new Order();
		order.setDescription("Order taskMod 1");
		ordersService.persistEntity(order);
		
		Task task1 = new Task("Task taskMod 1", order);
		task1.setAppointedTo(employee);
		task1.setModifiedBy(employee);
		
		Task task2 = new Task("Task taskMod 2", order);
		task2.setAppointedTo(employee);
		task2.setModifiedBy(employee);
		
		Task task3 = new Task("Task taskMod 3", order);
		task3.setAppointedTo(employee);
		task3.setModifiedBy(employee);
		
		tasksService.persistEntities(task1, task2, task3);
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(
			"GET", URI.create("/internal/employees/" + employeeId + "/tasks-modified-by"));
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(request);
		
		//THEN
		resultActions.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Task taskMod 1")))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Task taskMod 2")))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Task taskMod 3")))
			
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"href\":\"http://localhost/internal/tasks/" + task1.getIdentifier())))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"href\":\"http://localhost/internal/tasks/" + task1.getIdentifier())))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"href\":\"http://localhost/internal/tasks/" + task1.getIdentifier())))
			
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"rel\":\"currentPage\",\"href\":\"http://localhost/internal/employees/" + employeeId + "/tasks-modified-by")));
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void employee_TasksCreatedBy_Should_Be_Pageable_Included_With_Self_And_Navigation_Links() throws Exception {
		//GIVEN an Employee with 3 appointed Tasks
		Department department = new Department("Department taskCreated 1");
		departmentsService.persistEntity(department);
		
		Position position = new Position("Position taskCreated 1", department);
		positionsService.persistEntity(position);
		
		Employee employee = new Employee("Fname", "LName", "12345", "emplo@workshop.pro",
			LocalDate.now().minusYears(55), position);
		
		long employeeId = employeesService.persistEntity(employee).getIdentifier();
		
		Order order = new Order();
		order.setDescription("Order taskCreated 1");
		ordersService.persistEntity(order);
		
		Task task1 = new Task("Task taskCreated 1", order);
		task1.setCreatedBy(employee);
		task1.setOrder(order);
		
		Task task2 = new Task("Task taskCreated 2", order);
		task2.setCreatedBy(employee);
		task2.setOrder(order);
		
		Task task3 = new Task("Task taskCreated 3", order);
		task3.setCreatedBy(employee);
		task3.setOrder(order);
		
		tasksService.persistEntities(task1, task2, task3);
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(
			"GET", URI.create("/internal/employees/" + employeeId + "/tasks-created-by"));
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(request);
		
		//THEN
		resultActions.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Task taskCreated 1")))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Task taskCreated 2")))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Task taskCreated 3")))
			
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"href\":\"http://localhost/internal/tasks/" + task1.getIdentifier())))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"href\":\"http://localhost/internal/tasks/" + task2.getIdentifier())))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"href\":\"http://localhost/internal/tasks/" + task3.getIdentifier())))
			
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"rel\":\"currentPage\",\"href\":\"http://localhost/internal/employees/" + employeeId + "/tasks-created-by")));
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void employee_OrdersModifiedBy_Should_Be_Pageable_Included_With_Self_And_Navigation_Links() throws Exception {
		//GIVEN an Employee with 3 appointed Tasks
		Department department = new Department("Department orderMod 1");
		departmentsService.persistEntity(department);
		
		Position position = new Position("Position orderMod 1", department);
		positionsService.persistEntity(position);
		
		Employee employee = new Employee("Fname", "LName", "12345", "employ@workshop.pro",
			LocalDate.now().minusYears(55), position);
		
		long employeeId = employeesService.persistEntity(employee).getIdentifier();
		
		Order order1 = new Order();
		order1.setDescription("Order orderMod 1");
		order1.setModifiedBy(employee);
		
		Order order2 = new Order();
		order2.setModifiedBy(employee);
		order2.setDescription("Order orderMod 2");
		
		Order order3 = new Order();
		order3.setDescription("Order orderMod 3");
		order3.setModifiedBy(employee);
		
		ordersService.persistEntities(order1, order2, order3);
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(
			"GET", URI.create("/internal/employees/" + employeeId + "/orders-modified-by"));
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(request);
		
		//THEN
		resultActions.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Order orderMod 1")))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Order orderMod 2")))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Order orderMod 3")))
			
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"href\":\"http://localhost/internal/orders/" + order1.getIdentifier())))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"href\":\"http://localhost/internal/orders/" + order2.getIdentifier())))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"href\":\"http://localhost/internal/orders/" + order3.getIdentifier())))
			
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"rel\":\"currentPage\",\"href\":\"http://localhost/internal/employees/" + employeeId + "/orders-modified-by")));
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void employee_OrdersCreatedBy_Should_Be_Pageable_Included_With_Self_And_Navigation_Links() throws Exception {
		//GIVEN an Employee with 3 appointed Tasks
		Department department = new Department("Department ordCreatedBy 1");
		departmentsService.persistEntity(department);
		
		Position position = new Position("Position ordCreatedBy 1", department);
		positionsService.persistEntity(position);
		
		Employee employee = new Employee("Fname", "LName", "12345", "emp22@workshop.pro",
			LocalDate.now().minusYears(55), position);
		
		long employeeId = employeesService.persistEntity(employee).getIdentifier();
		
		Order order1 = new Order();
		order1.setDescription("Order ordCreatedBy 1");
		order1.setCreatedBy(employee);
		
		Order order2 = new Order();
		order2.setCreatedBy(employee);
		order2.setDescription("Order ordCreatedBy 2");
		
		Order order3 = new Order();
		order3.setDescription("Order ordCreatedBy 3");
		order3.setCreatedBy(employee);
		ordersService.persistEntities(order1, order2, order3);
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(
			"GET", URI.create("/internal/employees/" + employeeId + "/orders-created-by"));
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(request);
		
		//THEN
		resultActions.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Order ordCreatedBy 1")))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Order ordCreatedBy 2")))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Order ordCreatedBy 3")))
			
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"href\":\"http://localhost/internal/orders/" + order1.getIdentifier())))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"href\":\"http://localhost/internal/orders/" + order2.getIdentifier())))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"href\":\"http://localhost/internal/orders/" + order3.getIdentifier())))
			
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"rel\":\"currentPage\",\"href\":\"http://localhost/internal/employees/" + employeeId + "/orders-created-by")));
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void task_Classifiers_Should_Be_Pageable_Included_With_Self_And_Navigation_Links() throws Exception {
		//GIVEN
		Department department = new Department("Department taskClass 1");
		departmentsService.persistEntity(department);
		
		Position position = new Position("Position taskClass 1", department);
		positionsService.persistEntity(position);
		
		Employee employee = new Employee("Fname", "LName", "12345", "emp223@workshop.pro",
			LocalDate.now().minusYears(55), position);
		employeesService.persistEntity(employee);
		
		Order order = new Order();
		order.setDescription("Order taskClass 1");
		ordersService.persistEntity(order);
		
		Classifier classifier1 = new Classifier();
		classifier1.setName("Classifier taskClass 1");
		
		Classifier classifier2 = new Classifier();
		classifier2.setName("Classifier taskClass 2");
		
		Classifier classifier3 = new Classifier();
		classifier3.setName("Classifier taskClass 3");
		classifiersService.persistEntities(classifier1, classifier2, classifier3);
		
		Task task = new Task("Task taskClass 1", order);
		task.setCreatedBy(employee);
		task.addClassifier(classifier1, classifier2, classifier3);
		tasksService.persistEntity(task);
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(
			"GET", URI.create("/internal/tasks/" + task.getIdentifier() + "/classifiers"));
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(request);
		
		//THEN
		resultActions.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Classifier taskClass 1")))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Classifier taskClass 2")))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Classifier taskClass 3")))
			
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"href\":\"http://localhost/internal/classifiers/" + classifier1.getIdentifier())))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"href\":\"http://localhost/internal/classifiers/" + classifier2.getIdentifier())))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"href\":\"http://localhost/internal/classifiers/" + classifier3.getIdentifier())))
			
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"rel\":\"currentPage\",\"href\":\"http://localhost/internal/tasks/" + task.getIdentifier() + "/classifiers")));
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void user_OrdersCreatedFor_Should_Be_Pageable_Included_With_Self_And_Navigation_Links() throws Exception {
		//GIVEN
		Department department = new Department("Department ordCreatedFor 1");
		departmentsService.persistEntity(department);
		
		Position position = new Position("Position ordCreatedFor 1", department);
		positionsService.persistEntity(position);
		
		Employee employee = new Employee("Fname", "LName", "12345", "emp225@workshop.pro",
			LocalDate.now().minusYears(55), position);
		employeesService.persistEntity(employee);
		
		User user = new User();
		user.setFirstName("UserFname");
		user.setEmail("user@workshop.pro");
		usersService.persistEntity(user);
		
		Order order = new Order();
		order.setDescription("Order ordCreatedFor 1");
		order.setCreatedFor(user);
		
		Order order2 = new Order();
		order2.setDescription("Order ordCreatedFor 2");
		order2.setCreatedFor(user);
		
		ordersService.persistEntities(order, order2);
		
		Classifier classifier1 = new Classifier();
		classifier1.setName("Classifier ordCreatedFor 1");
		
		Classifier classifier2 = new Classifier();
		classifier2.setName("Classifier ordCreatedFor 2");
		
		Classifier classifier3 = new Classifier();
		classifier3.setName("Classifier ordCreatedFor 3");
		
		classifiersService.persistEntities(classifier1, classifier2, classifier3);
		
		Task task = new Task("Task ordCreatedFor 1", order);
		task.setCreatedBy(employee);
		task.setClassifiers(new HashSet<>(Arrays.asList(classifier1, classifier2, classifier3)));
		tasksService.persistEntity(task);
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(
			"GET", URI.create("/internal/users/" + user.getIdentifier() + "/orders"));
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(request);
		
		//THEN
		resultActions.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Order ordCreatedFor 1")))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Order ordCreatedFor 2")))
			
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"href\":\"http://localhost/internal/orders/" + order.getIdentifier())))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"href\":\"http://localhost/internal/orders/" + order2.getIdentifier())))
			
			.andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(
				"\"rel\":\"currentPage\",\"href\":\"http://localhost/internal/users/" + user.getIdentifier() + "/orders")));
	}
}