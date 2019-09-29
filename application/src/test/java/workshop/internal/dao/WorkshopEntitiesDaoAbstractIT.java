package workshop.internal.dao;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import workshop.internal.entities.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Overall DaoAbstract with EntityManager test by performing some common operations for all the DAOs
 * within existing ApplicationContext.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestEntityManager
@EnableTransactionManagement
@Transactional
@TestPropertySource(properties = {""})
@Slf4j
class WorkshopEntitiesDaoAbstractIT {
	
	static List<Employee> employees;
	static List<Order> orders;
	static List<User> users;
	static List<Phone> phones;
	static List<Classifier> classifiers;
	static List<Task> tasks;
	static List<Position> positions;
	static List<Department> departments;
	@Autowired
	OrdersDao ordersDao;
	@Autowired
	EmployeesDao employeesDao;
	@Autowired
	TasksDao tasksDao;
	@Autowired
	ClassifiersDao classifiersDao;
	@Autowired
	UsersDao usersDao;
	@Autowired
	PositionsDao positionsDao;
	@Autowired
	DepartmentsDao departmentsDao;
	@Autowired
	InternalAuthoritiesDao internalAuthoritiesDao;
	@PersistenceContext
	EntityManager entityManager;
	@Autowired
	EntityManagerFactory emf; //To support transactions with emf.getEntityManager();
	
	public static Stream<? extends Arguments> entitiesFactory() {
		return Stream.of(Arguments.of(employees.get(0)), Arguments.of(employees.get(1)), Arguments.of(orders.get(0)),
			Arguments.of(orders.get(1)), Arguments.of(orders.get(2)));
	}
	
	@Test
	@org.junit.jupiter.api.Order(1)
	public void context_Should_Be_Initialized() {
		assertNotNull(ordersDao);
		assertNotNull(ordersDao.getEntityManager());
		assertNotNull(entityManager);
	}
	
	@Test
	@org.junit.jupiter.api.Order(2)
	@Transactional
	@DisplayName("The persisting simple entities without identifier (identifier=0) one by one should be successful")
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void persist_Simple_Entities_One_By_One_Should_Be_Successful() {
		//GIVEN
		//New entities
		Department department1 = new Department("Department one");
		Department department2 = new Department("Department two");
		Classifier classifier1 = Classifier.builder().name("Classifier one").description("").isOfficial(true)
			.price(BigDecimal.valueOf(20.20)).build();
		Classifier classifier2 = Classifier.builder().name("Classifier two").description("").isOfficial(true)
			.price(BigDecimal.valueOf(30.35)).build();
		
		//WHEN Persist entities one by one
		Optional<Department> departmentOne = departmentsDao.persistEntity(department1);
		Optional<Department> departmentTwo = departmentsDao.persistEntity(department2);
		Optional<Classifier> classifierOne = classifiersDao.persistEntity(classifier1);
		Optional<Classifier> classifierTwo = classifiersDao.persistEntity(classifier2);
		
		//THEN all of them got their ids
		assertAll(
			() -> assertTrue(departmentOne.get().getIdentifier() > 0),
			() -> assertTrue(departmentTwo.get().getIdentifier() > 0),
			() -> assertTrue(classifierOne.get().getIdentifier() > 0),
			() -> assertTrue(classifierTwo.get().getIdentifier() > 0)
		);
	}
	
