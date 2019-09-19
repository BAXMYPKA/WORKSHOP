package workshop.internal.controllers;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import workshop.internal.entities.Classifier;
import workshop.internal.entities.Task;
import workshop.internal.services.ClassifiersService;
import workshop.internal.services.OrdersService;
import workshop.internal.services.TasksService;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@ExtendWith(SpringExtension.class)
@SpringBootTest
//@ContextConfiguration(classes = SecurityTestApplication.class)
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
	@WithMockUser(username = "employee@workshop.pro", authorities = {"Admin", "Manager"})
	public void inheritedddd() throws Exception {
		//GIVEN
		Classifier classifier = new Classifier();
		classifier.setName("Classifier 1");
		classifiersService.persistEntity(classifier);
		
		workshop.internal.entities.Order order = new workshop.internal.entities.Order();
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
	
}
