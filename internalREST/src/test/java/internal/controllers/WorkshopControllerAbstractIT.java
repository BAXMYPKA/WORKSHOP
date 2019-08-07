package internal.controllers;

import internal.entities.Department;
import internal.service.DepartmentsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class WorkshopControllerAbstractIT {
	
	@Autowired
	DepartmentsController departmentsController;
	@Autowired
	DepartmentsService departmentsService;
	@Autowired
	MockMvc mockMvc;
	Department department;
	
	@Test
	public void init_Test(){
		assertNotNull(departmentsController);
		assertNotNull(departmentsService);
	}
	
	@Test
	public void response_Test() throws Exception {
		//GIVEN
		long departmentId = department.getIdentifier();
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request("GET", URI.create("/internal/departments/" + departmentId));
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(request);
		
		//THEN
		resultActions.andDo(MockMvcResultHandlers.print());
	}
	
	@BeforeEach
	public void prepare(){
		department = new Department("Department one");
		department = departmentsService.persistEntity(department);
	}
}