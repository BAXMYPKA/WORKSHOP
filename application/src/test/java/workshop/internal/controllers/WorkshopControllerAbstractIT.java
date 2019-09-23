package workshop.internal.controllers;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Order;
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
import workshop.internal.entities.Department;
import workshop.internal.entities.Position;
import workshop.internal.services.*;
import workshop.internal.services.serviceUtils.JsonServiceUtils;

import java.math.BigDecimal;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class WorkshopControllerAbstractIT {
	
	@Autowired
	private DepartmentsController departmentsController;
	@Autowired
	private PositionsController positionsController;
	@Autowired
	private DepartmentsService departmentsService;
	@Autowired
	private PositionsService positionsService;
	@Autowired
	private ClassifiersService classifiersService;
	@Autowired
	private TasksService tasksService;
	@Autowired
	private OrdersService ordersService;
	@Autowired
	private ClassifiersController classifiersController;
	@Autowired
	private JsonServiceUtils jsonServiceUtils;
	@Autowired
	private MockMvc mockMvc;
	private Department departmentOne;
	private Position positionOne;
	private Position positionTwo;
	
	@Test
	@Order(1)
	public void init_Test() {
		
		assertAll(
			() -> assertNotNull(mockMvc),
			() -> assertNotNull(departmentsController),
			() -> assertNotNull(positionsController),
			() -> assertNotNull(departmentsService),
			() -> assertNotNull(positionsService)
		);
	}
	
	
	@Test
	@Order(2)
	@WithMockUser(username = "employee@workshop.pro", authorities = {"ADMIN_FULL"})
	public void inherited_Method_getOne_Should_Return_One_WorkshopEntity() throws Exception {
		//GIVEN
		departmentOne = new Department("Department unique one");
		departmentOne = departmentsService.persistEntity(departmentOne);
		
		long departmentId = departmentOne.getIdentifier();
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(
			"GET", URI.create("/internal/departments/" + departmentId))
			.accept(MediaTypes.HAL_JSON_UTF8);
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(request);
		
		//THEN
		resultActions
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers
				.content().string(Matchers.containsString("\"name\":\"Department unique one\"")));
	}
	
	@Test
	@Order(3)
	@WithMockUser(username = "employee@workshop.pro", authorities = {"ADMIN_FULL"})
	public void inherited_Method_getAll_Should_Return_All_Default_Paged_WorkshopEntities() throws Exception {
		//GIVEN
		departmentOne = new Department("Department unique");
		departmentOne = departmentsService.persistEntity(departmentOne);
		
		positionOne = new Position("Position unique one", departmentOne);
		positionTwo = new Position("Position unique two", departmentOne);
		
		positionOne = positionsService.persistEntity(positionOne);
		positionTwo = positionsService.persistEntity(positionTwo);
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(
			"GET",
			URI.create("/internal/positions"))
			.accept(MediaTypes.HAL_JSON_UTF8);
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(request);
		
		//THEN
		resultActions
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers
				.content().string(Matchers.containsString("\"name\":\"Position unique one\"")))
			.andExpect(MockMvcResultMatchers
				.content().string(Matchers.containsString("\"name\":\"Position unique two\"")));
	}
	
	@Test
	@Order(4)
	@WithMockUser(username = "employee@workshop.pro", authorities = {"ADMIN_FULL"})
	public void inherited_Method_Post_Should_Return_Persisted_Classifier() throws Exception {
		//GIVEN
		Classifier classifier = new Classifier();
		classifier.setName("Classifier new 1");
		classifier.setPrice(BigDecimal.TEN);
		
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
				.content().string(Matchers.containsString("\"name\":\"Classifier new 1\"")));
		
	}
	
	@Test
	@Order(5)
	@WithMockUser(username = "employee@workshop.pro", authorities = {"ADMIN_FULL"})
	public void inherited_Method_Put_Should_Return_Updated_Classifier() throws Exception {
		//GIVEN
		Classifier classifier = new Classifier();
		classifier.setName("Classifier not new 1");
		classifier.setPrice(BigDecimal.TEN);
		classifiersService.persistEntity(classifier);
		
		classifier.setName("Classifier not new again");
		
		String jsonClassifier = jsonServiceUtils.workshopEntityObjectsToJson(classifier);
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(
			"PUT",
			URI.create("/internal/classifiers/" + classifier.getIdentifier()))
			.contentType(MediaType.APPLICATION_JSON_UTF8)
			.content(jsonClassifier)
			.accept(MediaTypes.HAL_JSON_UTF8);
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(request);
		
		//THEN
		resultActions
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers
				.content().string(Matchers.containsString("\"name\":\"Classifier not new again\"")))
			.andExpect(MockMvcResultMatchers
				.content().string(Matchers
					.not(Matchers.containsString("\"name\":\"Classifier not new 1\""))));
		
	}
	
	
}