	@Test
	@org.junit.jupiter.api.Order(3)
	@Transactional
	@DisplayName("Entities should be removed one by one.")
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void remove_Entities_One_By_One() {
		//GIVEN
		//New entities
		Department department1 = new Department("Department one");
		Department department2 = new Department("Department two");
		Position position1 = new Position("Position one", department1);
		Position position2 = new Position("Position two", department2);
		//Persisted entities
		Optional<Department> departmentOne = departmentsDao.persistEntity(department1);
		Optional<Department> departmentTwo = departmentsDao.persistEntity(department2);
		Optional<Position> positionOne = positionsDao.persistEntity(position1);
		Optional<Position> positionTwo = positionsDao.persistEntity(position2);
		//Is persistence successful (just a check)
		assertAll(
			() -> assertTrue(departmentOne.get().getIdentifier() > 0),
			() -> assertTrue(departmentTwo.get().getIdentifier() > 0),
			() -> assertTrue(positionOne.get().getIdentifier() > 0),
			() -> assertTrue(positionTwo.get().getIdentifier() > 0)
		);
		
		//WHEN
		//Remove one by one
		positionsDao.removeEntity(positionOne.get());
		positionsDao.removeEntity(positionTwo.get());
		departmentsDao.removeEntity(departmentOne.get());
		//Try to get the removed entities
		Optional<Position> positionOneById = positionsDao.findById(departmentOne.get().getIdentifier());
		Optional<Position> positionTwoById = positionsDao.findById(departmentOne.get().getIdentifier());
		Optional<Position> departmentOneId = positionsDao.findById(departmentOne.get().getIdentifier());
		
		//THEN
		assertAll(
			() -> assertFalse(positionOneById.isPresent()),
			() -> assertFalse(positionTwoById.isPresent()),
			() -> assertFalse(departmentOneId.isPresent())
		);
	}
	
	@Test
	@org.junit.jupiter.api.Order(4)
	@Transactional
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void batch_remove_Entities_By_Collection() {
		//GIVEN
		Department department1 = new Department("Delete me 1");
		Department department2 = new Department("Delete me 2");
		Department department3 = new Department("Delete me 3");
		//Check the persistence
		Collection<Department> departments =
			departmentsDao.persistEntities(Arrays.asList(department1, department2, department3)).get();
		departments.forEach(department -> assertNotNull(department.getIdentifier()));
		
		//WHEN
		departmentsDao.removeEntities(departments);
		//THEN No entities of that kind should be found
		departments.forEach(department -> assertFalse(departmentsDao.isExist(department.getIdentifier())));
	}
	
	@Test
	@DisplayName("New entity with including new entities graph should all be persisted")
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void cascade_Persisting_New_Order_And_Its_Tasks_Should_Persist_All() {
		//GIVEN entities with CascadeType.PERSIST allowed
		Order order1 = new Order();
		Task task1 = Task.builder().name("Task one 1").order(order1).build();
		System.out.println(task1.getPrice());
		order1.setTasks(new HashSet<>(Collections.singleton(task1)));
		
		//WHEN
		Order orderPersisted = ordersDao.persistEntity(order1).get();
		
		//THEN
		assertAll(
			() -> assertNotNull(orderPersisted.getIdentifier()),
			() -> assertNotNull(orderPersisted.getTasks().iterator().next().getIdentifier())
		);
	}
	
	@Disabled
	@Test
	@DisplayName("Pagination with custom orderBy and default descending order")
	@Transactional
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void pagination_With_Custom_Sorting_Should_Sort_Properly() {
		//GIVEN
		// 21 pre-persisted Orders from order1 to order21
		int pageNum = 1;
		int pageSize = 5;
		
		//WHEN
		List<Order> ordersPageAscending = ordersDao.findAllEntities(
			pageSize, pageNum, "description", Sort.Direction.ASC).get();
		
		//THEN
		assertEquals(5, ordersPageAscending.size());
		
		assertAll(
			() -> assertEquals(
				-1,
				ordersPageAscending.get(0).getOverallPrice().compareTo(ordersPageAscending.get(1).getOverallPrice())),
			() -> assertEquals(
				-1,
				ordersPageAscending.get(1).getOverallPrice().compareTo(ordersPageAscending.get(2).getOverallPrice())),
			() -> assertEquals(
				-1,
				ordersPageAscending.get(3).getOverallPrice().compareTo(ordersPageAscending.get(4).getOverallPrice()))
		);
	}
	
