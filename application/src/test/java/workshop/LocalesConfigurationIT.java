package workshop;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import workshop.internal.controllers.LoginController;

import javax.servlet.http.Cookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * URI '/internal/login' has to be presented in the application
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class LocalesConfigurationIT {
	
	private final String TEST_MESSAGES_LOCATION = "classpath:testMessages";
	private final String MAIN_MESSAGES_LOCATION = "classpath:internal";
	@Autowired
	private LocaleResolver localeResolver;
	@Autowired
	private MessageSource messageSource;
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private LoginController loginController;
	
	@Test
	@Order(1)
	public void init_Test() {
		assertNotNull(messageSource);
		assertNotNull(localeResolver);
		assertNotNull(mockMvc);
	}
	
	@Disabled("The test identifier suited only for AcceptLanguageLocaleResolver")
	@Test
	public void messageSource_Should_Support_Locales() {
		//GIVEN
		List<Locale> expectedLocales = new ArrayList<>(Arrays.asList(
			Locale.ENGLISH,
			Locale.forLanguageTag("ru")));
		
		//WHEN
		List<Locale> supportedLocales = ((AcceptHeaderLocaleResolver) localeResolver).getSupportedLocales();
		
		//THEN
		assertTrue(supportedLocales.containsAll(expectedLocales));
	}
	
	@Test
	public void messageSource_Should_Resolve_Test_English_Messages() {
		//GIVEN _en message
		String test_greetings_en = "Hello!";
		
		//WHEN
		String greetingMessage = messageSource.getMessage("test.greetings", null, Locale.ENGLISH);
		
		//THEN
		assertEquals(test_greetings_en, greetingMessage);
	}
	
	@Test
	public void messageSource_Should_Resolve_Test_Russian_Messages() {
		//GIVEN _en message
		String test_greetings_ru = "Привет!";
		
		//WHEN
		String greetingMessage = messageSource.getMessage("test.greetings", null, Locale.forLanguageTag("ru"));
		
		//THEN
		assertEquals(test_greetings_ru, greetingMessage);
	}
	
	@Test
	@DisplayName("'Accept-Language':'ru' should return 'Content-Language':'ru-RU'")
	public void accept_Language_Header_Ru_Should_Return_Content_Language_Header_Ru_Via_MVC() throws Exception {
		//GIVEN
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(
			MockMvcRequestBuilders
				.request(HttpMethod.GET, URI.create("/workshop/internal/login"))
				.header("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7"));
		
		//THEN
		resultActions
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.header().string("Content-Language", "ru-RU"));
	}
	
	@Test
	@DisplayName("'Accept-Language':'en' should return 'Content-Language':'en-US' via MVC")
	public void accept_Language_Header_En_Should_Return_Content_Language_Header_En_Via_MVC() throws Exception {
		//GIVEN
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(
			MockMvcRequestBuilders
				.request(HttpMethod.GET, URI.create("/workshop/internal/login"))
				.header("Accept-Language", "en-US;q=0.8,en;q=0.7,ru-RU;q=0.6"));
		
		//THEN
		resultActions
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.header().string("Content-Language", "en-US"));
	}
	
	@Test
	@DisplayName("'Accept-Language':'' should return 'Content-Language':'ru-RU' via MVC")
	public void accept_Language_Header_Empty_Should_Return_Content_Language_Header_Default_Via_MVC() throws Exception {
		//GIVEN
//		((AcceptHeaderLocaleResolver) localeResolver).setDefaultLocale(Locale.ENGLISH);
		((CookieLocaleResolver) localeResolver).setDefaultLocale(Locale.ENGLISH);
		
		//WHEN the request without Accept-Language header
		ResultActions resultActionsWithoutAcceptLanguage = mockMvc.perform(
			MockMvcRequestBuilders.request(HttpMethod.GET, URI.create("/workshop/internal/login")));
		
		//THEN
		resultActionsWithoutAcceptLanguage
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.header().string("Content-Language", "en"));
	}
	
	@Test
	public void exceptionHandlerController_Should_Return_status415_According_to_Ru_Accept_Language() throws Exception {
		//GIVEN russian message from internal.properties for 415 HttpStatus
		String exception_unsupportedMediaType_ru = "{\"errorMessage\":\"Принят HttpRequest с неподдерживаемым типом данных!" +
			" Проверьте заголовок 'Content-Type' при отправке запросов к этому адресу!\"}";
		//Request without MediaType set
		MockHttpServletRequestBuilder requestWithErrorBody = MockMvcRequestBuilders
			.request("POST", URI.create("/workshop/internal/orders"))
			.header("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(requestWithErrorBody);
		
		//THEN
		resultActions
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isUnsupportedMediaType())
			.andExpect(MockMvcResultMatchers.content().json(exception_unsupportedMediaType_ru));
	}
	
	@Test
	public void exceptionHandlerController_Should_Return_status415_According_to_En_Accept_Language() throws Exception {
		//GIVEN english message from internal.properties for 415 HttpStatus
		String exception_unsupportedMediaType_en = "{\"errorMessage\":\"The HttpRequest with unsupported media type received!" +
			" Check 'Content-Type' header for this endpoint!\"}";
		//Request without MediaType set
		MockHttpServletRequestBuilder requestWithErrorBody = MockMvcRequestBuilders
			.request("POST", URI.create("/workshop/internal/orders"))
			.header("Accept-Language", "en-US;q=0.8,en;q=0.7");
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(requestWithErrorBody);
		
		//THEN
		resultActions
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isUnsupportedMediaType())
			.andExpect(MockMvcResultMatchers.content().json(exception_unsupportedMediaType_en));
	}
	
	@Test
	public void exceptionHandlerController_Should_Return_status415_According_to_Default_Accept_Language() throws Exception {
		//GIVEN
//		((AcceptHeaderLocaleResolver) localeResolver).setDefaultLocale(Locale.ENGLISH);
		((CookieLocaleResolver) localeResolver).setDefaultLocale(Locale.ENGLISH);
		// default message from internal.properties for 415 HttpStatus
		String exception_unsupportedMediaType = "{\"errorMessage\":\"The HttpRequest with unsupported media type received!" +
			" Check 'Content-Type' header for this endpoint!\"}";
		//Request without MediaType set
		MockHttpServletRequestBuilder requestWithErrorBody = MockMvcRequestBuilders
			.request("POST", URI.create("/workshop/internal/orders"))
			.header("Accept-Language", "fr-FR;q=0.8,al;q=0.7");
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(requestWithErrorBody);
		
		//THEN
		resultActions
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isUnsupportedMediaType())
			.andExpect(MockMvcResultMatchers.content().json(exception_unsupportedMediaType));
	}
	
	@Test
	public void request_With_RU_Locale_Change_Parameter_Should_Response_With_RU_Cookie_Lang() throws Exception {
		//GIVEN a mockMvc without lang cookie set
		MockHttpServletRequestBuilder localeChangeRequest = MockMvcRequestBuilders
			.request(HttpMethod.GET, "/workshop/internal/orders/all")
			.param("lang", "ru")
			.header("Accept-Language", "fr-FR;q=0.8,al;q=0.7");
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(localeChangeRequest);
		
		//THEN
		resultActions
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.cookie()
				.value("lang", "ru"));
	}
	
	@Test
	public void request_With_EN_Locale_Change_Parameter_Should_Response_With_EN_Cookie_Lang() throws Exception {
		//GIVEN a mockMvc without lang cookie set
		MockHttpServletRequestBuilder localeChangeRequest = MockMvcRequestBuilders
			.request(HttpMethod.GET, "/workshop/internal/orders/all")
			.param("lang", "en")
			.header("Accept-Language", "fr-FR;q=0.8,al;q=0.7");
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(localeChangeRequest);
		
		//THEN
		resultActions
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.cookie()
				.value("lang", "en"));
	}
	
	@Test
	public void request_With_RU_Cookie_Should_Response_With_RU_Language() throws Exception {
		//GIVEN a mockMvc without lang cookie set
		Cookie rusCookie = new Cookie("lang", "ru");
		rusCookie.setHttpOnly(true);
		
		MockHttpServletRequestBuilder localeChangeRequest = MockMvcRequestBuilders
			.request(HttpMethod.GET, "/workshop/internal/orders/100500")
			.header("Accept-Language", "fr-FR;q=0.8,al;q=0.7")
			.cookie(rusCookie);
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(localeChangeRequest);
		
		//THEN
		resultActions
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isNotFound())
			.andExpect(MockMvcResultMatchers.content().json("{\"errorMessage\":\"Ничего из Order с identifier=100500 не было найдено!\"}"));
	}
	
	@Test
	public void request_With_EN_Cookie_Should_Response_With_EN_Language() throws Exception {
		//GIVEN a mockMvc without lang cookie set
		Cookie rusCookie = new Cookie("lang", "en");
		rusCookie.setHttpOnly(true);
		
		MockHttpServletRequestBuilder localeChangeRequest = MockMvcRequestBuilders
			.request(HttpMethod.GET, "/workshop/internal/orders/100500")
			.header("Accept-Language", "fr-FR;q=0.8,al;q=0.7")
			.cookie(rusCookie);
		
		//WHEN
		ResultActions resultActions = mockMvc.perform(localeChangeRequest);
		
		//THEN
		resultActions
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isNotFound())
			.andExpect(MockMvcResultMatchers.content().json(
				"{\"errorMessage\":\"No Order with identifier=100500 was found!\"}"));
	}
	
	@BeforeEach
	public void settingUp() {
		((ReloadableResourceBundleMessageSource) messageSource).addBasenames(TEST_MESSAGES_LOCATION);
		((CookieLocaleResolver) localeResolver).setDefaultLocale(null);
	}
}