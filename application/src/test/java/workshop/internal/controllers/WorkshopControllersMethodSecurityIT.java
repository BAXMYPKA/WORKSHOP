package workshop.internal.controllers;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import workshop.internal.entities.Classifier;
import workshop.internal.entities.Order;
import workshop.internal.entities.Task;
import workshop.internal.services.ClassifiersService;
import workshop.internal.services.OrdersService;
import workshop.internal.services.TasksService;
import workshop.internal.services.serviceUtils.JsonServiceUtils;
import workshop.security.EmployeesDetailsService;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(SpringExtension.class)
@SpringBootTest
@PropertySource("classpath:applicationTest.properties")
@AutoConfigureMockMvc
@DirtiesContext
public class WorkshopControllersMethodSecurityIT {
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ClassifiersService classifiersService;
	
	@Autowired
	private TasksService tasksService;
	
	@Autowired
	private OrdersService ordersService;
	
	@Autowired
	private JsonServiceUtils jsonServiceUtils;
	
	@Test
	public void initTest() {
		assertAll(
			() -> assertNotNull(mockMvc),
			() -> assertNotNull(tasksService));
	}
	
	@Test
	@WithMockUser(username = "employee@workshop.pro", authorities = {"Manager", "Admin"})
	public void methodSecurity_With_Wrong_Authority_Admin_Should_Just_Return_Status401() {
		//GIVEN
		Classifier classifier = new Classifier();
		classifier.setName("Classifier 0");
		classifiersService.persistEntity(classifier);
		
		Order order = new Order();
		order.setDescription("Order 0");
		ordersService.persistEntity(order);
		
		Task task = new Task();
		task.setName("Task 0");
		task.setOrder(order);
		task.addClassifier(classifier);
		tasksService.persistEntity(task);
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(
			"GET",
			URI.create("/internal/classifiers/" + classifier.getIdentifier() + "/tasks/"))
			.accept(MediaTypes.HAL_JSON_UTF8);
		
		//THEN
		assertDoesNotThrow(() ->
			mockMvc.perform(request)
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers
					.status().isNotFound())
				.andExpect(MockMvcResultMatchers
					.content().contentType(MediaType.APPLICATION_JSON_UTF8)));
	}
	
	@Test
	@WithMockUser(username = "employee@workshop.pro", authorities = {"ADMIN_READ"})
	public void methodSecurity_With_AdminRead_Authority_Should_Grant_Access_To_Read_Classifier() throws Exception {
		//GIVEN
		Classifier classifier = new Classifier();
		classifier.setName("Classifier to read");
		classifiersService.persistEntity(classifier);
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(
			"GET",
			URI.create("/internal/classifiers/" + classifier.getIdentifier()))
			.contentType(MediaType.APPLICATION_JSON_UTF8)
			.accept(MediaTypes.HAL_JSON_UTF8);
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(request);
		
		//THEN
		resultActions
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers
				.content().string(Matchers.containsString("\"name\":\"Classifier to read\"")));
		
	}
	
	@Test
	@WithMockUser(username = "employee@workshop.pro", authorities = {"ADMIN_FULL"})
	public void methodSecurity_With_AdminFull_Authority_Should_Grant_Access_To_Read_ClassifierTasks() throws Exception {
		//GIVEN
		Classifier classifier = new Classifier();
		classifier.setName("Classifier 1");
		classifiersService.persistEntity(classifier);
		
		Order order = new Order();
		order.setDescription("Order 1");
		ordersService.persistEntity(order);
		
		Task task = new Task();
		task.setName("Task 1");
		task.setOrder(order);
		task.addClassifier(classifier);
		tasksService.persistEntity(task);
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(
			"GET",
			URI.create("/internal/classifiers/" + classifier.getIdentifier() + "/tasks/"))
			.accept(MediaTypes.HAL_JSON_UTF8);
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(request);
		
		//THEN
		resultActions
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers
				.content().string(Matchers.containsString("\"name\":\"Task 1\"")));
		
	}
	
	@Test
	@WithMockUser(username = "employee@workshop.pro", authorities = {"EMPLOYEE"})
	public void methodSecurity_Without_Administrator_Authority_Should_Not_Grant_Access_To_Read_ClassifierTasks() throws Exception {
		//GIVEN
		Classifier classifier = new Classifier();
		classifier.setName("Classifier 2");
		classifiersService.persistEntity(classifier);
		
		Order order = new Order();
		order.setDescription("Order 2");
		ordersService.persistEntity(order);
		
		Task task = new Task();
		task.setName("Task 2");
		task.setOrder(order);
		task.addClassifier(classifier);
		tasksService.persistEntity(task);
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(
			"GET",
			URI.create("/internal/classifiers/" + classifier.getIdentifier() + "/tasks/"))
			.accept(MediaTypes.HAL_JSON_UTF8);
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(request);
		
		//THEN
		resultActions
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers
				.status().isNotFound())
			.andExpect(MockMvcResultMatchers
				.content().contentType(MediaType.APPLICATION_JSON_UTF8));
	}
	
	@Test
	@WithMockUser(username = "employee@workshop.pro", authorities = {"ADMIN_FULL"})
	public void methodSecurity_With_AdminFull_Authority_Should_Grant_Access_To_Put_Classifier() throws Exception {
		//GIVEN
		Classifier classifier = new Classifier();
		classifier.setName("Classifier new 11");
		
		String jsonClassifier = jsonServiceUtils.workshopEntityObjectsToJson(classifier);
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(
			"POST",
			URI.create("/internal/classifiers"))
			.contentType(MediaType.APPLICATION_JSON_UTF8)
			.content(jsonClassifier)
			.accept(MediaTypes.HAL_JSON_UTF8);
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(request);
		
		//THEN
		resultActions
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers
				.content().string(Matchers.containsString("\"name\":\"Classifier new 11\"")));
		
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", authorities = {"EMPLOYEE_READ", "WORKSHOP_READ", "WORKSHOP_FULL"},
		password = "12345")
	public void methodSecurity_With_WorkshopFull_Should_Grant_Access_To_Post_Classifier() throws Exception {
		//GIVEN
		Classifier classifier = new Classifier();
		classifier.setName("Classifier new 2");
		
		String jsonClassifier = jsonServiceUtils.workshopEntityObjectsToJson(classifier);
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(
			"POST",
			URI.create("/internal/classifiers"))
			.contentType(MediaType.APPLICATION_JSON_UTF8)
			.content(jsonClassifier)
			.accept(MediaTypes.HAL_JSON_UTF8);
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(request);
		
		//THEN
		resultActions
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers
				.content().string(Matchers.containsString("\"name\":\"Classifier new 2\"")));
		
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345",
		authorities = {"EMPLOYEE_READ", "ADMIN_READ", "ADMIN_WRITE", "WORKSHOP_READ", "WORKSHOP_WRITE", "HR_READ", "HR_WRITE"})
	public void methodSecurity_With_Non_Post_Permission_For_Classifier_Should_Not_Grant_Access_To_Post_One() throws Exception {
		//GIVEN
		Classifier classifier = new Classifier();
		classifier.setName("Classifier new 3");
		
		String jsonClassifier = jsonServiceUtils.workshopEntityObjectsToJson(classifier);
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(
			"POST",
			URI.create("/internal/classifiers"))
			.contentType(MediaType.APPLICATION_JSON_UTF8)
			.content(jsonClassifier)
			.accept(MediaTypes.HAL_JSON_UTF8);
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(request);
		
		//THEN
		resultActions
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isUnauthorized());
		
	}
	
}
