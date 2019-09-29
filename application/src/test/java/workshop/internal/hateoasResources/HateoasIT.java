package workshop.internal.hateoasResources;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
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
import workshop.internal.controllers.DepartmentsController;
import workshop.internal.controllers.PositionsController;
import workshop.internal.entities.Department;
import workshop.internal.entities.Position;
import workshop.internal.exceptions.EntityNotFoundException;
import workshop.internal.services.DepartmentsService;
import workshop.internal.services.EmployeesService;
import workshop.internal.services.PositionsService;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class HateoasIT {
	
	@Autowired
	private DepartmentsController departmentsController;
	@Autowired
	private PositionsController positionsController;
	@Autowired
	private DepartmentsService departmentsService;
	@Autowired
	private PositionsService positionsService;
	@Autowired
	private EmployeesService employeesService;
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
	@WithMockUser(username = "employee@workshop.pro", authorities = {"Admin", "ADMIN_FULL"})
	public void single_Resource_WorkshopEntity_Should_Contain_SelfLink() throws Exception {
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
	@WithMockUser(username = "employee@workshop.pro", authorities = {"Admin", "ADMIN_FULL"})
	public void all_defaultPage_Resources_Should_Contain_SelfLinks_And_currentPageLink() throws Exception {
		//GIVEN
		Department department = new Department("Department for position");
		departmentsService.persistEntity(department);
		
		positionOne = new Position("Position unique one", department);
		positionTwo = new Position("Position unique two", department);
		
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
	@WithMockUser(username = "employee@workshop.pro", authorities = {"Admin", "ADMIN_FULL"})
	public void firstPage_Resources_Should_Contain_Only_CurrentPageLink_NextPageLink_LastPageLink() throws Exception {
		//GIVEN at least 10 elements by 3 on a page = 4 pages
		departmentIdOfTotalPersistedPositions();
		//Retrieve first page of 4 with 3 elements of 10 total elements
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(
			"GET", URI.create("/internal/positions?pageSize=3&pageNum=1"));
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(request);
		
		//THEN
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
					"\"rel\":\"lastPage\",\"href\":\"http://localhost/internal/positions?pageSize=3&")))
			.andExpect(MockMvcResultMatchers
				.content().string(Matchers.not(Matchers.containsString(
					"\"rel\":\"previousPage\"")))).andExpect(MockMvcResultMatchers
			.content().string(Matchers.not(Matchers.containsString(
				"\"rel\":\"firstPage\""))));
		
	}
	
	@Test
	@WithMockUser(username = "employee@workshop.pro", authorities = {"Admin", "ADMIN_FULL"})
	@Transactional
	public void secondPage_Resources_Should_Contain_CurrentPageLink_PreviousPageLink_NextPageLink_LastPageLink_FirstPageLink()
		throws Exception {
		//GIVEN at least 10 elements by 3 on a page = 4 pages
		departmentIdOfTotalPersistedPositions();
		//Retrieve second page of 4 with 3 elements of 10 total elements
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(
			"GET", URI.create("/internal/positions?pageSize=3&pageNum=2"));
		
		//WHEN
		int totalRetrieved = positionsService.findAllEntities(0, 0, null, Sort.Direction.DESC).size();
		
		ResultActions resultActions = mockMvc.perform(request);
		
		//THEN
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
					"\"rel\":\"lastPage\",\"href\":\"http://localhost/internal/positions?pageSize=3&")));
	}
	
	@Test
	@WithMockUser(username = "employee@workshop.pro", authorities = {"Admin", "ADMIN_FULL"})
	@Transactional
	public void lastPage_Resources_Should_Contain_Only_CurrentPageLink_PreviousPageLink_FirstPageLink()	throws Exception {
		//GIVEN 10 elements by 3 on a page = 4 pages
		long departmentId = departmentIdOfTotalPersistedPositions();
		//Retrieve last page of 4 with 3 elements of 10 total elements
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(
			"GET", URI.create(
				"/internal/departments/" + departmentId + "/positions?pageSize=3&pageNum=4&order-by=name&order=asc"));
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(request);
		
		//THEN
		resultActions
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers
				.content().string(Matchers.containsString(
					"\"rel\":\"currentPage\",\"href\":\"" +
						"http://localhost/internal/departments/" + departmentId + "/positions?pageSize=3&pageNum=4")))
			.andExpect(MockMvcResultMatchers
				.content().string(Matchers.containsString(
					"\"rel\":\"previousPage\",\"href\":\"" +
						"http://localhost/internal/departments/" + departmentId + "/positions?pageSize=3&pageNum=3")))
			.andExpect(MockMvcResultMatchers
				.content().string(Matchers.containsString(
					"\"rel\":\"firstPage\",\"href\":\"" +
						"http://localhost/internal/departments/" + departmentId + "/positions?pageSize=3&pageNum=1")))
			
			.andExpect(MockMvcResultMatchers.content().string(Matchers.not(
				Matchers.containsString("\"rel\":\"lastPage\""))))
			.andExpect(MockMvcResultMatchers.content().string(Matchers.not(
				Matchers.containsString("\"rel\":\"nextPage\""))));
	}
	
	@Test
	@WithMockUser(username = "employee@workshop.pro", authorities = {"Admin", "ADMIN_FULL"})
	@Transactional
	public void exceeding_LastPageNumber_Should_Return_HttpStatus404_NotFound()
		throws Exception {
		//GIVEN 10 elements by 3 on a page = 4 pages
		departmentIdOfTotalPersistedPositions();
		//Retrieve non-existing 5th page
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(
			"GET", URI.create("/internal/positions?pageSize=3&pageNum=55"));
		
		//WHEN
		int totalRetrieved = positionsService.findAllEntities(0, 0, null, Sort.Direction.DESC).size();
		
		ResultActions resultActions = mockMvc.perform(request);
		
		//THEN
		resultActions
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isNotFound());
	}
	
	@Test
	@WithMockUser(username = "employee@workshop.pro", authorities = {"Admin", "ADMIN_FULL"})
	@Transactional
	public void orderBy_Custom_PropertyName_With_Default_Desc_Order_Should_Be_Correct()
		throws Exception {
		//GIVEN least 10 elements by 3 on a page = 4 pages
		long departmentId = departmentIdOfTotalPersistedPositions();
		//Retrieve first page of 4 with 3 elements of 10 total elements.
		//The request with orderBy 'name' property
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(
			"GET", URI.create(
				"/internal/departments/" + departmentId + "/positions?pageSize=3&pageNum=1&order-by=name"));
		
		//WHEN
		int totalRetrieved = positionsService.findAllEntities(0, 0, null, Sort.Direction.DESC).size();
		
		ResultActions resultActions = mockMvc.perform(request);
		
		//THEN
		resultActions
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers
				.content().string(Matchers.containsString("\"name\":\"Position unique 10\"")))
			.andExpect(MockMvcResultMatchers
				.content().string(Matchers.containsString("\"name\":\"Position unique 09\"")))
			.andExpect(MockMvcResultMatchers
				.content().string(Matchers.containsString("\"name\":\"Position unique 08\"")))
			
			.andExpect(MockMvcResultMatchers
				.content().string(Matchers.not(
					Matchers.containsString("\"name\":\"Position unique 07\""))))
			.andExpect(MockMvcResultMatchers
				.content().string(Matchers.not(
					Matchers.containsString("\"name\":\"Position unique 01\""))));
	}
	
	@Test
	@WithMockUser(username = "employee@workshop.pro", authorities = {"Admin", "ADMIN_FULL"})
	@Transactional
	public void orderBy_Custom_PropertyName_With_Asc_Order_Should_Be_Correct()
		throws Exception {
		//GIVEN 10 elements by 3 on a page = 4 pages
		long departmentId = departmentIdOfTotalPersistedPositions();
		//Retrieve first page of 4 with 3 elements of 10 total elements.
		//The request with orderBy 'name' property with ascending order
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(
			"GET", URI.create(
				"/internal/departments/" + departmentId + "/positions?pageSize=3&pageNum=1&order-by=name&order=asc"));
		
		//WHEN
		int totalRetrieved = positionsService.findAllEntities(0, 0, null, Sort.Direction.DESC).size();
		
		ResultActions resultActions = mockMvc.perform(request);
		
		//THEN
		resultActions
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers
				.content().string(Matchers.containsString("\"name\":\"Position unique 01\"")))
			.andExpect(MockMvcResultMatchers
				.content().string(Matchers.containsString("\"name\":\"Position unique 02\"")))
			.andExpect(MockMvcResultMatchers
				.content().string(Matchers.containsString("\"name\":\"Position unique 03\"")))
			
			.andExpect(MockMvcResultMatchers
				.content().string(Matchers.not(
					Matchers.containsString("\"name\":\"Position unique 04\""))))
			.andExpect(MockMvcResultMatchers
				.content().string(Matchers.not(
					Matchers.containsString("\"name\":\"Position unique 10\""))));
	}
	
	long departmentIdOfTotalPersistedPositions() {
		try {
			List<Department> departmentToDelete =
				departmentsService.findByProperty("name", "Department for paginated position");
			List<Position> positionsToBeRemoved = positionsService.findPositionsByDepartment(PageRequest.of(
				0, 100, Sort.Direction.DESC, "created"),
				departmentToDelete.get(0).getIdentifier()).getContent();
			departmentsService.removeEntity(departmentToDelete.get(0));
			positionsService.removeEntities(positionsToBeRemoved);
		} catch (EntityNotFoundException e) {
			//Can only be caught for the first time. Doesnt matter.
		} finally {
			Department department = new Department("Department for paginated position");
			departmentsService.persistEntity(department);
			
			positionOne = new Position("Position unique 01", department);
			positionTwo = new Position("Position unique 02", department);
			Position position3 = new Position("Position unique 03", department);
			Position position4 = new Position("Position unique 04", department);
			Position position5 = new Position("Position unique 05", department);
			Position position6 = new Position("Position unique 06", department);
			Position position7 = new Position("Position unique 07", department);
			Position position8 = new Position("Position unique 08", department);
			Position position9 = new Position("Position unique 09", department);
			Position position10 = new Position("Position unique 10", department);
			
			positionsService.persistEntities(Arrays.asList(positionOne, positionTwo, position3,
				position4, position5, position6, position7, position8, position9, position10));
			
			return department.getIdentifier();
		}
	}
}