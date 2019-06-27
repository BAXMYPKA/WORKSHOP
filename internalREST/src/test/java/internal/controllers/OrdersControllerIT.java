package internal.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import internal.entities.Order;
import internal.service.JsonService;
import internal.service.OrdersService;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
//@WebMvcTest(controllers = {OrdersController.class})
//@WebAppConfiguration
class OrdersControllerIT {
	
	@Autowired
	MockMvc mockMvc;
//	MockMvc mockMvc = MockMvcBuilders.standaloneSetup(OrdersController.class).build();
	@MockBean
	OrdersService ordersService;
	@MockBean
	JsonService jsonService;
	List<Order> orders = new ArrayList<>(10);
	
	
	@Test
	public void spring_Boot_Test_Sees_The_Application_Context() {
		assertAll(
			() -> assertNotNull(mockMvc),
			() -> assertNotNull(ordersService),
			() -> assertNotNull(jsonService)
		);
	}
	
	@Test
	public void on_Incorrect_Request_Returns_400_Status() throws Exception {
		
		//GIVEN
		
		//Request without obligatory 'size' parameter
		
		//WHEN THEN
		
		mockMvc.perform(
			(MockMvcRequestBuilders.get("/internal/orders/all"))
//				.param("size", "3")
				.param("page", "2"))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().is(400));
	}
	
	@Test
	public void on_Simple_Request_Returns_Orders_List_With_Ok_Status_And_Json_ContentType() throws Exception {
		
		//GIVEN
		
		Mockito.when(ordersService.findAllOrders(3, 2, "", "desc"))
			.thenReturn(orders);
		
		String jsonOrders = "[{\"id\":1},{\"id\":2}]";
		
		Mockito.when(jsonService.convertEntitiesToJson(orders)).thenReturn(jsonOrders);
		
		//WHEN THEN
		
		mockMvc.perform(
			(MockMvcRequestBuilders.get("/internal/orders/all"))
				.param("size", "3")
				.param("page", "2"))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().is(200))
			.andExpect(MockMvcResultMatchers.content().string(jsonOrders))
			.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
	}
	
	@Test
	public void on_Correctly_Parametrized_Request_Returns_Orders_List_With_Ok_Status_And_Json_Content() throws Exception {
		
		//GIVEN
		
		Mockito.when(ordersService.findAllOrders(3, 2, "created", "asc"))
			.thenReturn(orders);
		
		String jsonOrders = "[{\"id\":1},{\"id\":2}]";
		
		Mockito.when(jsonService.convertEntitiesToJson(orders)).thenReturn(jsonOrders);
		
		//WHEN THEN
		
		mockMvc.perform(
			(MockMvcRequestBuilders.get("/internal/orders/all"))
				.param("size", "3")
				.param("page", "2")
				.param("sort", "created")
				.param("asc-desc", "asc"))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().is(200))
			.andExpect(MockMvcResultMatchers.content().string(jsonOrders))
			.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
	}
	
	@BeforeEach
	public void init() {
		Order order1 = new Order();
		order1.setId(1);
		order1.setCreated(LocalDateTime.of(2019, 10, 15, 9, 35, 45));
		Order order2 = new Order();
		order2.setId(2);
		order2.setCreated(LocalDateTime.of(2018, 11, 20, 9, 35, 45));
		Order order3 = new Order();
		order3.setId(3);
		order3.setCreated(LocalDateTime.of(2017, 11, 20, 9, 35, 45));
		Order order4 = new Order();
		order4.setId(4);
		order4.setCreated(LocalDateTime.of(2016, 11, 20, 9, 35, 45));
		Order order5 = new Order();
		order5.setId(5);
		order5.setCreated(LocalDateTime.of(2015, 11, 20, 9, 35, 45));
		Order order6 = new Order();
		order6.setId(6);
		order6.setCreated(LocalDateTime.of(2014, 11, 20, 9, 35, 45));
		Order order7 = new Order();
		order7.setId(7);
		order7.setCreated(LocalDateTime.of(2013, 11, 20, 9, 35, 45));
		Order order8 = new Order();
		order8.setId(8);
		order8.setCreated(LocalDateTime.of(2012, 11, 20, 9, 35, 45));
		Order order9 = new Order();
		order9.setId(9);
		order9.setCreated(LocalDateTime.of(2011, 11, 20, 9, 35, 45));
		Order order10 = new Order();
		order10.setId(10);
		order10.setCreated(LocalDateTime.of(2010, 11, 20, 9, 35, 45));
		
		orders.addAll(Arrays.asList(order1, order2, order3, order4, order5, order6, order7, order8, order9, order10));
	}
}