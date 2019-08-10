package internal.controllers;

import internal.entities.Department;
import internal.entities.Position;
import internal.service.DepartmentsService;
import internal.service.PositionsService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class HateoasIT {
	
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
	public void single_WorkshopEntity_Should_Contain_SelfLink() throws Exception {
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
				.content().string(Matchers.containsString("\"name\":\"Department unique one\"")))
			.andExpect(MockMvcResultMatchers
				.content().string(Matchers.containsString("\"links\":[{\"rel\":\"self\"")))
			.andExpect(MockMvcResultMatchers
				.content().string(Matchers.containsString(
					"\"href\":\"http://localhost/internal/departments/" + departmentId)));
	}
	
	@Test
	@WithMockUser(username = "employee@workshop.pro", authorities = {"Admin", "Manager"})
	public void all_defaultPage_Should_Contain_SelfLinks_And_currentPageLink() throws Exception {
		//GIVEN
		positionsController.setDEFAULT_PAGE_SIZE(2);
		
		positionOne = new Position("Position unique one", departmentOne);
		positionTwo = new Position("Position unique two", departmentOne);
		
		positionOne = positionsService.persistEntity(positionOne);
		positionTwo = positionsService.persistEntity(positionTwo);
		
		long positionOneId = positionOne.getIdentifier();
		long positionTwoId = positionTwo.getIdentifier();
		//Default request without parameters
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request("GET", URI.create("/internal/positions/"));
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(request);
		
		//THEN
		resultActions
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers
				.content().string(Matchers.containsString("\"name\":\"Position unique one\"")))
			.andExpect(MockMvcResultMatchers
				.content().string(Matchers.containsString(
					"\"rel\":\"self\",\"href\":\"http://localhost/internal/positions/" + positionOneId)))
			.andExpect(MockMvcResultMatchers
				.content().string(Matchers.containsString("\"name\":\"Position unique two\"")))
			.andExpect(MockMvcResultMatchers
				.content().string(Matchers.containsString(
					"\"rel\":\"self\",\"href\":\"http://localhost/internal/positions/" + positionTwoId)))
			.andExpect(MockMvcResultMatchers
				.content().string(Matchers.containsString(
					"\"rel\":\"currentPage\",\"href\":\"http://localhost/internal/positions")));
	}
	
	@Test
	@WithMockUser(username = "employee@workshop.pro", authorities = {"Admin", "Manager"})
	@Transactional
	public void firstPage_Should_Contain_Only_CurrentPageLink_NextPageLink_LastPageLink() throws Exception {
		//GIVEN
		int totalPersisted = totalPersistedPositionsForPaginationTests();
		//Retrieve first page of 4 with 3 elements of 10 total elements
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(
			"GET", URI.create("/internal/positions?pageSize=3&pageNum=1"));
		
		//WHEN
		int totalRetrieved = positionsService.findAllEntities(0, 0, null, Sort.Direction.DESC).size();
		
		ResultActions resultActions = mockMvc.perform(request);
		
		//THEN
		assertEquals(totalPersisted, totalRetrieved);
		
		resultActions
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers
				.content().string(Matchers.containsString(
					"\"rel\":\"currentPage\",\"href\":\"http://localhost/internal/positions?pageSize=3&pageNum=1")))
			.andExpect(MockMvcResultMatchers
				.content().string(Matchers.containsString(
					"\"rel\":\"nextPage\",\"href\":\"http://localhost/internal/positions?pageSize=3&pageNum=2")))
			.andExpect(MockMvcResultMatchers
				.content().string(Matchers.containsString(
					"\"rel\":\"lastPage\",\"href\":\"http://localhost/internal/positions?pageSize=3&pageNum=4")))
			.andExpect(MockMvcResultMatchers
				.content().string(Matchers.not(Matchers.containsString(
					"\"rel\":\"previousPage\"")))).andExpect(MockMvcResultMatchers
			.content().string(Matchers.not(Matchers.containsString(
				"\"rel\":\"firstPage\""))));
		
	}
	
	@Test
	@WithMockUser(username = "employee@workshop.pro", authorities = {"Admin", "Manager"})
	@Transactional
	public void secondPage_Should_Contain_CurrentPageLink_PreviousPageLink_NextPageLink_LastPageLink_FirstPageLink()
		throws Exception {
		//GIVEN
		int totalPersisted = totalPersistedPositionsForPaginationTests();
		//Retrieve first page of 4 with 3 elements of 10 total elements
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(
			"GET", URI.create("/internal/positions?pageSize=3&pageNum=2"));
		
		//WHEN
		int totalRetrieved = positionsService.findAllEntities(0, 0, null, Sort.Direction.DESC).size();
		
		ResultActions resultActions = mockMvc.perform(request);
		
		//THEN
		assertEquals(totalPersisted, totalRetrieved);
		
		resultActions
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers
				.content().string(Matchers.containsString(
					"\"rel\":\"currentPage\",\"href\":\"http://localhost/internal/positions?pageSize=3&pageNum=2")))
			.andExpect(MockMvcResultMatchers
				.content().string(Matchers.containsString(
					"\"rel\":\"nextPage\",\"href\":\"http://localhost/internal/positions?pageSize=3&pageNum=3")))
			.andExpect(MockMvcResultMatchers
				.content().string(Matchers.containsString(
					"\"rel\":\"previousPage\",\"href\":\"http://localhost/internal/positions?pageSize=3&pageNum=1")))
			.andExpect(MockMvcResultMatchers
				.content().string(Matchers.containsString(
					"\"rel\":\"firstPage\",\"href\":\"http://localhost/internal/positions?pageSize=3&pageNum=1")))
			.andExpect(MockMvcResultMatchers
				.content().string(Matchers.containsString(
					"\"rel\":\"lastPage\",\"href\":\"http://localhost/internal/positions?pageSize=3&pageNum=4")));
	}
	
	@Test
	@WithMockUser(username = "employee@workshop.pro", authorities = {"Admin", "Manager"})
	@Transactional
	public void lastPage_Should_Contain_Only_CurrentPageLink_PreviousPageLink_FirstPageLink()
		throws Exception {
		//GIVEN
		int totalPersisted = totalPersistedPositionsForPaginationTests();
		//Retrieve first page of 4 with 3 elements of 10 total elements
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(
			"GET", URI.create("/internal/positions?pageSize=3&pageNum=4"));
		
		//WHEN
		int totalRetrieved = positionsService.findAllEntities(0, 0, null, Sort.Direction.DESC).size();
		
		ResultActions resultActions = mockMvc.perform(request);
		
		//THEN
		assertEquals(totalPersisted, totalRetrieved);
		
		resultActions
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers
				.content().string(Matchers.containsString(
					"\"rel\":\"currentPage\",\"href\":\"http://localhost/internal/positions?pageSize=3&pageNum=4")))
			.andExpect(MockMvcResultMatchers
				.content().string(Matchers.containsString(
					"\"rel\":\"previousPage\",\"href\":\"http://localhost/internal/positions?pageSize=3&pageNum=3")))
			.andExpect(MockMvcResultMatchers
				.content().string(Matchers.containsString(
					"\"rel\":\"firstPage\",\"href\":\"http://localhost/internal/positions?pageSize=3&pageNum=1")))
			
			.andExpect(MockMvcResultMatchers
				.content().string(Matchers.not
					(Matchers.containsString("\"rel\":\"lastPage\""))))
			.andExpect(MockMvcResultMatchers.content().string(
				Matchers.not(Matchers.containsString("\"rel\":\"nextPage\""))));
	}
	
	@Test
	@WithMockUser(username = "employee@workshop.pro", authorities = {"Admin", "Manager"})
	@Transactional
	public void lastPage_Should_Contain_Single_WorkshopEntity()
		throws Exception {
		//GIVEN
		int totalPersisted = totalPersistedPositionsForPaginationTests();
		//Retrieve first page of 4 with 3 elements of 10 total elements
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(
			"GET", URI.create("/internal/positions?pageSize=3&pageNum=4"));
		
		//WHEN
		int totalRetrieved = positionsService.findAllEntities(0, 0, null, Sort.Direction.DESC).size();
		
		ResultActions resultActions = mockMvc.perform(request);
		
		//THEN
		assertEquals(totalPersisted, totalRetrieved);
		
		resultActions
			.andDo(MockMvcResultHandlers.print())
			
			.andExpect(MockMvcResultMatchers
				.content().string(Matchers.not
					(Matchers.containsString("\"rel\":\"lastPage\""))))
			.andExpect(MockMvcResultMatchers.content().string(
				Matchers.not(Matchers.containsString("\"rel\":\"nextPage\""))));
	}
	
	private int totalPersistedPositionsForPaginationTests() {
		//DO NOT ADD NEW POSITIONS!
		positionsService.removeEntities(positionsService.findAllEntities(0, 0, null, Sort.Direction.DESC));
		
		positionOne = new Position("Position unique 1", departmentOne);
		positionTwo = new Position("Position unique 2", departmentOne);
		Position position3 = new Position("Position unique 3", departmentOne);
		Position position4 = new Position("Position unique 4", departmentOne);
		Position position5 = new Position("Position unique 5", departmentOne);
		Position position6 = new Position("Position unique 6", departmentOne);
		Position position7 = new Position("Position unique 7", departmentOne);
		Position position8 = new Position("Position unique 8", departmentOne);
		Position position9 = new Position("Position unique 9", departmentOne);
		Position position10 = new Position("Position unique 10", departmentOne);
		
		return positionsService.persistEntities(Arrays.asList(positionOne, positionTwo, position3,
			position4, position5, position6, position7, position8, position9, position10)).size();
	}
}