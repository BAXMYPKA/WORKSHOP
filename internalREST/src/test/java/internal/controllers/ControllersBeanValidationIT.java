package internal.controllers;

import internal.entities.*;
import internal.service.serviceUtils.JsonServiceUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
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
import java.net.URI;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;

//TODO: to set a localization Cookie

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
class ControllersBeanValidationIT {
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private JsonServiceUtils jsonServiceUtils;
	
	@Test
	public void testInit() {
		assertNotNull(mockMvc);
	}
	
	@ParameterizedTest
	@MethodSource("getEntitiesToPersistWithId")
	@WithMockUser(username = "employee@workshop.pro", authorities = {"Admin"})
	public void persist_Entities_With_Id_Set_Should_Return_UnprocessableEntity_HttpCode(WorkshopEntity entity) throws Exception {
		//GIVEN
		Trackable trackable = null;
		String urlToPersist = "";
		if ("Order".equals(entity.getClass().getSimpleName())) {
			trackable = (Order) entity;
			urlToPersist = "/internal/orders";
		}
		String jsonEntity = jsonServiceUtils.convertEntityToJson(trackable);
		
		//WHEN
		Employee employee = Employee.builder()
			.email("employee@workshop.pro").firstName("FN").lastName("LN").birthday(LocalDate.now().minusYears(18)).build();
		ResultActions perform = mockMvc.perform(MockMvcRequestBuilders
			.request(HttpMethod.POST, urlToPersist)
			.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
			.content(jsonEntity));
		
		//THEN
		perform.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
			.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
			.andExpect(MockMvcResultMatchers.jsonPath("$..identifier", Matchers.hasSize(1)));
	}
	
	@ParameterizedTest
	@MethodSource("getSimpleEntitiesToPersistWithErrors")
	@DisplayName("Controller should return the HttpStatusCode 422 with a Json body with exact FieldError name and value as:" +
		"['fieldName:fieldError'] content like")
	public void persist_Simple_Entities_With_Errors_Should_Return_UnprocessableEntity_HttpStatusCode(WorkshopEntity entity, String uri)
		throws Exception {
		//GIVEN
		Trackable trackable = null;
		if ("Order".equals(entity.getClass().getSimpleName())) {
			trackable = (Order) entity;
		}
		String jsonEntity = jsonServiceUtils.convertEntityToJson(trackable);
		
		//WHEN
		ResultActions perform = mockMvc.perform(MockMvcRequestBuilders
			.request(HttpMethod.POST, uri)
			.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
			.content(jsonEntity));
		
		//THEN
		perform.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
			.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
			//Created has to be PastOrPresent
			.andExpect(MockMvcResultMatchers.jsonPath("$..created", Matchers.hasSize(1)))
			//Finished can only be PresentOrPast
			.andExpect(MockMvcResultMatchers.jsonPath("$..finished", Matchers.hasSize(1)))
			//Modified must be null
			.andExpect(MockMvcResultMatchers.jsonPath("$..modified", Matchers.hasSize(1)))
			//Deadline can only be in the Future
			.andExpect(MockMvcResultMatchers.jsonPath("$..deadline", Matchers.hasSize(1)))
			//OverallPrice can be zero or greater
			.andExpect(MockMvcResultMatchers.jsonPath("$..overallPrice", Matchers.hasSize(1)));
	}
	
