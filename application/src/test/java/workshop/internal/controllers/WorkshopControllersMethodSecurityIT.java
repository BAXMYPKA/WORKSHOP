package workshop.internal.controllers;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
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

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class WorkshopControllersMethodSecurityIT {
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ClassifiersService classifiersService;
	
	@Autowired
	private TasksService tasksService;
	
	@Autowired
	private OrdersService ordersService;
	
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
					.status().isUnauthorized())
				.andExpect(MockMvcResultMatchers
					.content().contentType(MediaType.APPLICATION_JSON_UTF8)));
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
	public void methodSecurity_Without_Administrator_Authority_Should_Dont_Grant_Access_To_Read_ClassifierTasks() throws Exception {
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
				.status().isUnauthorized())
			.andExpect(MockMvcResultMatchers
				.content().contentType(MediaType.APPLICATION_JSON_UTF8));
	}
	
}
