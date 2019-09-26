package workshop.internal.dao;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
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
		removeAllPersistedEntities();
//		initNewEntities();
		//Persisted entities collections
		departmentsDao.persistEntities(departments);
		positionsDao.persistEntities(positions);
		Optional<Collection<Employee>> employeesPersisted = employeesDao.persistEntities(WorkshopEntitiesDaoAbstractIT.employees);
		Optional<Collection<Order>> ordersPersisted = ordersDao.persistEntities(WorkshopEntitiesDaoAbstractIT.orders);
		//Check is the persistence successful
		assertTrue(employeesPersisted.isPresent());
		assertTrue(ordersPersisted.isPresent());
		
		//WHEN Batch total remove some kind of entities
		employeesDao.removeEntities(employeesPersisted.get());
		ordersDao.removeEntities(ordersPersisted.get());
		
		//THEN No entities of that kind should be found
		Optional<List<Employee>> emptyEmployees = employeesDao.findAllEntities(100, 0, "created", Sort.Direction.ASC);
		Optional<List<Order>> emptyOrders = ordersDao.findAllEntities(100, 0, "created", Sort.Direction.ASC);
		
		assertFalse(emptyEmployees.isPresent());
		assertFalse(emptyOrders.isPresent());
	}
	
	@Test
	@org.junit.jupiter.api.Order(5)
	@DisplayName("Also checks the availability to be merge and returned back to the managed state from the detached")
	@Transactional
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void batch_Persisting_Collections() {
		
		//GIVEN Employees and Orders collections. EntityManager doesn't contain them
		initNewEntities();
		removeAllPersistedEntities();
		
		//Pre-persist Entities without CascadeType.Persist
		departmentsDao.persistEntities(departments);
		positionsDao.persistEntities(positions);
		
		orders.forEach(order -> assertFalse(entityManager.contains(order)));
		employees.forEach(employee -> assertFalse(entityManager.contains(employee)));
		
		//WHEN
		
		employeesDao.persistEntities(employees);
		ordersDao.persistEntities(orders);
		
		//THEN all entities are managed and have an identifier
		
		employees.forEach(employee -> assertTrue(employee.getIdentifier() > 0));
		employees.forEach(employee -> assertNotNull(employee.getCreated()));
		
		orders.forEach(order -> assertNotNull(order.getCreated()));
		orders.forEach(order -> assertTrue(order.getIdentifier() > 0));
		
		employees.forEach(employee -> assertTrue(entityManager.contains(employee)));
		orders.forEach(order -> assertTrue(entityManager.contains(order)));
	}
	
	@Test
	@DisplayName("New entity with including new entities graph should all be persisted")
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void cascade_Persisting_New_Entities_Graph() {
		//GIVEN entities with CascadeType.PERSIST allowed
		//First pair to be cascade-persisted
		Department department1 = new Department("Department one");
		Position position1 = new Position("Position one", department1);
		department1.setPositions(new HashSet<>(Collections.singletonList(position1)));
		//Second pair to be cascade-persisted
		Order order1 = new Order();
		Classifier classifier1 = new Classifier("Classifier one", "", true, BigDecimal.ONE);
		Classifier classifier2 = new Classifier("Classifier two", "", true, BigDecimal.TEN);
		Task task1 = Task.builder().name("Task one").build();
		task1.setClassifiers(new HashSet<>(Arrays.asList(classifier1, classifier2)));
		task1.setOrder(order1);
		order1.setTasks(new HashSet<>(Collections.singleton(task1)));
		
		//WHEN
		Optional<Order> orderPersisted = ordersDao.persistEntity(order1);
		Optional<Department> departmentPersisted = departmentsDao.persistEntity(department1);
		
		//THEN
		assertAll(
			() -> assertTrue(orderPersisted.get().getIdentifier() > 0)
		);
		//The Department has been persisted with the appropriate Position
		assertAll(
			() -> assertTrue(departmentPersisted.get().getIdentifier() > 0),
			() -> assertTrue(departmentPersisted.get().getPositions().iterator().next().getIdentifier() > 0)
		);
		//The Task has been persisted with appropriate new Classifiers
		assertAll(
			() -> assertFalse(orderPersisted.get().getTasks().isEmpty()),
			() -> assertTrue(orderPersisted.get().getTasks().iterator().next().getIdentifier() > 0),
			() -> assertTrue(orderPersisted.get().getTasks().iterator().next().getClassifiers().iterator().next().getIdentifier() > 0)
		);
	}
	
	@ParameterizedTest
	@ValueSource(ints = {1, 2, 3, 4})
	@DisplayName("Check default pagination within limit and offsets")
	@Transactional
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void pagination_With_Limits_And_Offsets_Should_Work_Properly(int pageNum) {
		//GIVEN DAO layer with Spring Page starts pages count from 0
		// 21 pre-persisted Orders from order1 to order21
		persistAllOrders();
		int customPageSize = 4;
		
		//WHEN
		List<Order> ordersPageFromExisting = ordersDao.findAllEntities(customPageSize, pageNum, null, null).get();
		
		//THEN
		assertEquals(customPageSize, ordersPageFromExisting.size());
		
		//To clear the DataBase
		removeAllOrders();
	}
	
	@Test
	@DisplayName("Pagination with custom orderBy and default descending order")
	@Transactional
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void pagination_With_Custom_Sorting_Should_Sort_Properly() {
		//GIVEN
		// 21 pre-persisted Orders from order1 to order21
		persistAllOrders();
		int pageNum = 1;
		int pageSize = 5;
		
		//WHEN get a page with the descending order by default
		List<Order> ordersPageDescendingDefault = ordersDao.findAllEntities(
			pageSize, pageNum, "overallPrice", null).get();
		List<Order> ordersPageAscending = ordersDao.findAllEntities(
			pageSize, pageNum, "description", Sort.Direction.ASC).get();
		
		//THEN
		assertEquals(5, ordersPageDescendingDefault.size());
		assertEquals(5, ordersPageAscending.size());
		
		assertAll(
			() -> assertEquals(
				1,
				ordersPageDescendingDefault.get(0).getOverallPrice().compareTo(ordersPageDescendingDefault.get(1).getOverallPrice())),
			() -> assertEquals(
				1,
				ordersPageDescendingDefault.get(1).getOverallPrice().compareTo(ordersPageDescendingDefault.get(2).getOverallPrice())),
			() -> assertEquals(
				1,
				ordersPageDescendingDefault.get(3).getOverallPrice().compareTo(ordersPageDescendingDefault.get(4).getOverallPrice()))
		);
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
		
		//To clear the DataBase
		removeAllOrders();
	}
	
	@Test
	@DisplayName("Check pagination with the last partial filled page")
	@Transactional
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void pagination_With_Limits_And_Offsets_Last_Page_Should_Work_Properly() {
		//GIVEN DAO layer starts pages count from 0
		// 21 pre-persisted Orders from order1 to order21
		persistAllOrders();
		int customPageSize = 5;
		int lastPage = 4; //DAO layer starts pages count from 0
		//All existing Orders to compare with
		List<Order> allPersistedOrders = ordersDao.findAllEntities(0, 0, null, null).get();
		
		//WHEN
		List<Order> ordersPageFromExisting = ordersDao.findAllEntities(customPageSize, lastPage, null, null).get();
		
		//THEN the last fifth page should contain only the one last Order
		assertEquals(1, ordersPageFromExisting.size());
		assertEquals(
			allPersistedOrders.get(allPersistedOrders.size() - 1).getDescription(),
			ordersPageFromExisting.get(0).getDescription());
		
		//To clear the DataBase
		removeAllOrders();
	}
	
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
	
	@Test
	@DisplayName("Manually set 'createdBy' property should be persisted")
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void createdBy_Property_If_Set_Manually_Should_Be_Persisted() {
		//GIVEN
		//Pre persist entities
		Department department = new Department("Department");
		Position position = new Position("Position", department);
		Employee employeeToBeCreatedBy = new Employee("fn", "ln", "12345", "employee@workshop.pro",
			LocalDate.now().minusYears(50), position);
		
		departmentsDao.persistEntity(department);
		positionsDao.persistEntity(position);
		employeesDao.persistEntity(employeeToBeCreatedBy);
		
		//The given Classifier with createdBy preset
		Classifier classifier = new Classifier("Classifier", "Descr", true, BigDecimal.TEN);
		classifier.setCreatedBy(employeeToBeCreatedBy);
		
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
	
	@ParameterizedTest
	@CsvSource({"overallPrice, 10.11", "overallPrice, 10.21", "description, Order3", "description, Order11"})
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void findByProperty_Should_Return_Proper_String_Result(String propertyName, String propertyValue) {
		//GIVEN 21 pre-persisted Orders
		persistAllOrders();
		
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
		
		//To clear the database
		removeAllOrders();
	}
	
	@ParameterizedTest
	@CsvSource({"created, 2017-11-20T09:35:45+03:00", "deadline, 2020-12-30T12:00:00+00:00"})
	@DisplayName("FindByProperty should parse Temporal and return more than one result if they are")
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void findByProperty_Should_Return_Proper_ZonedDateTime_Results(String propertyName, String propertyValue) {
		//GIVEN 21 pre-persisted Orders
		persistAllOrders();
		
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
		
		//To clear the database
		removeAllOrders();
	}
	
	//	@Disabled
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	@Transactional
	public void positions_By_DepartmentId_Should_Be_Returned() {
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
		Optional<List<Position>> allPositionsByDepartment =
			positionsDao.findPositionsByDepartment(0, 0, null, null, departmentId);
		
		//THEN
		assertEquals(9, allPositionsByDepartment.get().size());
		assertTrue(allPositionsByDepartment.get().stream().anyMatch(position -> "Position 1".equals(position.getName())));
		assertTrue(allPositionsByDepartment.get().stream().anyMatch(position -> "Position 5".equals(position.getName())));
		assertTrue(allPositionsByDepartment.get().stream().anyMatch(position -> "Position 9".equals(position.getName())));
		
		removeAllPersistedEntities();
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	@Transactional
	public void positions_By_DepartmentId_Should_Be_Returned_OrderedBy_Name_Ascending() {
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
		List<Position> allPositionsByDepartmentOrderedByNameAsc =
			positionsDao.findPositionsByDepartment(0, 0, "name", Sort.Direction.ASC, departmentId).get();
		
		//THEN
		assertEquals(9, allPositionsByDepartmentOrderedByNameAsc.size());
		assertEquals("Position 1", allPositionsByDepartmentOrderedByNameAsc.get(0).getName());
		assertEquals("Position 9", allPositionsByDepartmentOrderedByNameAsc.get(8).getName());
		
		removeAllPersistedEntities();
	}
	
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
		removeAllPersistedEntities();
	}
	
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
		
		removeAllPersistedEntities();
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	@Transactional
	public void tasks_By_OrderId_Should_Return_All_Included_Tasks() {
		//GIVEN
		Order order1 = new Order();
		order1.setDescription("Order 1");
		
		Task task1 = new Task();
		task1.setName("Task 1");
		
		Task task2 = new Task();
		task2.setName("Task 2");
		
		Order orderPersisted = ordersDao.persistEntity(order1).get();
		long orderPersistedId = orderPersisted.getIdentifier();
		
		task1.setOrder(orderPersisted);
		task2.setOrder(orderPersisted);
		
		Task task1Persisted = tasksDao.persistEntity(task1).get();
		Task task2Persisted = tasksDao.persistEntity(task2).get();
		
		//WHEN
		List<Task> tasksByOrder =
			tasksDao.findAllTasksByOrder(0, 0, "name", Sort.Direction.DESC, orderPersistedId).get();
		
		//THEN
		assertEquals(2, tasksByOrder.size());
		assertTrue(tasksByOrder.contains(task1Persisted));
		assertTrue(tasksByOrder.contains(task2Persisted));
		
		ordersDao.removeEntity(order1);
	}
	
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
	
	@BeforeEach
	@DisplayName("Clears the DataBase from persisted Entities and initializes the new ones.")
	public void initNewEntities() {
		//Clear the DataBase
		removeAllPersistedEntities();
		
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
	
	/**
	 * Remove all the entities from the current DataBase
	 */
	@Transactional
	public void removeAllPersistedEntities() {
		Optional<List<Order>> ordersManaged = ordersDao.findAllEntities(0, 0);
		ordersDao.removeEntities(ordersManaged.orElse(Collections.emptyList()));
		
		Optional<List<Task>> tasksManaged = tasksDao.findAllEntities(0, 0);
		tasksDao.removeEntities(tasksManaged.orElse(Collections.emptyList()));
		
		Optional<List<Employee>> employeesManaged = employeesDao.findAllEntities(0, 0);
		employeesDao.removeEntities(employeesManaged.orElse(Collections.emptyList()));
		
		Optional<List<Classifier>> classifiersManaged = classifiersDao.findAllEntities(0, 0);
		classifiersDao.refreshEntities(classifiersManaged.orElse(Collections.emptyList()));
		
		Optional<List<User>> usersManaged = usersDao.findAllEntities(0, 0);
		usersDao.removeEntities(usersManaged.orElse(Collections.emptyList()));
		
		Optional<List<Position>> positionsManaged = positionsDao.findAllEntities(0, 0);
		positionsDao.removeEntities(positionsManaged.orElse(Collections.emptyList()));
		
		Optional<List<Department>> departmentsManaged = departmentsDao.findAllEntities(0, 0);
		departmentsDao.removeEntities(departmentsManaged.orElse(Collections.emptyList()));
		
		entityManager.clear();
	}
	
	@Transactional
	public void persistAllOrders() {
		//DO NOT CHANGE OR OVERWRITE THESE INSTANCES WITHOUT CHECK (!) THEIR VALUES ARE USED ACROSS this.getMethods()
		Order order1 = new Order();
		order1.setDescription("Order1");
		order1.setOverallPrice(BigDecimal.valueOf(10.01));
		order1.setCreated(ZonedDateTime.of(2018, 11, 20, 9, 35, 45, 0, ZoneId.of("Europe/Moscow")));
		Order order2 = new Order();
		order2.setOverallPrice(BigDecimal.valueOf(10.02));
		order2.setDescription("Order2");
		order2.setCreated(ZonedDateTime.of(2017, 11, 20, 9, 35, 45, 0, ZoneId.systemDefault()));
		Order order3 = Order.builder().description("Order3").build();
		order3.setCreated(ZonedDateTime.of(2016, 11, 20, 9, 35, 45, 0, ZoneId.of("Europe/Moscow")));
		order3.setOverallPrice(BigDecimal.valueOf(10.03));
		Order order4 = Order.builder().description("Order4").build();
		order4.setOverallPrice(BigDecimal.valueOf(10.04));
		Order order5 = Order.builder().description("Order5").build();
		order5.setOverallPrice(BigDecimal.valueOf(10.05));
		order5.setDeadline(ZonedDateTime.of(2020, 12, 30, 12, 0, 0, 0, ZoneId.of("UTC")));
		Order order6 = Order.builder().description("Order6").build();
		order6.setOverallPrice(BigDecimal.valueOf(10.06));
		order6.setDeadline(ZonedDateTime.of(2020, 12, 30, 12, 0, 0, 0, ZoneId.of("UTC")));
		Order order7 = Order.builder().description("Order7").build();
		order7.setOverallPrice(BigDecimal.valueOf(10.07));
		Order order8 = Order.builder().description("Order8").build();
		order8.setOverallPrice(BigDecimal.valueOf(10.08));
		Order order9 = Order.builder().description("Order9").build();
		order9.setOverallPrice(BigDecimal.valueOf(10.09));
		Order order10 = Order.builder().description("Order10").build();
		order10.setOverallPrice(BigDecimal.valueOf(10.10));
		Order order11 = Order.builder().description("Order11").build();
		order11.setOverallPrice(BigDecimal.valueOf(10.11));
		Order order12 = Order.builder().description("Order12").build();
		order12.setOverallPrice(BigDecimal.valueOf(10.12));
		Order order13 = Order.builder().description("Order13").build();
		order13.setOverallPrice(BigDecimal.valueOf(10.13));
		Order order14 = Order.builder().description("Order14").build();
		order14.setOverallPrice(BigDecimal.valueOf(10.14));
		Order order15 = Order.builder().description("Order15").build();
		order15.setOverallPrice(BigDecimal.valueOf(10.15));
		Order order16 = Order.builder().description("Order16").build();
		order16.setOverallPrice(BigDecimal.valueOf(10.16));
		Order order17 = Order.builder().description("Order17").build();
		order17.setOverallPrice(BigDecimal.valueOf(10.17));
		Order order18 = Order.builder().description("Order18").build();
		order18.setOverallPrice(BigDecimal.valueOf(10.18));
		Order order19 = Order.builder().description("Order19").build();
		order19.setOverallPrice(BigDecimal.valueOf(10.19));
		Order order20 = Order.builder().description("Order20").build();
		order20.setOverallPrice(BigDecimal.valueOf(10.20));
		Order order21 = Order.builder().description("Order21").build();
		order21.setOverallPrice(BigDecimal.valueOf(10.21));
		
		orders = new ArrayList<>(Arrays.asList(order1, order2, order3, order4, order5, order6, order7, order8, order9,
			order10, order11, order12, order13, order14, order15, order16, order17, order18, order19, order20, order21));
		
		ordersDao.persistEntities(orders);
	}
	
	@Transactional
	public void removeAllOrders() {
		ordersDao.removeEntities(orders);
	}
}