	@Test
	@DisplayName("Controller should return the HttpStatusCode 422 with the Json body with multiple included FieldErrors" +
		"for cascading persistence of including entities graph." +
		"Also checks the 'PersistenceCheck.class' validation group" +
		"Also checks CascadeType.PERSIST for all the included Entities.")
	@WithMockUser(username = "employee@workshop.pro", roles = {"Administrator"})
	public void persist_New_Entities_With_Errors_With_CascadeType_Persist() throws Exception {
		//GIVEN
		Order orderWithGraphErrors = new Order();
		orderWithGraphErrors.setModified(ZonedDateTime.now().minusHours(1)); //Null constraint violation
		
		Classifier classifier = new Classifier();
		classifier.setName(""); //NotBlank constraint violation
		classifier.setPrice(new BigDecimal("-25.68"));//NegativeOrZero constraint violation
		
		Task task = new Task();
		task.setDeadline(ZonedDateTime.now()); //Future constraint violation
		task.setCreated(ZonedDateTime.now().plusMinutes(3)); //PresentOrPast constraint violation
		
		Employee employee = new Employee();
		employee.setEmail(null);
		employee.setFirstName("");
//		employee.setLastName(null); //Not set
		
		User user = new User();
		user.setIdentifier(3L); //If CascadeType.PERSIST - must be zero or null
		user.setModified(ZonedDateTime.now()); //Must by null while persisting as sets automatically
		
		Phone phone = new Phone();
		phone.setIdentifier(2L);
		phone.setPhone("");
		
		user.setPhones(Collections.singleton(phone));
		orderWithGraphErrors.setCreatedFor(user);
		task.setAppointedTo(employee);
		task.setClassifiers(Collections.singleton(classifier));
		orderWithGraphErrors.setTasks(Collections.singleton(task));
		
		String json = jsonServiceUtils.convertEntityToJson(orderWithGraphErrors);
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(
			MockMvcRequestBuilders
				.request("POST", new URI("/internal/orders"))
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(json));
		
		//THEN ERRORS HAVE TO BE EXPECTED
		resultActions.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
			.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
			//Order's not null modified
			.andExpect(MockMvcResultMatchers.jsonPath("$..modified", Matchers.hasSize(1)))
			//Order.Set<Task> as the FieldError 'tasks[].deadline'
			.andExpect(MockMvcResultMatchers.jsonPath("$..['tasks[].deadline']", Matchers.hasSize(1)))
			//Order.Set<Task> 'created' field error as 'tasks[].created'
			.andExpect(MockMvcResultMatchers.jsonPath("$..['tasks[].created']", Matchers.hasSize(1)))
			//Order.Task.Set<Classifier> 'name' blank value
			.andExpect(MockMvcResultMatchers.jsonPath("$..['tasks[].classifiers[].name']", Matchers.hasSize(1)))
			//Order.Task.Set<Classifier> 'price' negative value
			.andExpect(MockMvcResultMatchers.jsonPath("$..['tasks[].classifiers[].price']", Matchers.hasSize(1)))
			//Order.User 'modified' has to be null
			.andExpect(MockMvcResultMatchers.jsonPath("$..['createdFor.modified']", Matchers.hasSize(1)))
			//Order.User.Set<Phone> 'phone' min 5 max 15 digits
			.andExpect(MockMvcResultMatchers.jsonPath("$..['createdFor.phones[].phone']", Matchers.hasSize(1)));
	}
	
	public static Stream<Arguments> getEntitiesToPersistWithId() {
		Order order1 = new Order();
		order1.setIdentifier(1);
		
		Order order2 = new Order();
		order2.setIdentifier(2);
		
		//TODO: User, Task, Classifier etc...
		
		return Stream.of(Arguments.of(order1), Arguments.of(order2));
	}
	
	/**
	 * @return Trackable entities with errors for validation.
	 * URI = String with uri to controller which has to validate those entities.
	 */
	public static Stream<Arguments> getSimpleEntitiesToPersistWithErrors() {
		String ordersUri = "/internal/orders";
		Order order = new Order();
		order.setCreated(ZonedDateTime.now().plusMinutes(1));
		order.setModified(ZonedDateTime.now().minusHours(1));
		order.setFinished(ZonedDateTime.now().plusMinutes(10));
		order.setDeadline(ZonedDateTime.now().minusMinutes(1));
		order.setOverallPrice(new BigDecimal("-1.2"));
		
		
		return Stream.of(Arguments.of(order, ordersUri));
	}
	
	public Order getCorrectJsonOrder() throws IOException {
		
		User user = new User();
		user.setBirthday(LocalDate.of(1960, 10, 5));
		user.setEmail("user@email.pro");
		user.setFirstName("Ivan");
		
		Employee employee = new Employee();
		employee.setIdentifier(100);
		employee.setEmail("appointed@workshop.pro");
		
		Classifier classifier1 = new Classifier();
		classifier1.setIdentifier(1);
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
		task3ForOrder1.setDeadline(ZonedDateTime.of(2020, 12, 5, 15, 300, 0, 0, ZoneId.systemDefault()));
		task3ForOrder1.setName("Task three");
		
		Order correctOrder1 = new Order();
		correctOrder1.setDescription("The Correct Order One");
//		correctOrder1.setTasks(new HashSet<Task>(Arrays.asList(task1ForOrder1, task2ForOrder1, task3ForOrder1)));
		correctOrder1.setTasks(new HashSet<Task>(Arrays.asList(task1ForOrder1, task2ForOrder1, task3ForOrder1)));
		correctOrder1.setCreatedFor(user);
		correctOrder1.setDeadline(ZonedDateTime.of(2020, 12, 12, 12, 550, 0, 0, ZoneId.systemDefault()));
		
		return correctOrder1;
	}
}