	@Disabled
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void find_Entities_By_Email_Should_Return_Entities_With_Emails() {
		//GIVEN
		//Pre-persist necessary entities
		departmentsDao.persistEntities(departments);
		positionsDao.persistEntities(positions);
		
		String employeeEmail = "employeeToBeFoune@workshop.pro";
		String userEmail = "userToBeFound@user.com";
		//Entities to be persisted
		Employee employee = new Employee("fn", "ln", "12345", employeeEmail,
			LocalDate.now().minusYears(50), positions.get(0));
		User user = new User(userEmail);
		//Persisting
		employeesDao.persistEntity(employee);
		usersDao.persistEntity(user);
		
		//WHEN
		Employee employeeByEmail = employeesDao.findEmployeeByEmail(employeeEmail).get();
		Optional<User> userByEmail = usersDao.findByEmail(userEmail);
		
		//THEN
		assertAll(
			() -> assertEquals(employeeEmail, employeeByEmail.getEmail()),
			() -> assertTrue(userByEmail.isPresent()),
			() -> assertEquals(userEmail, userByEmail.get().getEmail())
		);
	}
	
	@Disabled
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	@DisplayName("JPA should persist and return ZonedDateTime in UTC despite a set one.")
	public void zonedDateTime_Should_Be_Saved_And_Returned_In_UTC_TimeZone() {
		//GIVEN an Employee with Europe/Moscow (+3) TimeZone
		ZonedDateTime europeMoscowZone = ZonedDateTime.of(
			2019, 1, 30, 12, 30, 0, 0, ZoneId.of("Europe/Moscow"));
		ZonedDateTime utcZone = europeMoscowZone.withZoneSameInstant(ZoneId.of("UTC"));
		
		Employee employee = employees.get(0);
		employee.setFinished(europeMoscowZone);
		//Pre persist required objects graph
		departmentsDao.persistEntities(departments);
		positionsDao.persistEntities(positions);
		
		//WHEN persist and get the Entity back
		Optional<Employee> employeeFromDb = employeesDao.persistEntity(employee);
		
		//THEN receive the Entity with UTC-corrected ZonedDateTime
		//Just a check that UTC zone 3 hours less
		assertEquals(utcZone.getHour(), europeMoscowZone.minusHours(3).getHour());
		//Persisted Entity now has the UTC-corrected field
		assertTrue(employeeFromDb.get().getFinished().isEqual(utcZone));
	}
	
	@Disabled
	@Test
	@DisplayName("CreatedBy should by automatically set from the SecurityContext")
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void createdBy_Property_Should_Be_Automatically_Persisted_From_SecurityContext() {
		//GIVEN
		//Pre persist entities.
		Department department = new Department("Department");
		Position position = new Position("Position", department);
		Employee employeeToBeCreatedBy = new Employee("Admin", "ln", "54321",
			"admin@workshop.pro", LocalDate.now().minusYears(50), position);
		Employee employeeNotToBeCreatedBy = new Employee("Employee", "ln", "12345",
			"employee@workshop.pro", LocalDate.now().minusYears(55), position);
		
		departmentsDao.persistEntity(department);
		positionsDao.persistEntity(position);
		employeesDao.persistEntities(new ArrayList<Employee>(Arrays.asList(employeeToBeCreatedBy, employeeNotToBeCreatedBy)));
		//This Employee has to be automatically got from SecurityContext
		SecurityContextHolder.getContext().setAuthentication(
			new UsernamePasswordAuthenticationToken(employeeToBeCreatedBy, "",
				new ArrayList<>(Collections.singletonList(new SimpleGrantedAuthority("Admin")))));
		
		//The given Classifier without createdBy
		Classifier classifier = new Classifier("Classifier", "Descr", true, BigDecimal.TEN);
		
		//WHEN
		Optional<Classifier> classifierPersisted = classifiersDao.persistEntity(classifier);
		
		//THEN
		assertEquals(employeeToBeCreatedBy, classifierPersisted.get().getCreatedBy());
	}
	
	@ParameterizedTest
	@ValueSource(strings = {"property", "anotherProperty"})
	@DisplayName("FindByProperty should throw IllegalArgExc with exact message if Entity doesn't have such a property")
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void findByProperty_Should_Throw_IllegalArgsEx_If_Entity_Doesnt_Have_Such_Property(String propertyName) {
		//GIVEN
		
		//WHEN
		IllegalArgumentException exceptionMessage = assertThrows(IllegalArgumentException.class,
			() -> departmentsDao.findByProperty(propertyName, "value"));
		
		assertEquals(
			Department.class.getSimpleName() + " doesn't have such a '" + propertyName + "' property!",
			exceptionMessage.getMessage());
		
	}
	
