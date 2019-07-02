package internal.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import internal.entities.*;
import internal.service.JsonService;
import internal.service.OrdersService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
class OrdersControllerIT {
	
	@Autowired
	MockMvc mockMvc;
	//	MockMvc mockMvc = MockMvcBuilders.standaloneSetup(OrdersController.class).build();
	@MockBean
	OrdersService ordersService;
	@MockBean
	JsonService jsonService;
	List<Order> orders = new ArrayList<>(10);
	
	
	@Test
	public void init_Context() {
		assertAll(
			() -> assertNotNull(mockMvc),
			() -> assertNotNull(ordersService),
			() -> assertNotNull(jsonService)
		);
	}
	
	@Test
	public void on_Incorrect_Request_Returns_400_Status() throws Exception {
		
		//GIVEN
		
		//Request without obligatory 'size' parameter
		
		//WHEN THEN
		
		mockMvc.perform(
			(MockMvcRequestBuilders.get("/internal/orders/all"))
//				.param("size", "3")
				.param("page", "2"))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().is(400));
	}
	
	@Test
	public void on_Simple_Request_Returns_Orders_List_With_Ok_Status_And_Json_ContentType() throws Exception {
		
		//GIVEN

//		Mockito.when(ordersService.findAllOrders(3, 2, "", Sort.Direction.ASC))
//			.thenReturn(java.util.Optional.ofNullable(orders));
		PageRequest created = PageRequest.of(2, 3, new Sort(Sort.Direction.ASC, "created"));
		
		Mockito.when(ordersService.findAllOrders(Mockito.any(Pageable.class), Mockito.any()))
			.thenReturn(new PageImpl<Order>(orders));
		
		
		String jsonOrders = "[{\"id\":1},{\"id\":2}]";
		
		Mockito.when(jsonService.convertEntitiesToJson(orders)).thenReturn(jsonOrders);
		
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
		
		Mockito.when(ordersService.findAllOrders(3, 2, "created", Sort.Direction.ASC))
			.thenReturn(java.util.Optional.ofNullable(orders));
		Mockito.when(ordersService.findAllOrders(
			PageRequest.of(2, 3, Sort.by(Sort.Direction.ASC, "created")), "created"))
			.thenReturn(new PageImpl<Order>(orders));
		
		String jsonOrders = "[{\"id\":1},{\"id\":2}]";
		
		Mockito.when(jsonService.convertEntitiesToJson(orders)).thenReturn(jsonOrders);
		
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
		"{\"description\":\"The Descr\", \"deadline\":\"2020-6-5\"}"})
	@DisplayName("ControllerAdvice with ExceptionHandler test with no or corrupted JSON inside a Request body")
	public void post_Empty_or_Incorrect_Json_Produces_Bad_Request_Response_With_Predefined_Message(String requestBody)
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
	
	@ParameterizedTest
	@DisplayName("Order may contain incorrect properties")
	@ValueSource(strings = {
		"{\"id\":1,\"description\":\"The Descr\"}",
		"{\"description\":\"The Descr\", \"createdFor\":\"2020\"}"})
	public void post_Incorrect_Order_as_Json_Body_Produces_422UnprocessableEntity_Http_Status(String incorrectJson)
		throws Exception {
		//GIVEN
		//Incorrect jsoned Orders
		ResultActions resultActions = null;
		
		//WHEN
		resultActions = mockMvc.perform(
			MockMvcRequestBuilders
				.post("/internal/orders")
				.accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(incorrectJson));
		
		//THEN
		resultActions
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isInternalServerError())
			.andExpect(MockMvcResultMatchers.content().string("The Order creation failure!"));
	}
	
	@Test
	@DisplayName("Name")
	@WithMockUser(username = "employee@workshop.pro", authorities = {"Admin", "Manager"})
	public void post_Correct_Order_as_Json() throws Exception {
		//GIVEN json Order
		String jsonOrder = getCorrectJsonOrder();
		ResultActions resultActions = null;
		
		//WHEN
		resultActions = mockMvc.perform(
			MockMvcRequestBuilders
				.post("/internal/orders")
				.accept(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(jsonOrder));
		
		//THEN
		resultActions
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isInternalServerError())
			.andExpect(MockMvcResultMatchers.content().string("The Order creation failure!"));
	}
	
	@BeforeEach
	public void init() {
		Order order1 = new Order();
		order1.setId(1);
		order1.setCreated(LocalDateTime.of(2019, 10, 15, 9, 35, 45));
		Order order2 = new Order();
		order2.setId(2);
		order2.setCreated(LocalDateTime.of(2018, 11, 20, 9, 35, 45));
		Order order3 = new Order();
		order3.setId(3);
		order3.setCreated(LocalDateTime.of(2017, 11, 20, 9, 35, 45));
		Order order4 = new Order();
		order4.setId(4);
		order4.setCreated(LocalDateTime.of(2016, 11, 20, 9, 35, 45));
		Order order5 = new Order();
		order5.setId(5);
		order5.setCreated(LocalDateTime.of(2015, 11, 20, 9, 35, 45));
		Order order6 = new Order();
		order6.setId(6);
		order6.setCreated(LocalDateTime.of(2014, 11, 20, 9, 35, 45));
		Order order7 = new Order();
		order7.setId(7);
		order7.setCreated(LocalDateTime.of(2013, 11, 20, 9, 35, 45));
		Order order8 = new Order();
		order8.setId(8);
		order8.setCreated(LocalDateTime.of(2012, 11, 20, 9, 35, 45));
		Order order9 = new Order();
		order9.setId(9);
		order9.setCreated(LocalDateTime.of(2011, 11, 20, 9, 35, 45));
		Order order10 = new Order();
		order10.setId(10);
		order10.setCreated(LocalDateTime.of(2010, 11, 20, 9, 35, 45));
		
		orders.addAll(Arrays.asList(order1, order2, order3, order4, order5, order6, order7, order8, order9, order10));
	}
	
	public String getCorrectJsonOrder() throws IOException {
		
		User user = new User();
		user.setBirthday(LocalDate.of(1960, 10, 5));
		user.setEmail("user@email.pro");
		user.setFirstName("Ivan");
		
		Employee employee = new Employee();
		employee.setId(100);
		employee.setEmail("appointed@workshop.pro");
		
		Classifier classifier1 = new Classifier();
		classifier1.setId(1);
		classifier1.setPrice(BigDecimal.valueOf(20.20));
		classifier1.setName("Classifier One");
		
		Classifier classifier2 = new Classifier();
//		classifier2.setId(2);
		classifier2.setPrice(BigDecimal.valueOf(40.25));
		classifier2.setName("Classifier Two");
		
		Classifier classifier3 = new Classifier();
//		classifier3.setId(3);
		classifier3.setPrice(BigDecimal.valueOf(30.15));
		classifier3.setName("Classifier Three");
		
		Task task1ForOrder1 = new Task();
		task1ForOrder1.setClassifiers(new HashSet<Classifier>(Arrays.asList(classifier1, classifier2)));
		task1ForOrder1.setAppointedTo(employee);
		task1ForOrder1.setDeadline(LocalDateTime.of(2020, 10, 15, 10, 30));
		task1ForOrder1.setName("Task one");
		
		Task task2ForOrder1 = new Task();
//		task2ForOrder1.setId(10);
		task2ForOrder1.setClassifiers(new HashSet<Classifier>(Arrays.asList(classifier2, classifier3)));
		task2ForOrder1.setAppointedTo(employee);
		task2ForOrder1.setDeadline(LocalDateTime.of(2020, 5, 12, 12, 30));
		task2ForOrder1.setName("Task two");
		
		Task task3ForOrder1 = new Task();
		task3ForOrder1.setId(11);
		task3ForOrder1.setClassifiers(new HashSet<Classifier>(Arrays.asList(classifier1, classifier3)));
		task3ForOrder1.setAppointedTo(employee);
		task3ForOrder1.setDeadline(LocalDateTime.of(2020, 12, 5, 15, 30));
		task3ForOrder1.setName("Task three");
		
		Order correctOrder1 = new Order();
		correctOrder1.setDescription("The Correct Order One");
//		correctOrder1.setTasks(new HashSet<Task>(Arrays.asList(task1ForOrder1, task2ForOrder1, task3ForOrder1)));
		correctOrder1.setTasks(new HashSet<Task>(Arrays.asList(task1ForOrder1, task2ForOrder1, task3ForOrder1)));
		correctOrder1.setCreatedFor(user);
		correctOrder1.setDeadline(LocalDateTime.of(2020, 12, 12, 12, 55));
		
		JsonService jsonService = new JsonService();
		
		String jsonOrder = jsonService.convertEntityToJson(correctOrder1);
		
		Order order = jsonService.convertEntityFromJson(jsonOrder, Order.class);
		
		return jsonOrder;
	}
}