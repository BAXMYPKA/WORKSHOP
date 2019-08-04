package internal.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import internal.controllers.DepartmentsController;
import internal.dao.DepartmentsDao;
import internal.entities.*;
import internal.entities.hateoasResources.DepartmentResource;
import internal.service.serviceUtils.JsonServiceUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JsonServiceUtilsTest {
	
	JsonServiceUtils jsonServiceUtils;
	Department department;
	DepartmentsController departmentsController;
	Position positionOne;
	Position positionTwo;
	
	@BeforeEach
	public void init() {
		jsonServiceUtils = new JsonServiceUtils();
		
		department = new Department();
		department.setId(1);
		department.setName("The Department");
		
		positionOne = new Position();
		positionOne.setId(222);
		positionOne.setName("The Position One");
		positionOne.setDescription("The description");
		positionOne.setCreated(ZonedDateTime.of(2019, 6, 15, 12, 35, 45, 0, ZoneId.of("UTC")));
		positionOne.setModified(ZonedDateTime.now().minusMinutes(5).withZoneSameInstant(ZoneId.of("UTC")));
		positionOne.setDepartment(department);
		
		positionTwo = new Position();
		positionTwo.setId(333);
		positionTwo.setName("The Position Two");
		positionTwo.setDescription("The second description");
		positionTwo.setCreated(ZonedDateTime.of(2019, 7, 20, 14, 40, 55, 0, ZoneId.of("UTC")));
		positionTwo.setModified(ZonedDateTime.now().minusMinutes(5).withZoneSameInstant(ZoneId.of("UTC")));
		positionTwo.setDepartment(department);
	}
	
	@DisplayName("Entities with only simple fields")
	@Test
	public void simple_Entities_Return_Valid_Json() throws JsonProcessingException {
		//GIVEN
		// 1) Department without embedded Position
		// 2) PositionOne
		
		//WHEN
		String jsonedDepartment = jsonServiceUtils.convertEntityToJson(department);
		String jsonedPosition = jsonServiceUtils.convertEntityToJson(positionOne);
		
		//THEN
		// 1) check properties as valid JSON
		// 2) one included Department object as JSON
		// 3) LocalDateTime readable format
		assertAll(
			() -> assertTrue(jsonedDepartment.contains("\"id\":1")),
			() -> assertTrue(jsonedDepartment.contains("\"name\":\"The Department\"")),
			() -> assertFalse(jsonedDepartment.contains("\"positions\":null"))
		);
		
		String departmentInPosition = "\"department\":" +
			"{\"id\":1," +
			"\"name\":\"The Department\"," +
			"\"positions\":null}";
		
		assertAll(
			() -> assertTrue(jsonedPosition.contains("\"id\":2")),
			() -> assertTrue(jsonedPosition.contains("\"name\":\"The Position One\"")),
			() -> assertTrue(jsonedPosition.contains("\"description\":\"The description\"")),
			() -> assertTrue(jsonedPosition.contains("\"created\":\"2019-06-15T12:35:45Z\"")),
			() -> assertTrue(jsonedPosition.contains("\"finished\":null")),
			() -> assertFalse(jsonedPosition.contains(departmentInPosition)),
			() -> assertFalse(jsonedPosition.contains("\"authority\":\"The Position One\""))
		);
	}
	
	@Test
	@DisplayName("Serialized Entity must contain a custom TimeZone. Deserialized one must have only UTC-corrected.")
	public void zonedDateTime_Properly_Converts_Vise_Versa() throws IOException {
		//GIVEN a Position with a custom ZonedDateTime (+3 hours)
		Position position = new Position("ZonedDateTimePosition", new Department());
		position.setCreated(ZonedDateTime.of(2019, 1, 30, 12, 30, 0, 0, ZoneId.of("Europe/Moscow")));
		
		//WHEN converts vise versa
		String serializedPosition = jsonServiceUtils.convertEntityToJson(position);
		Position deserializedPosition = jsonServiceUtils.convertEntityFromJson(serializedPosition, Position.class);
		
		//THEN
		//Serialized Position contains original ZonedDateTime
		assertTrue(serializedPosition.contains("\"created\":\"2019-01-30T12:30:00+03:00\""));
		//Deserialized one contains UTC-corrected ZonedDateTime (-3 hours)
		assertTrue(deserializedPosition.getCreated().isEqual(ZonedDateTime.of(
			2019, 1, 30, 9, 30, 0, 0, ZoneId.of("UTC"))));
	}
	
	/**
	 * @param originalEntity Dont forget to add every new Entity at the "entitiesStream" method to test them here
	 */
	@ParameterizedTest
	@MethodSource("entitiesStream")
	@DisplayName("Every simple Entity (without deep graph) can be converted to JSON and back to the Entity" +
		"Entities as Objects and as Jsoned Objects are save original TimeZone," +
		"but after deserialization UTC will be returned.")
	public void every_Entity_Can_Be_Properly_Serialized_And_Deserialized_Back(WorkshopEntity originalEntity)
		throws IOException {
		//GIVEN incoming Stream.of Entities from "entitiesStream" method
		
		//WHEN convert Entity to JSON and back
		String jsonedEntity = jsonServiceUtils.convertEntityToJson(originalEntity);
		WorkshopEntity deserializedEntity = jsonServiceUtils.convertEntityFromJson(jsonedEntity, originalEntity.getClass());
		
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
					((Classifier) originalEntity).getCreated().withZoneSameInstant(ZoneId.of("UTC")),
					((Classifier) deserializedEntity).getCreated()
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
					((Position) originalEntity).getCreated().withZoneSameInstant(ZoneId.of("UTC")),
					((Position) deserializedEntity).getCreated()
				)
			);
		} else if ("Task".equals(originalEntity.getClass().getSimpleName())) {
			assertAll(
				() -> assertEquals(
					((Task) originalEntity).getName(), ((Task) deserializedEntity).getName()
				),
				() -> assertEquals(
					((Task) originalEntity).getDeadline().withZoneSameInstant(ZoneId.of("UTC")),
					((Task) deserializedEntity).getDeadline()
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
		String jsonedUser = jsonServiceUtils.convertEntityToJson(user);
		String jsonedEmployee = jsonServiceUtils.convertEntityToJson(employee);
		
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
		User deserializedUser = jsonServiceUtils.convertEntityFromJson(jsonedUserWithPassword, User.class);
		Employee deserializedEmployee = jsonServiceUtils.convertEntityFromJson(jsonedEmployeeWithPassword, Employee.class);
		
		//THEN
		assertEquals("12345", deserializedUser.getPassword());
		assertEquals("54321", deserializedEmployee.getPassword());
	}
	
	@Test
	@DisplayName("ZonedDateTime has to be converted to JSON and back without issues." +
		"JsonService saves original TimeZone but returns the UTC value!")
	public void zonedDateTime_converts_vise_versa() throws IOException {
		//GIVEN
		positionOne = new Position();
		positionOne.setName("PositionOne");
		//ZonedDateTime in the local TimeZone of current JVM
		positionOne.setCreated(ZonedDateTime.now().minusHours(1).withMinute(10).withSecond(0).withNano(0).withZoneSameInstant(ZoneId.systemDefault()));
		positionOne.setModified(ZonedDateTime.now().minusDays(3).withMinute(10).withHour(3).withSecond(0).withNano(0).withZoneSameInstant(ZoneId.systemDefault()));
		
		//WHEN convert Entity to JSON and back
		String jsonedPosition = jsonServiceUtils.convertEntityToJson(positionOne);
		Position positionFromJson = jsonServiceUtils.convertEntityFromJson(jsonedPosition, Position.class);
		
		//THEN
		assertAll(
			() -> assertEquals(positionFromJson.getCreated(),
				ZonedDateTime.now().minusHours(1).withMinute(10).withSecond(0).withNano(0).withZoneSameInstant(ZoneId.of("UTC"))),
			() -> assertEquals(positionFromJson.getModified(),
				ZonedDateTime.now().minusDays(3).withMinute(10).withHour(3).withSecond(0).withNano(0).withZoneSameInstant(ZoneId.of("UTC")))
		);
	}
	
	@Test
	@DisplayName("Related Entitites dont have to throw 'Intinite loop' Exception with StackOverFlow")
	public void no_Infinite_Recursion_With_Included_Objects() throws JsonProcessingException {
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
		
		department.setPositions(new ArrayList<>(Arrays.asList(positionOne, positionTwo)));
		
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
		assertDoesNotThrow(() -> jsonServiceUtils.convertEntityToJson(department));
		assertDoesNotThrow(() -> jsonServiceUtils.convertEntityToJson(positionOne));
		assertDoesNotThrow(() -> jsonServiceUtils.convertEntityToJson(positionTwo));
		assertDoesNotThrow(() -> jsonServiceUtils.convertEntityToJson(classifier));
		assertDoesNotThrow(() -> jsonServiceUtils.convertEntityToJson(task));
		assertDoesNotThrow(() -> jsonServiceUtils.convertEntityToJson(phone));
		assertDoesNotThrow(() -> jsonServiceUtils.convertEntityToJson(user));
		assertDoesNotThrow(() -> jsonServiceUtils.convertEntityToJson(order));
	}
	
	@Test
	public void collection_Of_Entities_Returns_Valid_Json() throws JsonProcessingException {
		//GIVEN Positions and Department from initNewEntities method
		department.setPositions(new ArrayList<Position>(Arrays.asList(positionOne, positionTwo)));
		
		//WHEN
		String positions = jsonServiceUtils.convertEntitiesToJson(Arrays.asList(positionOne, positionTwo));
		
		System.out.println(positions);
		
		//THEN
		assertAll(
			() -> assertTrue(positions.startsWith("[{") && positions.endsWith("}]")),
			() -> assertTrue(positions.contains("\"id\":222") && positions.contains("\"id\":333"))
		);
	}
	
	@Test
	public void collection_Of_Simple_Entities_Serializes_And_Deserializes_Properly() throws IOException, ClassNotFoundException {
		
		//GIVEN a list of simple Entities without nested ones
		
		department.setPositions(null);
		
		Department department1 = new Department();
		department1.setId(100);
		department1.setName("The Department 1");
		
		Department department2 = new Department();
		department2.setId(101);
		department2.setName("The Department 2");
		
		Position position1 = new Position();
		position1.setId(200);
		position1.setName("The Position 1");
		position1.setDescription("The description 1");
		position1.setCreated(ZonedDateTime.of(2017, 6, 15, 12, 35, 45, 0, ZoneId.systemDefault()));
		position1.setModified(ZonedDateTime.now().minusMinutes(5));
		
		Position position2 = new Position();
		position2.setId(201);
		position2.setName("The Position 2");
		position2.setDescription("The description 2");
		position2.setCreated(ZonedDateTime.of(2018, 6, 15, 12, 35, 45, 0, ZoneId.systemDefault()));
		position2.setModified(ZonedDateTime.now().minusMinutes(5));
		
		Position position3 = new Position();
		position3.setId(202);
		position3.setName("The Position 3");
		position3.setDescription("The description 3");
		position3.setCreated(ZonedDateTime.of(2016, 6, 15, 12, 35, 45, 0, ZoneId.systemDefault()));
		position3.setModified(ZonedDateTime.now().minusMinutes(5));
		
		
		//WHEN serialize and deserialize back
		
		String jsonDepartments = jsonServiceUtils.convertEntitiesToJson(
			new ArrayList<Department>(Arrays.asList(department, department1, department2)));
		String jsonPositions = jsonServiceUtils.convertEntitiesToJson(
			new ArrayList<Position>(Arrays.asList(position1, position2, position3)));
		
		List<Department> departments = Arrays.asList(
			jsonServiceUtils.getObjectMapper().readValue(jsonDepartments, Department[].class));
		List<Position> positions = Arrays.asList(
			jsonServiceUtils.getObjectMapper().readValue(jsonPositions, Position[].class));
		
		System.out.println("JSON: " + jsonDepartments);
		System.out.println("OBJ: " + departments);
		
		//THEN
		
		assertAll(
			() -> assertTrue(() -> departments.size() == 3),
			() -> assertTrue(() -> departments.get(0).getId() == 1 && departments.get(0).getName().equals("The Department")),
			() -> assertTrue(() -> departments.get(1).getId() == 100 && departments.get(1).getName().equals("The Department 1")),
			() -> assertTrue(() -> departments.get(2).getId() == 101 && departments.get(2).getName().equals("The Department 2"))
		);
		assertAll(
			() -> assertTrue(() -> positions.get(0).getId() == 200 &&
				positions.get(0).getName().equals("The Position 1") &&
				positions.get(0).getCreated().getMonthValue() == 6),
			() -> assertTrue(() -> positions.get(1).getId() == 201 &&
				positions.get(1).getName().equals("The Position 2") &&
				positions.get(1).getCreated().getMinute() == 35),
			() -> assertTrue(positions.get(2).getId() == 202 &&
				positions.get(2).getCreated().getYear() == 2016)
		);
	}
	
	@Test
	public void collection_Of_Linked_Entities_Serializes_And_Deserializes_Properly() throws IOException {
		
		//GIVEN a list of the same Entities as above but linked with each other. Plus Department & Positions from initNewEntities
		
		department.setPositions(new ArrayList<Position>(Arrays.asList(positionOne, positionTwo)));
		positionOne.setDepartment(department);
		positionTwo.setDepartment(department);
		
		Department department1 = new Department();
		department1.setId(100);
		department1.setName("The Department 1");
		
		Department department2 = new Department();
		department2.setId(101);
		department2.setName("The Department 2");
		
		Position position1 = new Position();
		position1.setId(200);
		position1.setName("The Position 1");
		position1.setDescription("The description 1");
		position1.setCreated(ZonedDateTime.of(2017, 6, 15, 12, 35, 45, 0, ZoneId.systemDefault()));
		position1.setModified(ZonedDateTime.now().minusMinutes(5));
		
		Position position2 = new Position();
		position2.setId(201);
		position2.setName("The Position 2");
		position2.setDescription("The description 2");
		position2.setCreated(ZonedDateTime.of(2018, 6, 15, 12, 35, 45, 0, ZoneId.systemDefault()));
		position2.setModified(ZonedDateTime.now().minusMinutes(5));
		
		Position position3 = new Position();
		position3.setId(202);
		position3.setName("The Position 3");
		position3.setDescription("The description 3");
		position3.setCreated(ZonedDateTime.of(2016, 6, 15, 12, 35, 45, 0, ZoneId.systemDefault()));
		position3.setModified(ZonedDateTime.now().minusMinutes(5));
		
		department1.setPositions(new ArrayList<Position>(Arrays.asList(position1, position2)));
		department2.setPositions(new ArrayList<Position>(Arrays.asList(position3)));
		position1.setDepartment(department1);
		position2.setDepartment(department1);
		position3.setDepartment(department2);
		
		//WHEN serialize and deserialize back
		
		String jsonDepartments = jsonServiceUtils.convertEntitiesToJson(
			new ArrayList<Department>(Arrays.asList(department, department1, department2)));
		String jsonPositions = jsonServiceUtils.convertEntitiesToJson(
			new ArrayList<Position>(Arrays.asList(positionOne, positionTwo, position1, position2, position3)));
		
		System.out.println(jsonDepartments);
		System.out.println(jsonPositions);
		
		List<Department> departments = Arrays.asList(
			jsonServiceUtils.getObjectMapper().readValue(jsonDepartments, Department[].class));
		List<Position> positions = Arrays.asList(
			jsonServiceUtils.getObjectMapper().readValue(jsonPositions, Position[].class));
		
		//THEN
		
		assertAll(
			() -> assertTrue(() -> departments.get(0).getId() == 1 && departments.get(0).getName().equals("The Department")),
			() -> assertTrue(() -> departments.get(1).getId() == 100 && departments.get(1).getName().equals("The Department 1")),
			() -> assertTrue(() -> departments.get(2).getId() == 101 && departments.get(2).getName().equals("The Department 2")),
			() -> assertNull(departments.get(0).getPositions()),
			() -> assertNull(departments.get(1).getPositions()),
			() -> assertNull(departments.get(2).getPositions())
		);
		
		assertAll(
			() -> assertTrue(() -> positions.get(0).getId() == 222 && positions.get(0).getCreated().getMinute() == 35),
			() -> assertTrue(() -> positions.get(2).getId() == 200 && positions.get(2).getDescription().equals("The description 1")),
			() -> assertNull(positions.get(1).getDepartment()),
			() -> assertNull(positions.get(2).getDepartment()),
			() -> assertNull(positions.get(3).getDepartment())
		);
	}
	
	@Test
	public void simple_WorkshopEntityResource_Should_Return_Json_With_WorkshopEntity_And_Links() throws Throwable {
		//GIVEN
		departmentsController = new DepartmentsController(new DepartmentsService(new DepartmentsDao()));
		Department department = new Department("DepartmentResource");
		DepartmentResource departmentResource = new DepartmentResource(department, departmentsController);
		
		//WHEN
		String jsonDepartmentResource = jsonServiceUtils.convertEntityResourceToJson(departmentResource);
		
		//THEN
		System.out.println(jsonDepartmentResource);
		assertNotNull(jsonDepartmentResource);
		assertTrue(jsonDepartmentResource.contains("{\"id\":0"));
		assertTrue(jsonDepartmentResource.contains("\"links\""));
		assertTrue(jsonDepartmentResource.contains("\"rel\":\"self\""));
		assertTrue(jsonDepartmentResource.contains("\"rel\":\"all\""));
		assertTrue(jsonDepartmentResource.contains("\"href\":\"/internal/departments/0\""));
		assertTrue(jsonDepartmentResource.contains("\"href\":\"/internal/departments\""));
	}
	
	private static Stream<Arguments> entitiesStream() {
		Classifier classifier = new Classifier();
		classifier.setId(65);
		classifier.setName("The Classifier");
		classifier.setPrice(new BigDecimal("45.25"));
		classifier.setCreated(ZonedDateTime.now().minusMonths(5));
		
		Department department = new Department();
		department.setId(38);
		department.setName("The Department");
		
		Position position = new Position();
		position.setId(2);
		position.setName("The Position");
		position.setDescription("The description");
		position.setCreated(ZonedDateTime.of(2019, 6, 15, 12, 35, 45, 0, ZoneId.systemDefault()));
		position.setModified(ZonedDateTime.now().minusMinutes(5));
		
		Order order = new Order();
		order.setId(10);
		order.setDescription("Order desc");
		order.setOverallPrice(new BigDecimal("333.22"));
		order.setCreated(ZonedDateTime.of(2019, 10, 13, 13, 45, 15, 0, ZoneId.systemDefault()));
		order.setDeadline(ZonedDateTime.now().plusDays(10));
		order.setModified(ZonedDateTime.now());
		
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
		task.setDeadline(ZonedDateTime.now().plusDays(15));
		
		Phone phone = new Phone();
		phone.setId(39);
		phone.setName("Phone name");
		phone.setPhone("98765");
		
		User user = new User();
		user.setId(100);
		user.setEmail("user@workshop.pro");
		user.setBirthday(LocalDate.now().minusYears(50));
		user.setCreated(ZonedDateTime.now().minusMonths(3));
		user.setModified(ZonedDateTime.now());
		user.setFirstName("First User Name");
		user.setPassword("54321");
		
		return Stream.of(Arguments.of(department), Arguments.of(position), Arguments.of(order),
			Arguments.of(employee), Arguments.of(task), Arguments.of(phone), Arguments.of(user));
	}
}