	@Disabled
	@ParameterizedTest
	@CsvSource({"overallPrice, 10.11", "overallPrice, 10.21", "description, Order3", "description, Order11"})
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void findByProperty_Should_Return_Proper_String_Result(String propertyName, String propertyValue) {
		//GIVEN 21 pre-persisted Orders
		
		//WHEN
		Optional<List<Order>> orderByProperty = ordersDao.findByProperty(propertyName, propertyValue);
		
		//THEN
		assertTrue(orderByProperty.isPresent());
		if ("overallPrice".equals(propertyName)) {
			assertEquals(new BigDecimal(propertyValue), orderByProperty.get().get(0).getOverallPrice());
		} else if ("description".equals(propertyName)) {
			assertEquals(propertyValue, orderByProperty.get().get(0).getDescription());
		} else if ("deadline".equals(propertyName)) {
			System.out.println(orderByProperty.get().get(0).getDeadline());
		}
	}
	
	@Disabled
	@ParameterizedTest
	@CsvSource({"created, 2017-11-20T09:35:45+03:00", "deadline, 2020-12-30T12:00:00+00:00"})
	@DisplayName("FindByProperty should parse Temporal and return more than one result if they are")
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void findByProperty_Should_Return_Proper_ZonedDateTime_Results(String propertyName, String propertyValue) {
		//GIVEN 21 pre-persisted Orders
		
		ZonedDateTime expectedCreatedInOrder2 = ZonedDateTime.of(
			2017, 11, 20, 9, 35, 45, 0, ZoneId.systemDefault());
		
		ZonedDateTime expectedDeadline = ZonedDateTime.of(
			2020, 12, 30, 12, 0, 0, 0, ZoneId.of("UTC"));
		
		//WHEN
		Optional<List<Order>> orderByProperty = ordersDao.findByProperty(propertyName, propertyValue);
		
		//THEN
		assertTrue(orderByProperty.isPresent());
		
		if ("created".equals(propertyName)) {
			assertEquals(expectedCreatedInOrder2, orderByProperty.get().get(0).getCreated());
		} else if ("deadline".equals(propertyName)) {
			//We have 2 Orders with deadline=2020-12-30T12:00:00+00:00
			assertEquals(2, orderByProperty.get().size());
			assertEquals(expectedDeadline, orderByProperty.get().get(0).getDeadline());
		}
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	@Transactional
	public void positions_By_DepartmentId_Should_Be_Returned() {
		//GIVEN
		Department department1 = new Department("Department id 1");
		departmentsDao.persistEntity(department1).get();
		Long departmentId = department1.getIdentifier();
		
		Position position1 = new Position("Position id 1", department1);
		Position position2 = new Position("Position id 2", department1);
		Position position3 = new Position("Position id 3", department1);
		Position position4 = new Position("Position id 4", department1);
		Position position5 = new Position("Position id 5", department1);
		Position position6 = new Position("Position id 6", department1);
		Position position7 = new Position("Position id 7", department1);
		Position position8 = new Position("Position id 8", department1);
		Position position9 = new Position("Position id 9", department1);
		
		positionsDao.persistEntities(Arrays.asList(position1, position2, position3, position4, position5, position6,
			position7, position8, position9));
		
		//WHEN
		Optional<List<Position>> allPositionsByDepartment =
			positionsDao.
				findPositionsByDepartment(20, 0, "created", Sort.Direction.DESC, departmentId);
		
		//THEN
		assertEquals(9, allPositionsByDepartment.get().size());
		assertTrue(allPositionsByDepartment.get().stream().anyMatch(position -> "Position id 1".equals(position.getName())));
		assertTrue(allPositionsByDepartment.get().stream().anyMatch(position -> "Position id 5".equals(position.getName())));
		assertTrue(allPositionsByDepartment.get().stream().anyMatch(position -> "Position id 9".equals(position.getName())));
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	@Transactional
	public void positions_By_DepartmentId_Should_Be_Returned_OrderedBy_Name_Ascending() {
		//GIVEN
		Department department1 = new Department("Department is 1");
		departmentsDao.persistEntity(department1).get();
		Long departmentId = department1.getIdentifier();
		
		Position position1 = new Position("Position is 1", department1);
		Position position2 = new Position("Position is 2", department1);
		Position position3 = new Position("Position is 3", department1);
		Position position4 = new Position("Position is 4", department1);
		Position position5 = new Position("Position is 5", department1);
		Position position6 = new Position("Position is 6", department1);
		Position position7 = new Position("Position is 7", department1);
		Position position8 = new Position("Position is 8", department1);
		Position position9 = new Position("Position is 9", department1);
		
		positionsDao.persistEntities(Arrays.asList(position1, position2, position3, position4, position5, position6,
			position7, position8, position9));
		
		//WHEN
		List<Position> allPositionsByDepartmentOrderedByNameAsc =
			positionsDao.findPositionsByDepartment(20, 0, "name", Sort.Direction.ASC, departmentId).get();
		
		//THEN
		assertEquals(9, allPositionsByDepartmentOrderedByNameAsc.size());
		assertEquals("Position is 1", allPositionsByDepartmentOrderedByNameAsc.get(0).getName());
		assertEquals("Position is 9", allPositionsByDepartmentOrderedByNameAsc.get(8).getName());
	}
	
	@Disabled
	@ParameterizedTest
	@ValueSource(ints = {0, 2})
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	@Transactional
	public void positions_By_DepartmentId_Should_Return_First_And_Last_Pages_OrderedBy_Name_Descending(int pageNum) {
		//GIVEN
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
		//CascadeType.PERSIST will persist all the corresponding Positions
		department1.addPosition(position1, position2, position3, position4, position5, position6, position7,
			position8, position9);
		
		Department department1Persisted = departmentsDao.persistEntity(department1).get();
		Long departmentId = department1Persisted.getIdentifier();
		
		//WHEN
		List<Position> positionsByDepartmentOrderedByNameDesc =
			positionsDao.findPositionsByDepartment(3, pageNum, "name", Sort.Direction.DESC, departmentId).get();
		
		//THEN
		assertEquals(3, positionsByDepartmentOrderedByNameDesc.size());
		
		if (pageNum == 0) {
			assertEquals("Position 9", positionsByDepartmentOrderedByNameDesc.get(0).getName());
			assertEquals("Position 7", positionsByDepartmentOrderedByNameDesc.get(2).getName());
		} else if (pageNum == 2) {
			assertEquals("Position 3", positionsByDepartmentOrderedByNameDesc.get(0).getName());
			assertEquals("Position 1", positionsByDepartmentOrderedByNameDesc.get(2).getName());
		}
	}
	
	@Disabled
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	@Transactional
	public void department_By_PositionId_Should_Return_() {
		//GIVEN
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
		//CascadeType.PERSIST will persist all the corresponding Positions
		department1.addPosition(position1, position2, position3, position4, position5, position6, position7,
			position8);
		
		Department department1Persisted = departmentsDao.persistEntity(department1).get();
		long departmentId = department1Persisted.getIdentifier();
		
		Position positionPersisted = positionsDao.persistEntity(position9).get();
		Long positionId = positionPersisted.getIdentifier();
		
		//WHEN
		Department departmentByPosition = departmentsDao.findDepartmentByPosition(positionId).get();
		
		//THEN
		assertEquals(departmentId, departmentByPosition.getIdentifier());
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	@Transactional
	public void tasks_By_OrderId_Should_Return_All_Included_Tasks() {
		//GIVEN
		Order order1 = new Order();
		order1.setDescription("Order is 1");
		
		Task task1 = new Task();
		task1.setName("Task is 1");
		
		Task task2 = new Task();
		task2.setName("Task is 2");
		
		Order orderPersisted = ordersDao.persistEntity(order1).get();
		long orderPersistedId = orderPersisted.getIdentifier();
		
		task1.setOrder(orderPersisted);
		task2.setOrder(orderPersisted);
		
		Task task1Persisted = tasksDao.persistEntity(task1).get();
		Task task2Persisted = tasksDao.persistEntity(task2).get();
		
		//WHEN
		List<Task> tasksByOrder =
			tasksDao.findAllTasksByOrder(20, 0, "name", Sort.Direction.DESC, orderPersistedId).get();
		
		//THEN
		assertEquals(2, tasksByOrder.size());
		assertTrue(tasksByOrder.contains(task1Persisted));
		assertTrue(tasksByOrder.contains(task2Persisted));
		
		ordersDao.removeEntity(order1);
	}
	
	@Disabled
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	@Transactional
	public void classifier_Tasks_Should_Be_Returned_As_ManyToMany() {
		//GIVEN
		Order order1 = new Order();
		
		Classifier classifier1 = new Classifier("Classifier 1", "", true, BigDecimal.ONE);
		Classifier classifier2 = new Classifier("Classifier 2", "", true, BigDecimal.TEN);
		
		classifiersDao.persistEntities(Arrays.asList(classifier1, classifier2));
		
		Task task1 = Task.builder().name("Task 1").build();
		task1.setClassifiers(new HashSet<>(Arrays.asList(classifier1, classifier2)));
		task1.setOrder(order1);
		
		Task task2 = Task.builder().name("Task 2").build();
		task2.setClassifiers(new HashSet<>(Arrays.asList(classifier1, classifier2)));
		task2.setOrder(order1);
		
		Task task3 = Task.builder().name("Task 3").build();
		task3.setClassifiers(new HashSet<>(Arrays.asList(classifier1, classifier2)));
		task3.setOrder(order1);
		
		order1.setTasks(new HashSet<>(Arrays.asList(task1, task2, task3)));
		
		ordersDao.persistEntity(order1);
		
		//WHEN
		Optional<List<Task>> tasksByClassifier = tasksDao.findAllTasksByClassifier(
			10,
			0,
			"created",
			Sort.Direction.DESC,
			classifier1.getIdentifier());
		
		//THEN
		assertTrue(tasksByClassifier.isPresent());
		assertEquals(3, tasksByClassifier.get().size());
		assertAll(
			() -> assertTrue(tasksByClassifier.get().contains(task1)),
			() -> assertTrue(tasksByClassifier.get().contains(task2)),
			() -> assertTrue(tasksByClassifier.get().contains(task3))
		);
	}
	
	@Disabled
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void positions_By_Authority_Should_Return_All_Linked_Positions() {
		//GIVEN
		Department department = new Department("Department 1");
		departmentsDao.persistEntity(department);
		
		Position position1 = new Position("Position 1", department);
		Position position2 = new Position("Position 2", department);
		Position position3 = new Position("Position 3", department);
		positionsDao.persistEntities(Arrays.asList(position1, position2, position3));
		
		InternalAuthority authority1 = new InternalAuthority("Authority 1");
		InternalAuthority authority2 = new InternalAuthority("Authority 2");
		authority1.setPositions(new HashSet<>(Arrays.asList(position1, position2)));
		authority2.setPositions(new HashSet<>(Collections.singletonList(position3)));
		internalAuthoritiesDao.persistEntities(Arrays.asList(authority1, authority2));
		
		authority1 = internalAuthoritiesDao.findById(authority1.getIdentifier()).get();
		position1 = positionsDao.findById(position1.getIdentifier()).get();
		
		//WHEN
		List<Position> positionsByAuthority1 =
			positionsDao.findPositionsByInternalAuthority(
				10, 0, "created", Sort.Direction.DESC, authority1.getIdentifier()).get();
		
		List<Position> positionsByAuthority2 =
			positionsDao.findPositionsByInternalAuthority(
				10, 0, "created", Sort.Direction.DESC, authority2.getIdentifier()).get();
		
		//THEN
		assertEquals(2, positionsByAuthority1.size());
		assertTrue(positionsByAuthority1.contains(position1));
		assertTrue(positionsByAuthority1.contains(position2));
		
		assertEquals(1, positionsByAuthority2.size());
		assertTrue(positionsByAuthority2.contains(position3));
		
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void count_All_Positions_By_Department() {
		//GIVEN
		Department department = new Department("Department unique 100");
		departmentsDao.persistEntity(department);
		
		Position position1 = new Position("Position unique 100", department);
		Position position2 = new Position("Position unique 200", department);
		Position position3 = new Position("Position unique 300", department);
		positionsDao.persistEntities(Arrays.asList(position1, position2, position3));
		
		//WHEN
		long totalPositionsByDepartment = positionsDao.countAllPositionsByDepartment(department.getIdentifier());
		long totalPositionsByUnknownDepartment = positionsDao.countAllPositionsByDepartment(100501L);
		
		
		//THEN
		assertEquals(3, totalPositionsByDepartment);
		assertEquals(0, totalPositionsByUnknownDepartment);
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void count_All_Positions_By_InternalAuthority() {
		//GIVEN
		Department department = new Department("Department unique is 100");
		departmentsDao.persistEntity(department);
		
		Position position1 = new Position("Position unique is 100", department);
		Position position2 = new Position("Position unique is 200", department);
		Position position3 = new Position("Position unique is 300", department);
		positionsDao.persistEntities(Arrays.asList(position1, position2, position3));
		
		InternalAuthority authority = new InternalAuthority("Authority 1 for Positions");
		authority.setPositions(new HashSet<>(Arrays.asList(position1, position2, position3)));
		internalAuthoritiesDao.persistEntity(authority);
		
		//WHEN
		long totalPositionsByInternalAuthority = positionsDao.countAllPositionsByInternalAuthority(authority.getIdentifier());
		long totalPositionsByUnknownAuthority = positionsDao.countAllPositionsByInternalAuthority(100501L);
		
		//THEN
		assertEquals(3, totalPositionsByInternalAuthority);
		assertEquals(0, totalPositionsByUnknownAuthority);
	}
	
	public void initNewEntities() {
		//Init new Entities
		Department department = new Department();
		department.setName("Department one");
		
		Position position = new Position("Position one", department);
		
		Employee employee1 = new Employee();
		employee1.setEmail("firstEmployee@workshop.pro");
		employee1.setFirstName("EmployeeFirstName");
		employee1.setLastName("StaticFirstLastName");
		employee1.setBirthday(LocalDate.of(1968, 7, 15));
		employee1.setPassword("12345");
		employee1.setPosition(position);
		
		Employee employee2 = new Employee();
		employee2.setEmail("secondEmployee@workshop.pro");
		employee2.setFirstName("Employee2FirstName");
		employee2.setLastName("Employee2LastName");
		employee2.setBirthday(LocalDate.of(1967, 7, 15));
		employee2.setPassword("12345");
		employee2.setPosition(position);
		
		Employee employee3 = new Employee();
		employee3.setEmail("thirdEmployee@workshop.pro");
		employee3.setFirstName("Employee3FirstName");
		employee3.setLastName("Employee3LastName");
		employee3.setBirthday(LocalDate.of(1970, 7, 15));
		employee3.setPassword("12345");
		employee3.setPosition(position);
		
		Order order1 = new Order();
		order1.setDescription("Description one");
		order1.setCreated(ZonedDateTime.of(2018, 11, 20, 9, 35, 45, 0, ZoneId.systemDefault()));
		order1.setCreatedBy(employee1);
		
		Order order2 = new Order();
		order2.setCreatedBy(employee1);
		order2.setDescription("Description two");
		order2.setCreated(ZonedDateTime.of(2018, 11, 20, 9, 35, 45, 0, ZoneId.systemDefault()));
		
		Order order3 = new Order();
		order3.setCreatedBy(employee1);
		order3.setCreated(ZonedDateTime.of(2017, 11, 20, 9, 35, 45, 0, ZoneId.systemDefault()));
		
		Order order4 = new Order();
		order4.setDescription("Description");
		order4.setCreatedBy(employee1);
		
		Order order5 = new Order();
		order5.setCreatedBy(employee2);
		
		Order order6 = new Order();
		order6.setCreatedBy(employee2);
		
		Order order7 = new Order();
		order7.setCreatedBy(employee2);
		
		Order order8 = new Order();
		order8.setCreatedBy(employee2);
		
		Order order9 = new Order();
		order9.setCreatedBy(employee2);
		
		Order order10 = new Order();
		order10.setCreatedBy(employee3);
		
		Order order11 = new Order();
		order11.setCreatedBy(employee3);
		
		Order order12 = new Order();
		order12.setCreatedBy(employee3);
		
		Order order13 = new Order();
		order13.setCreatedBy(employee3);
		
		Order order14 = new Order();
		order14.setCreatedBy(employee3);
		
		Order order15 = new Order();
		order15.setCreatedBy(employee3);
		
		Order order16 = new Order();
		order16.setCreatedBy(employee3);
		
		Order order17 = new Order();
		order17.setCreatedBy(employee3);
		
		Order order18 = new Order();
		order18.setCreatedBy(employee3);
		
		Order order19 = new Order();
		order19.setCreatedBy(employee3);
		
		Order order20 = new Order();
		order20.setCreatedBy(employee3);
		
		Order order21 = new Order();
		order21.setCreatedBy(employee3);
		
		User user1 = new User();
		user1.setFirstName("UserOneFirst");
		user1.setLastName("UserOneLast");
		user1.setEmail("userone@workshop.pro");
		user1.setBirthday(LocalDate.now().minusYears(18));
		user1.setPassword("12345");
		
		User user2 = new User();
		user2.setFirstName("UserTwoFirst");
		user2.setLastName("UserTwoLast");
		user2.setEmail("usertwo@workshop.pro");
		user2.setBirthday(LocalDate.now().minusYears(15));
		user2.setPassword("12345");
		
		User user3 = new User();
		user3.setFirstName("UserThreeFirst");
		user3.setLastName("UserThreeLast");
		user3.setEmail("userthree@workshop.pro");
		user3.setBirthday(LocalDate.now().minusYears(16).minusDays(1));
		user3.setPassword("12345");
		
		Phone phone1 = new Phone();
		phone1.setName("Mobile");
		phone1.setPhone("12345678");
		
		Phone phone2 = new Phone();
		phone2.setName("Mobile");
		phone2.setPhone("123456789");
		
		Phone phone3 = new Phone();
		phone3.setName("Mobile");
		phone3.setPhone("1234567890");
		
		Task task1 = new Task();
		task1.setName("Task One");
		task1.setDeadline(ZonedDateTime.now().plusDays(5));
		
		Task task2 = new Task();
		task2.setName("Task Two");
		task2.setDeadline(ZonedDateTime.now().plusDays(5));
		
		Task task3 = new Task();
		task3.setName("Task Three");
		task3.setDeadline(ZonedDateTime.now().plusDays(5));
		
		Classifier classifier1 = new Classifier();
		classifier1.setName("Classifier One");
		classifier1.setPrice(new BigDecimal(20.20));
		classifier1.setDescription("The First Classifier");
		
		Classifier classifier2 = new Classifier();
		classifier2.setName("Classifier Two");
		classifier2.setPrice(new BigDecimal(30.50));
		classifier2.setDescription("The Second Classifier");
		
		Classifier classifier3 = new Classifier();
		classifier3.setName("Classifier Three");
		classifier3.setPrice(new BigDecimal(40.10));
		classifier3.setDescription("The Third Classifier");
		
		departments = new ArrayList<Department>(Collections.singletonList(department));
		positions = new ArrayList<Position>(Arrays.asList(position));
		employees = new ArrayList<Employee>(Arrays.asList(employee1, employee2, employee3));
		orders = new ArrayList<Order>(Arrays.asList(order1, order2, order3, order4, order5, order6, order7, order8, order9,
			order10, order11, order12, order13, order14, order15, order16, order17, order18, order19, order20, order21));
		users = new ArrayList<>(Arrays.asList(user1, user2, user3));
		phones = new ArrayList<>(Arrays.asList(phone1, phone2, phone3));
		tasks = new ArrayList<>(Arrays.asList(task1, task2, task3));
		classifiers = new ArrayList<>(Arrays.asList(classifier1, classifier2, classifier3));
	}
}