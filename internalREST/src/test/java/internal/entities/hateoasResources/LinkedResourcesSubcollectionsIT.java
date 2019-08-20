package internal.entities.hateoasResources;

import internal.controllers.DepartmentsController;
import internal.entities.Department;
import internal.entities.Position;
import internal.services.DepartmentsService;
import internal.services.PositionsService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class LinkedResourcesSubcollectionsIT {
	
	@Autowired
	MockMvc mockMvc;
	@Autowired
	DepartmentsService departmentsService;
	@Autowired
	PositionsService positionsService;
	@Autowired
	DepartmentsController departmentsController;
	
	@Test
	@Order(1)
	public void init() {
		assertNotNull(mockMvc);
	}
	
	@Test
	public void department_Resource_Should_Contain_Self_Link_And_Its_Positions_Link() throws Exception {
		//GIVEN
		Department department1 = new Department("Department 1");
		Position position1 = new Position("Position 1", department1);
		department1.addPosition(position1);
		
		Department department1Persisted = departmentsService.persistEntity(department1);
		long departmentId = department1Persisted.getIdentifier();
		
		departmentsController.setDEFAULT_PAGE_SIZE(2);
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request("GET",
			URI.create("/internal/departments/" + departmentId));
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(request);
		
		//THEN
		resultActions.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.content().string(
				Matchers.containsString("\"rel\":\"self\"")))
			.andExpect(MockMvcResultMatchers.content().string(
				Matchers.containsString("\"href\":\"http://localhost/internal/departments/" + departmentId)))
			.andExpect(MockMvcResultMatchers.content().string(
				Matchers.containsString("\"rel\":\"Positions\"")))
			.andExpect(MockMvcResultMatchers.content().string(
				Matchers.containsString("\"href\":\"http://localhost/internal/departments/" + departmentId + "/positions")));
	}
	
	@RepeatedTest(3)
	@Transactional
	public void departments_Positions_FirstPage_Should_Be_Returned_As_Sorted_By_Name_Order_Desc_Paged_Collection()
		throws Exception {
		//GIVEN 9 Positions, 2 Items pageSize for 5 pages
		Department department1 = new Department("Department 1");
		Position position1 = new Position("Position 1", department1);
		Position position2 = new Position("Position 2", department1);
		Position position3 = new Position("Position 3", department1);
		Position position4 = new Position("Position 4", department1);
		Position position5 = new Position("Position 5", department1);
		Position position6 = new Position("Position 6", department1);
		Position position7 = new Position("Position 7", department1);
		Position position8 = new Position("Position 8", department1);
		Position position9 = new Position("Position 9", department1);
		department1.addPosition(position1, position2, position3, position4, position5, position6, position7,
			position8, position9);

		Department department1Persisted = departmentsService.persistEntity(department1);
		
		long departmentId = department1Persisted.getIdentifier();
		
		MockHttpServletRequestBuilder sortByNameDescRequest = MockMvcRequestBuilders.request("GET",
			URI.create("/internal/departments/" + departmentId + "/positions?order-by=name&order=desc&pageSize=2"));
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(sortByNameDescRequest);
		
		//THEN
		resultActions.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.content().string(
				Matchers.containsString("\"name\":\"Position 9\"")))
			.andExpect(MockMvcResultMatchers.content().string(
				Matchers.containsString("\"name\":\"Position 8\"")))
			
			.andExpect(MockMvcResultMatchers.content().string(Matchers.not(
				Matchers.containsString("\"name\":\"Position 7\""))))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.not(
				Matchers.containsString("\"name\":\"Position 1\""))));
	}
	
	@RepeatedTest(3)
	@Transactional
	public void departments_Positions_SecondPage_Should_Be_Returned_With_Prev_Next_First_Last_Pages_Links()
		throws Exception {
		//GIVEN 9 Positions, 2 Items pageSize for 5 pages
		Department department1 = new Department("Department 1");
		Position position1 = new Position("Position 1", department1);
		Position position2 = new Position("Position 2", department1);
		Position position3 = new Position("Position 3", department1);
		Position position4 = new Position("Position 4", department1);
		Position position5 = new Position("Position 5", department1);
		Position position6 = new Position("Position 6", department1);
		Position position7 = new Position("Position 7", department1);
		Position position8 = new Position("Position 8", department1);
		Position position9 = new Position("Position 9", department1);
		department1.addPosition(position1, position2, position3, position4, position5, position6, position7,
			position8, position9);
		
		Department department1Persisted = departmentsService.persistEntity(department1);
		
		long departmentId = department1Persisted.getIdentifier();
		
		MockHttpServletRequestBuilder sortByNameDescRequest = MockMvcRequestBuilders.request("GET",
			URI.create("/internal/departments/" + departmentId + "/positions?order-by=name&order=desc&pageSize=2&pageNum=2"));
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(sortByNameDescRequest);
		
		//THEN
		resultActions.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.content().string(
				Matchers.containsString("\"name\":\"Position 7\"")))
			.andExpect(MockMvcResultMatchers.content().string(
				Matchers.containsString("\"name\":\"Position 6\"")))
			
			.andExpect(MockMvcResultMatchers.content().string(
				Matchers.containsString("\"rel\":\"previousPage\"")))
			.andExpect(MockMvcResultMatchers.content().string(
				Matchers.containsString("\"href\":\"http://localhost/internal/departments/" + departmentId+"/positions")))
			.andExpect(MockMvcResultMatchers.content().string(
				Matchers.containsString("\"rel\":\"nextPage\"")))
			.andExpect(MockMvcResultMatchers.content().string(
				Matchers.containsString("\"href\":\"http://localhost/internal/departments/" + departmentId+"/positions")))
			.andExpect(MockMvcResultMatchers.content().string(
				Matchers.containsString("\"rel\":\"firstPage\"")))
			.andExpect(MockMvcResultMatchers.content().string(
				Matchers.containsString("\"href\":\"http://localhost/internal/departments/" + departmentId+"/positions")))
			.andExpect(MockMvcResultMatchers.content().string(
				Matchers.containsString("\"rel\":\"lastPage\"")))
			.andExpect(MockMvcResultMatchers.content().string(
				Matchers.containsString("\"href\":\"http://localhost/internal/departments/" + departmentId+"/positions")))
			
			.andExpect(MockMvcResultMatchers.content().string(Matchers.not(
				Matchers.containsString("\"name\":\"Position 9\""))))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.not(
				Matchers.containsString("\"name\":\"Position 1\""))));
	}
}