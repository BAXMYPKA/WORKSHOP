package workshop.internal.controllers;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Order;
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
import workshop.internal.dao.DepartmentsDao;
import workshop.internal.entities.Classifier;
import workshop.internal.entities.Department;
import workshop.internal.entities.Position;
import workshop.internal.entities.Task;
import workshop.internal.services.*;

import java.math.BigDecimal;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {DepartmentsService.class, DepartmentsController.class, DepartmentsDao.class,
	PositionsController.class, PositionsService.class, TasksService.class, OrdersService.class,
	ClassifiersController.class, ClassifiersService.class})
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
	@WithMockUser(username = "employee@workshop.pro", authorities = {"Admin", "Manager"})
	public void inherited_Method_getOne_Should_Return_One_WorkshopEntity() throws Exception {
		//GIVEN
		departmentOne = new Department("Department unique one");
		departmentOne = departmentsService.persistEntity(departmentOne);
		
		long departmentId = departmentOne.getIdentifier();
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request("GET", URI.create("/workshop/internal/departments/" + departmentId));
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(request);
		
		//THEN
		resultActions
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers
				.content().string(Matchers.containsString("\"name\":\"Department unique one\"")));
	}
	
	@Test
	@WithMockUser(username = "employee@workshop.pro", authorities = {"Admin", "Manager"})
	public void inherited_Method_getAll_Should_Return_All_Default_Paged_WorkshopEntities() throws Exception {
		//GIVEN
		positionOne = new Position("Position unique one", departmentOne);
		positionTwo = new Position("Position unique two", departmentOne);
		
		positionOne = positionsService.persistEntity(positionOne);
		positionTwo = positionsService.persistEntity(positionTwo);
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(
			"GET",
			URI.create("/workshop/internal/positions"));
		
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
}