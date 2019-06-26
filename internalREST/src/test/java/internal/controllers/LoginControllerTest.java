package internal.controllers;

import com.sun.corba.se.impl.orbutil.LogKeywords;
import internal.httpSecurity.LoginAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureMockMvc
@WebMvcTest
@ContextConfiguration(classes = {LoginController.class})
class LoginControllerTest {
	
	@Autowired
	MockMvc mockMvc;
	
	@MockBean
	LoginAuthenticationFilter filter;
	
	
	public void init() {
//		mockMvc = MockMvcBuilders.standaloneSetup(LoginController.class).build();
	}
	
	@Test
	public void ttt() throws Exception {
		
		//GIVEN
		
		//WHEN
		mockMvc = MockMvcBuilders.standaloneSetup(LoginController.class).build();
		
		ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.request("GET", URI.create("internal" +
			"/login")));
		
		//THEN
		
		resultActions
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.view().name("Login"));
		
	}
}