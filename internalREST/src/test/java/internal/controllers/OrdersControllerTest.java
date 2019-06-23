package internal.controllers;

import internal.service.JsonService;
import internal.service.OrdersService;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
class OrdersControllerTest {
	
	@Autowired
	MockMvc mockMvc;
	@MockBean
	OrdersService ordersService;
	@MockBean
	JsonService jsonService;
	
	@BeforeEach
	public void init() {
	}
	
	@Test
	public void spring_Boot_Test_Sees_The_Application_Context() {
		assertAll(
			() -> assertNotNull(mockMvc),
			() -> assertNotNull(ordersService),
			() -> assertNotNull(jsonService)
		);
	}
	
	@Test
	public void another() throws Exception {
		mockMvc.perform(
			(MockMvcRequestBuilders.get("/internal/orders/all"))
				.param("size", "3")
				.param("page", "1"))
			.andExpect(MockMvcResultMatchers.status().is(200))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.content().string(""));
	}
}