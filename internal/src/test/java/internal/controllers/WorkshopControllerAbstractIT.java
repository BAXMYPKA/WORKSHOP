package internal.controllers;

import workshop.internal.controllers.DepartmentsController;
import workshop.internal.controllers.PositionsController;
import workshop.internal.entities.Department;
import workshop.internal.entities.Position;
import workshop.internal.services.DepartmentsService;
import workshop.internal.services.PositionsService;
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

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class WorkshopControllerAbstractIT {
	
	@Autowired
	DepartmentsController departmentsController;
	@Autowired
	PositionsController positionsController;
	@Autowired
	DepartmentsService departmentsService;
	@Autowired
	PositionsService positionsService;
	@Autowired
	MockMvc mockMvc;
	Department departmentOne;
	Position positionOne;
	Position positionTwo;
	
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
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request("GET", URI.create("/internal/departments/" + departmentId));
		
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
			URI.create("/internal/positions"));
		
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