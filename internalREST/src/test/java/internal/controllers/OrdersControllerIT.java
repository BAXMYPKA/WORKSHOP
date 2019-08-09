package internal.controllers;

import internal.entities.*;
import internal.service.EmployeesService;
import internal.service.serviceUtils.JsonServiceUtils;
import internal.service.OrdersService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
class OrdersControllerIT {
	
	@Autowired
	MockMvc mockMvc;
	@MockBean
	OrdersService ordersService;
	@MockBean
	EmployeesService employeesService;
	@MockBean
	JsonServiceUtils jsonServiceUtils;
	List<Order> orders = new ArrayList<>(10);
	
	
	@Test
	public void init_Context() {
		assertAll(
			() -> assertNotNull(mockMvc),
			() -> assertNotNull(ordersService),
			() -> assertNotNull(jsonServiceUtils)
		);
	}
	
	@Disabled
	@Test
	@DisplayName("Every Request for a List of Entities has to contain properties 'size' and 'page'")
	public void on_Incorrect_Request_Returns_400_Status() throws Exception {
		//GIVEN
		//A Request without obligatory 'size' parameter
		
		//WHEN THEN
		mockMvc.perform(
			(MockMvcRequestBuilders.get("/internal/orders/all"))
				.param("page", "2"))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().is(400));
	}
	
	@Test
	public void on_Simple_Request_Returns_Orders_List_With_Ok_Status_And_Json_ContentType() throws Exception {
		//GIVEN
		PageRequest created = PageRequest.of(2, 3, new Sort(Sort.Direction.ASC, "created"));
		
		Mockito.when(ordersService.findAllEntities(Mockito.any(Pageable.class), Mockito.any()))
			.thenReturn(new PageImpl<Order>(orders));
		
		
		String jsonOrders = "[{\"identifier\":1},{\"identifier\":2}]";
		
		Mockito.when(jsonServiceUtils.workshopEntityObjectsToJson(orders)).thenReturn(jsonOrders);
		
		//WHEN THEN
		mockMvc.perform(
			(MockMvcRequestBuilders.get("/internal/orders/all"))
				.param("size", "3")
				.param("page", "2"))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().is(200))
			.andExpect(MockMvcResultMatchers.content().string(jsonOrders))
			.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
	}
	
	@Test
	public void on_Correctly_Parametrized_Request_Returns_Orders_List_With_Ok_Status_And_Json_Content() throws Exception {
		
		//GIVEN
		
		Mockito.when(ordersService.findAllEntities(3, 2, "created", Sort.Direction.ASC))
			.thenReturn(orders);
		Mockito.when(ordersService.findAllEntities(
			PageRequest.of(2, 3, Sort.by(Sort.Direction.ASC, "created")), "created"))
			.thenReturn(new PageImpl<Order>(orders));
		
		String jsonOrders = "[{\"identifier\":1},{\"identifier\":2}]";
		
		Mockito.when(jsonServiceUtils.workshopEntityObjectsToJson(orders)).thenReturn(jsonOrders);
		
		//WHEN THEN
		
		mockMvc.perform(
			(MockMvcRequestBuilders.get("/internal/orders/all"))
				.param("size", "3")
				.param("page", "2")
				.param("order-by", "created")
				.param("order", "asc"))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().is(200))
			.andExpect(MockMvcResultMatchers.content().string(jsonOrders))
			.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
	}
	
	@ParameterizedTest
	@ValueSource(strings = {
		"",
		"{\"null\",\"name\":\"Name\"}",
		"{\"description\":\"The Descr\", \"deadline\":\"2020-6-5\"}",
		"{\"name\":1,\"description\":\"The Descr\"}",
		"{\"description\":\"The Descr\", \"createdFor\":\"2020\"}"})
	@DisplayName("ControllerAdvice with ExceptionHandler test with empty or incorrect JSON inside a Request body")
	public void post_Empty_or_Incorrect_Json_Returns_Bad_Request_Response_With_Predefined_Message(String requestBody)
		throws Exception {
		//GIVEN
		//RequestBody input strings
		ResultActions resultActions = null;
		
		//WHEN
		resultActions = mockMvc.perform(
			MockMvcRequestBuilders
				.post("/internal/orders")
				.accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(requestBody));
		
		//THEN
		resultActions
			.andExpect(MockMvcResultMatchers.status().isBadRequest())
			.andExpect(MockMvcResultMatchers.content().string("Incorrect request body!"))
			.andDo(MockMvcResultHandlers.print());
	}
	
	@Test
	@DisplayName("The Json Order with included new Tasks, Users and Classifiers which have to be persisted")
	@WithMockUser(username = "employee@workshop.pro", authorities = {"Admin", "Manager"})
	public void post_Correct_Order_as_Json_After_Persisting_Returns_Created201_HttpStatus() throws Exception {
		//GIVEN
		// Json Order with a lot of new Entities included
		String jsonOrder = getCorrectJsonOrder();
		// Order from Json passed to OrdersService
		ArgumentCaptor<Order> orderCaptured = ArgumentCaptor.forClass(Order.class);
		//Employee Authentication to pass to the Controller to be saved in 'createdBy' fields
		Employee authenticationEmployee = new Employee();
		authenticationEmployee.setIdentifier(150L);
		authenticationEmployee.setEmail("employee@workshop.pro");
		authenticationEmployee.setPosition(new Position("Position one", new Department("Department one")));
		authenticationEmployee.setBirthday(LocalDate.now().minusYears(49));
		authenticationEmployee.setFirstName("fn");
		authenticationEmployee.setLastName("ln");
		
		ResultActions resultActions = null;
		
		//WHEN
		//UserDetailsService has to return an Employee with @WithMockUser's credentials to be accessible from SecurityContext
		Mockito.lenient().when(employeesService.findByEmail("employee@workshop.pro")).thenReturn(Optional.of(authenticationEmployee));
		//OrdersService has to return a "persisted" non-empty Optional<Order>
		Mockito.when(ordersService.persistEntity(Mockito.any(Order.class))).thenReturn(new Order());
		
		resultActions = mockMvc.perform(
			MockMvcRequestBuilders
				.post("/internal/orders")
				.accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(jsonOrder));
		
		//THEN
		//Verify the correct Order from Json was passed to the OrdersService to be persisted
		Mockito.verify(ordersService, Mockito.atLeastOnce()).persistEntity(orderCaptured.capture());
		//Verify it was the same Order as in the Json from the Request
		assertEquals("The Correct Order One", orderCaptured.getValue().getDescription());
		
		
		resultActions
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isCreated())
			.andExpect(MockMvcResultMatchers.content().string(""));
	}
	
	@BeforeEach
	public void init() {
		Order order1 = new Order();
		order1.setIdentifier(1L);
		order1.setCreated(ZonedDateTime.of(2019, 10, 15, 9, 35, 45, 0, ZoneId.systemDefault()));
		Order order2 = new Order();
		order2.setIdentifier(2L);
		order2.setCreated(ZonedDateTime.of(2018, 11, 20, 9, 35, 45, 0, ZoneId.systemDefault()));
		Order order3 = new Order();
		order3.setIdentifier(3L);
		order3.setCreated(ZonedDateTime.of(2017, 11, 20, 9, 35, 45, 0, ZoneId.systemDefault()));
		Order order4 = new Order();
		order4.setIdentifier(4);
		order4.setCreated(ZonedDateTime.of(2016, 11, 20, 9, 35, 45, 0, ZoneId.systemDefault()));
		Order order5 = new Order();
		order5.setIdentifier(5);
		order5.setCreated(ZonedDateTime.of(2015, 11, 20, 9, 35, 45, 0, ZoneId.systemDefault()));
		Order order6 = new Order();
		order6.setIdentifier(6);
		order6.setCreated(ZonedDateTime.of(2014, 11, 20, 9, 35, 45, 0, ZoneId.systemDefault()));
		Order order7 = new Order();
		order7.setIdentifier(7);
		order7.setCreated(ZonedDateTime.of(2013, 11, 20, 9, 35, 45, 0, ZoneId.systemDefault()));
		Order order8 = new Order();
		order8.setIdentifier(8);
		order8.setCreated(ZonedDateTime.of(2012, 11, 20, 9, 35, 45, 0, ZoneId.systemDefault()));
		Order order9 = new Order();
		order9.setIdentifier(9);
		order9.setCreated(ZonedDateTime.of(2011, 11, 20, 9, 35, 45, 0, ZoneId.systemDefault()));
		Order order10 = new Order();
		order10.setIdentifier(10);
		order10.setCreated(ZonedDateTime.of(2010, 11, 20, 9, 35, 45, 0, ZoneId.systemDefault()));
		
		orders.addAll(Arrays.asList(order1, order2, order3, order4, order5, order6, order7, order8, order9, order10));
	}
	
	public String getCorrectJsonOrder() throws IOException {
		
		User user = new User();
		user.setBirthday(LocalDate.of(1960, 10, 5));
		user.setEmail("user@email.pro");
		user.setFirstName("Ivan");
		
		Employee employee = new Employee();
		employee.setIdentifier(100);
		employee.setFirstName("forValidationPass");
		employee.setLastName("forValidationPass");
		employee.setBirthday(LocalDateTime.now().minusYears(50).toLocalDate());
		employee.setEmail("appointed@workshop.pro");
		employee.setPosition(new Position("Position one", new Department("Department one")));
		
		Classifier classifier1 = new Classifier();
//		classifier1.setIdentifier(1);
		classifier1.setPrice(BigDecimal.valueOf(20.20));
		classifier1.setName("Classifier One");
		
		Classifier classifier2 = new Classifier();
//		classifier2.setIdentifier(2);
		classifier2.setPrice(BigDecimal.valueOf(40.25));
		classifier2.setName("Classifier Two");
		
		Classifier classifier3 = new Classifier();
//		classifier3.setIdentifier(3);
		classifier3.setPrice(BigDecimal.valueOf(30.15));
		classifier3.setName("Classifier Three");
		
		Task task1ForOrder1 = new Task();
		task1ForOrder1.setClassifiers(new HashSet<Classifier>(Arrays.asList(classifier1, classifier2)));
		task1ForOrder1.setAppointedTo(employee);
		task1ForOrder1.setDeadline(ZonedDateTime.of(2020, 10, 15, 10, 30, 0, 0, ZoneId.systemDefault()));
		task1ForOrder1.setName("Task one");
		
		Task task2ForOrder1 = new Task();
//		task2ForOrder1.setIdentifier(10);
		task2ForOrder1.setClassifiers(new HashSet<Classifier>(Arrays.asList(classifier2, classifier3)));
		task2ForOrder1.setAppointedTo(employee);
		task2ForOrder1.setDeadline(ZonedDateTime.of(2020, 5, 12, 12, 30, 0, 0, ZoneId.systemDefault()));
		task2ForOrder1.setName("Task two");
		
		Task task3ForOrder1 = new Task();
		task3ForOrder1.setIdentifier(11);
		task3ForOrder1.setClassifiers(new HashSet<Classifier>(Arrays.asList(classifier1, classifier3)));
		task3ForOrder1.setAppointedTo(employee);
		task3ForOrder1.setDeadline(ZonedDateTime.of(2020, 12, 5, 15, 30, 0, 0, ZoneId.systemDefault()));
		task3ForOrder1.setName("Task three");
		
		Order correctOrder1 = new Order();
		correctOrder1.setDescription("The Correct Order One");
//		correctOrder1.setTasks(new HashSet<Task>(Arrays.asList(task1ForOrder1, task2ForOrder1, task3ForOrder1)));
		correctOrder1.setTasks(new HashSet<Task>(Arrays.asList(task1ForOrder1, task2ForOrder1, task3ForOrder1)));
		correctOrder1.setCreatedFor(user);
		correctOrder1.setDeadline(ZonedDateTime.of(2020, 12, 12, 12, 55, 0, 0, ZoneId.systemDefault()));
		
		JsonServiceUtils jsonServiceUtils = new JsonServiceUtils();
		
		String jsonOrder = jsonServiceUtils.workshopEntityObjectsToJson(correctOrder1);
		
		Order order = jsonServiceUtils.workshopEntityObjectsFromJson(jsonOrder, Order.class);
		
		return jsonOrder;
	}
}