package internal.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import internal.entities.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.parameters.P;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JsonServiceTest {
	
	JsonService jsonService;
	Department department;
	Position positionOne;
	Position positionTwo;
	
	@BeforeEach
	public void init() {
		jsonService = new JsonService();
		
		department = new Department();
		department.setId(1);
		department.setName("The Department");
		
		positionOne = new Position();
		positionOne.setId(222);
		positionOne.setName("The Position One");
		positionOne.setDescription("The description");
		positionOne.setCreated(LocalDateTime.of(2019, 6, 15, 12, 35, 45, 0));
		positionOne.setModified(LocalDateTime.now().minusMinutes(5));
		positionOne.setDepartment(department);
		
		positionTwo = new Position();
		positionTwo.setId(333);
		positionTwo.setName("The Position Two");
		positionTwo.setDescription("The second description");
		positionTwo.setCreated(LocalDateTime.of(2019, 7, 20, 14, 40, 55, 0));
		positionTwo.setModified(LocalDateTime.now().minusMinutes(5));
		positionTwo.setDepartment(department);
	}
	
	@DisplayName("Entities with only simple fields")
	@Test
	public void simple_Entities_Return_Valid_Json() throws JsonProcessingException {
		//GIVEN
		// 1) Department without embedded Position
		// 2) PositionOne
		
		//WHEN
		String jsonedDepartment = jsonService.convertToJson(department);
		String jsonedPosition = jsonService.convertToJson(positionOne);
		
		//THEN
		// 1) check properties as valid JSON
		// 2) one included Department object as JSON
		// 3) LocalDateTime readable format
		assertAll(
			() -> assertTrue(jsonedDepartment.contains("\"id\":1")),
			() -> assertTrue(jsonedDepartment.contains("\"name\":\"The Department\"")),
			() -> assertTrue(jsonedDepartment.contains("\"positions\":null"))
		);
		
		String departmentInPosition = "\"department\":" +
			"{\"id\":1," +
			"\"name\":\"The Department\"," +
			"\"positions\":null}";
		
		assertAll(
			() -> assertTrue(jsonedPosition.contains("\"id\":2")),
			() -> assertTrue(jsonedPosition.contains("\"name\":\"The Position One\"")),
			() -> assertTrue(jsonedPosition.contains("\"description\":\"The description\"")),
			() -> assertTrue(jsonedPosition.contains("\"created\":\"2019-06-15T12:35:45\"")),
			() -> assertTrue(jsonedPosition.contains("\"finished\":null")),
			() -> assertTrue(jsonedPosition.contains(departmentInPosition)),
			() -> assertFalse(jsonedPosition.contains("\"authority\":\"The Position One\""))
		);
	}
	
	/**
	 * @param originalEntity Dont forget to add every new Entity at the "entitiesStream" method to test them here
	 */
	@ParameterizedTest
	@MethodSource("entitiesStream")
	@DisplayName("Every simple Entity (without deep graph) can be converted to JSON and back to the Entity")
	public void every_Entity_Can_Be_Properly_Serialized_And_Deserialized_Back(WorkshopEntity originalEntity)
		throws IOException {
		//GIVEN incoming Stream.of Entities from "entitiesStream" method
		
		//WHEN convert Entity to JSON and back
		String jsonedEntity = jsonService.convertToJson(originalEntity);
		WorkshopEntity deserializedEntity = jsonService.convertFromJson(jsonedEntity, originalEntity.getClass());
		
		//THEN
		// 1) no exceptions were thrown previously
		// 2) Compare original classes properties to same properties after double conversion
		assertEquals(originalEntity.getId(), deserializedEntity.getId());
		
		if ("Classifier".equals(originalEntity.getClass().getSimpleName())) {
			assertAll(
				() -> assertEquals(
					((Classifier) originalEntity).getName(), ((Classifier) deserializedEntity).getName()
				),
				() -> assertEquals(
					((Classifier) originalEntity).getCreated(), ((Classifier) deserializedEntity).getCreated()
				)
			);
		} else if ("Department".equals(originalEntity.getClass().getSimpleName())) {
			assertEquals(
				((Department) originalEntity).getName(), ((Department) deserializedEntity).getName());
		} else if ("Position".equals(originalEntity.getClass().getSimpleName())) {
			assertAll(
				() -> assertEquals(
					((Position) originalEntity).getDescription(), ((Position) deserializedEntity).getDescription()
				),
				() -> assertEquals(
					((Position) originalEntity).getCreated(), ((Position) deserializedEntity).getCreated()
				)
			);
		} else if ("Task".equals(originalEntity.getClass().getSimpleName())) {
			assertAll(
				() -> assertEquals(
					((Task) originalEntity).getName(), ((Task) deserializedEntity).getName()
				),
				() -> assertEquals(
					((Task) originalEntity).getDeadline(), ((Task) deserializedEntity).getDeadline()
				)
			);
		} else if ("Phone".equals(originalEntity.getClass().getSimpleName())) {
			assertAll(
				() -> assertEquals(
					((Phone) originalEntity).getName(), ((Phone) deserializedEntity).getName()
				),
				() -> assertEquals(
					((Phone) originalEntity).getPhone(), ((Phone) deserializedEntity).getPhone()
				)
			);
		} else if ("User".equals(originalEntity.getClass().getSimpleName())) {
			assertAll(
				() -> assertEquals(
					((User) originalEntity).getEmail(), ((User) deserializedEntity).getEmail()
				),
				() -> assertEquals(
					((User) originalEntity).getBirthday(), ((User) deserializedEntity).getBirthday()
				)
			);
		} else if ("Employee".equals(originalEntity.getClass().getSimpleName())) {
			assertAll(
				() -> assertEquals(
					((Employee) originalEntity).getEmail(), ((Employee) deserializedEntity).getEmail()
				),
				() -> assertEquals(
					((Employee) originalEntity).getBirthday(), ((Employee) deserializedEntity).getBirthday()
				)
			);
		}
	}
	
	@Test
	@DisplayName("Raw Passwords have to be read from User input but not given back from DB as encoded originals")
	public void passwords_Dont_Have_To_Be_Serialized_From_DataBase_To_Outside() throws JsonProcessingException {
		
		//GIVEN entitites from DataBase
		User user = new User();
		user.setId(100);
		user.setFirstName("First User Name");
		user.setPassword("54321");
		
		Employee employee = new Employee();
		employee.setId(23);
		employee.setFirstName("FName");
		employee.setEmail("email@workshop.pro");
		employee.setPassword("12345");
		
		//WHEN
		String jsonedUser = jsonService.convertToJson(user);
		String jsonedEmployee = jsonService.convertToJson(employee);
		
		//THEN
		assertFalse(jsonedUser.contains("\"password\":"));
		assertFalse(jsonedEmployee.contains("\"password\":"));
	}
	
	@Test
	@DisplayName("Raw Passwords have to be read from User input but not given back from DB as encoded originals")
	public void passwords_Have_To_Be_Deserialized_From_JSON_To_User_or_Employee_From_Outside() throws IOException {
		
		//GIVEN User and Employee from outside Input with raw passwords
		String jsonedUserWithPassword = "{\"id\":100,\"password\":\"12345\",\"firstName\":\"First User Name\"," +
			"\"lastName\":null,\"email\":null,\"created\":null,\"modified\":null,\"birthday\":null,\"phones\":null,\"orders\":null}";
		String jsonedEmployeeWithPassword = "{\"id\":23,\"password\":\"54321\",\"created\":null,\"modified\":null," +
			"\"finished\":null,\"createdBy\":null,\"modifiedBy\":null,\"firstName\":\"FName\",\"lastName\":null,\"email\":\"email@workshop.pro\",\"birthday\":null,\"phones\":null,\"position\":null,\"appointedTasks\":null,\"ordersModifiedBy\":null,\"ordersCreatedBy\":null,\"tasksModifiedBy\":null,\"tasksCreatedBy\":null}";
		
		//WHEN
		User deserializedUser = jsonService.convertFromJson(jsonedUserWithPassword, User.class);
		Employee deserializedEmployee = jsonService.convertFromJson(jsonedEmployeeWithPassword, Employee.class);
		
		//THEN
		assertEquals("12345", deserializedUser.getPassword());
		assertEquals("54321", deserializedEmployee.getPassword());
	}
	
	@Test
	@DisplayName("LocalDateTime has to be converted to JSON and back without issues")
	public void LocalDateTime_converts_vise_versa() throws IOException {
		//GIVEN
		positionOne = new Position();
		positionOne.setName("PositionOne");
		positionOne.setCreated(LocalDateTime.now().minusHours(1).withMinute(10).withSecond(0).withNano(0));
		positionOne.setModified(LocalDateTime.now().minusDays(3).withMinute(10).withHour(3).withSecond(0).withNano(0));
		
		//WHEN convert Entity to JSON and back
		String jsonedPosition = jsonService.convertToJson(positionOne);
		Position position = jsonService.convertFromJson(jsonedPosition, Position.class);
		
		//THEN
		assertAll(
			() -> assertEquals(position.getCreated(),
				LocalDateTime.now().minusHours(1).withMinute(10).withSecond(0).withNano(0)),
			() -> assertEquals(position.getModified(),
				LocalDateTime.now().minusDays(3).withMinute(10).withHour(3).withSecond(0).withNano(0))
		);
	}
	
	@Test
	@DisplayName("Related Entitites dont have to throw 'Intinite loop' Exception with StackOverFlow")
	public void no_Infinite_Recursion_With_Valid_Included_Objects() throws JsonProcessingException {
		//GIVEN
		Classifier classifier = new Classifier();
		classifier.setId(144);
		
		Employee employee = new Employee();
		employee.setId(456);
		
		Task task = new Task();
		task.setId(578);
		
		User user = new User();
		user.setId(231);
		
		Order order = new Order();
		order.setId(591);
		
		Phone phone = new Phone();
		phone.setId(523);
		
		department.setPositions(new HashSet<>(Arrays.asList(positionOne, positionTwo)));
		
		positionOne.setDepartment(department);
		positionOne.setEmployees(new HashSet<Employee>(Collections.singleton(employee)));
		
		positionTwo.setDepartment(department);
		positionTwo.setEmployees(new HashSet<Employee>(Collections.singleton(employee)));
		
		classifier.setTasks(new HashSet<Task>(Collections.singletonList(task)));
		classifier.setCreatedBy(employee);
		
		task.setAppointedTo(employee);
		task.setClassifiers(new HashSet<>(Collections.singleton(classifier)));
		task.setOrder(order);
		task.setModifiedBy(employee);
		
		phone.setUser(user);
		
		user.setOrders(new HashSet<>(Collections.singleton(order)));
		user.setPhones(new HashSet<>(Collections.singleton(phone)));
		
		order.setCreatedFor(user);
		order.setTasks(new HashSet<>(Collections.singleton(task)));
		order.setCreatedBy(employee);
		order.setModifiedBy(employee);
		
		//WHEN
		
		//THEN
		assertDoesNotThrow(() -> jsonService.convertToJson(department));
		assertDoesNotThrow(() -> jsonService.convertToJson(positionOne));
		assertDoesNotThrow(() -> jsonService.convertToJson(positionTwo));
		assertDoesNotThrow(() -> jsonService.convertToJson(classifier));
		assertDoesNotThrow(() -> jsonService.convertToJson(task));
		assertDoesNotThrow(() -> jsonService.convertToJson(phone));
		assertDoesNotThrow(() -> jsonService.convertToJson(user));
		assertDoesNotThrow(() -> jsonService.convertToJson(order));
	}
	
	private static Stream<Arguments> entitiesStream() {
		Classifier classifier = new Classifier();
		classifier.setId(65);
		classifier.setName("The Classifier");
		classifier.setPrice(new BigDecimal("45.25"));
		classifier.setCreated(LocalDateTime.now().minusMonths(5));
		
		Department department = new Department();
		department.setId(38);
		department.setName("The Department");
		
		Position position = new Position();
		position.setId(2);
		position.setName("The Position");
		position.setDescription("The description");
		position.setCreated(LocalDateTime.of(2019, 6, 15, 12, 35, 45, 0));
		position.setModified(LocalDateTime.now().minusMinutes(5));
		
		Order order = new Order();
		order.setId(10);
		order.setDescription("Order desc");
		order.setOverallPrice(new BigDecimal("333.22"));
		order.setCreated(LocalDateTime.of(2019, 10, 13, 13, 45, 15));
		order.setDeadline(LocalDateTime.now().plusDays(10));
		order.setModified(LocalDateTime.now());
		
		Employee employee = new Employee();
		employee.setId(23);
		employee.setFirstName("FName");
		employee.setLastName("LName");
		employee.setBirthday(LocalDate.now().minusYears(45));
		employee.setEmail("email@workshop.pro");
		employee.setPassword("12345");
		
		Task task = new Task();
		task.setId(45);
		task.setName("Task name");
		task.setPrice(new BigDecimal("123.98"));
		task.setDeadline(LocalDateTime.now().plusDays(15));
		
		Phone phone = new Phone();
		phone.setId(39);
		phone.setName("Phone name");
		phone.setPhone("98765");
		
		User user = new User();
		user.setId(100);
		user.setEmail("user@workshop.pro");
		user.setBirthday(LocalDate.now().minusYears(50));
		user.setCreated(LocalDateTime.now().minusMonths(3));
		user.setModified(LocalDateTime.now());
		user.setFirstName("First User Name");
		user.setPassword("54321");
		
		return Stream.of(Arguments.of(department), Arguments.of(position), Arguments.of(order),
			Arguments.of(employee), Arguments.of(task), Arguments.of(phone), Arguments.of(user));
	}
}