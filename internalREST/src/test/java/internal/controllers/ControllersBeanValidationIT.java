package internal.controllers;

import internal.entities.*;
import internal.service.JsonService;
import org.hamcrest.Matchers;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
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
	private JsonService jsonService;
	
/*
	@MockBean
	private EmployeesDao employeesDao;
	@MockBean
	private DaoAbstract daoAbstract;
	
	@BeforeEach
	private void init() {
		Employee employee = new Employee();
		employee.setId(500);
		employee.setEmail("employee@workshop.pro");
		Mockito.lenient().when(daoAbstract.findByEmail("employee@workshop.pro")).thenReturn(Optional.of(employee));
	}
*/
	
	@Test
	public void testInit() {
		assertNotNull(mockMvc);
	}
	
	@ParameterizedTest
	@MethodSource("getEntitiesToPersistWithId")
	@WithMockUser(username = "employee@workshop.pro", authorities = {"Admin"})
	public void persist_Entities_With_Id_Set(WorkshopEntity entity) throws Exception {
		//GIVEN
		Trackable trackable = null;
		String urlToPersist = "";
		if ("Order".equals(entity.getClass().getSimpleName())) {
			trackable = (Order) entity;
			urlToPersist = "/internal/orders";
		}
		String jsonEntity = jsonService.convertEntityToJson(trackable);
		
		//WHEN
		ResultActions perform = mockMvc.perform(MockMvcRequestBuilders
			.request(HttpMethod.POST, urlToPersist)
			.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
			.content(jsonEntity));
		
		//THEN
		perform.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
			.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
			.andExpect(MockMvcResultMatchers.jsonPath("$..id", Matchers.hasSize(1)));
	}
	
	@ParameterizedTest
	@MethodSource("getEntitiesToPersistWithErrors")
	public void persist_Entities_With_Errors(WorkshopEntity entity, String uri) throws Exception {
		//GIVEN
		Trackable trackable = null;
		if ("Order".equals(entity.getClass().getSimpleName())) {
			trackable = (Order) entity;
		}
		String jsonEntity = jsonService.convertEntityToJson(trackable);
		
		//WHEN
		ResultActions perform = mockMvc.perform(MockMvcRequestBuilders
			.request(HttpMethod.POST, uri)
			.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
			.content(jsonEntity));
		
		//THEN
		perform.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
			.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
			.andExpect(MockMvcResultMatchers.jsonPath("$..finished", Matchers.hasSize(1)))
			.andExpect(MockMvcResultMatchers.jsonPath("$..modified", Matchers.hasSize(1)));
	}
	
	public void update_Entities() {
		//
	}
	
	public static Stream<Arguments> getEntitiesToPersistWithId() {
		Order order1 = new Order();
		order1.setId(1);
		
		Order order2 = new Order();
		order2.setId(2);
		
		//TODO: User, Task, Classifier etc...
		
		return Stream.of(Arguments.of(order1), Arguments.of(order2));
	}
	
	public static Stream<Arguments> getEntitiesToPersistWithErrors() {
		String ordersUri = "/internal/orders";
		Order order = new Order();
		order.setModified(LocalDateTime.now().minusHours(1));
		order.setFinished(LocalDateTime.now().plusMinutes(10));
		
		return Stream.of(Arguments.of(order, ordersUri));
	}
	
	public Order getCorrectJsonOrder() throws IOException {
		
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
		
		return correctOrder1;
	}
}