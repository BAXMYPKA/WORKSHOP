package internal.dao;

import internal.entities.*;
import internal.entities.Order;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
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
class EntitiesDaoAbstractIT {
	
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
	@PersistenceContext
	EntityManager entityManager;
	@Autowired
	EntityManagerFactory emf; //To support transactions with emf.getEntityManager();
	
	static List<Employee> employees;
	static List<Order> orders;
	static List<User> users;
	static List<Phone> phones;
	static List<Classifier> classifiers;
	static List<Task> tasks;
	static List<Position> positions;
	static List<Department> departments;
	
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
	@DisplayName("The persisting simple entities without id (id=0) one by one should be successful")
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"Admin"})
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
			  () -> assertTrue(departmentOne.get().getId() > 0),
			  () -> assertTrue(departmentTwo.get().getId() > 0),
			  () -> assertTrue(classifierOne.get().getId() > 0),
			  () -> assertTrue(classifierTwo.get().getId() > 0)
		);
	}
	
	@Test
	@org.junit.jupiter.api.Order(3)
	@Transactional
	@DisplayName("Entities should be removed one by one.")
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"Admin"})
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
			  () -> assertTrue(departmentOne.get().getId() > 0),
			  () -> assertTrue(departmentTwo.get().getId() > 0),
			  () -> assertTrue(positionOne.get().getId() > 0),
			  () -> assertTrue(positionTwo.get().getId() > 0)
		);
		
		//WHEN
		//Remove one by one
		positionsDao.removeEntity(positionOne.get());
		positionsDao.removeEntity(positionTwo.get());
		departmentsDao.removeEntity(departmentOne.get());
		//Try to get the removed entities
		Optional<Position> positionOneById = positionsDao.findById(departmentOne.get().getId());
		Optional<Position> positionTwoById = positionsDao.findById(departmentOne.get().getId());
		Optional<Position> departmentOneId = positionsDao.findById(departmentOne.get().getId());
		
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
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"Admin"})
	public void batch_remove_Entities_By_Collection() {
		//GIVEN
		removeAllPersistedEntities();
//		initNewEntities();
		//Persisted entities collections
		departmentsDao.persistEntities(departments);
		positionsDao.persistEntities(positions);
		Optional<Collection<Employee>> employeesPersisted = employeesDao.persistEntities(EntitiesDaoAbstractIT.employees);
		Optional<Collection<Order>> ordersPersisted = ordersDao.persistEntities(EntitiesDaoAbstractIT.orders);
		//Check is the persistence successful
		assertTrue(employeesPersisted.isPresent());
		assertTrue(ordersPersisted.isPresent());
		
		//WHEN Batch total remove some kind of entities
		employeesDao.removeEntities(employeesPersisted.get());
		ordersDao.removeEntities(ordersPersisted.get());
		
		//THEN No entities of that kind should be found
		Optional<List<Employee>> emptyEmployees = employeesDao.findAll(0, 0, "", Sort.Direction.ASC);
		Optional<List<Order>> emptyOrders = ordersDao.findAll(0, 0, "", Sort.Direction.ASC);
		
		assertTrue(emptyEmployees.get().isEmpty());
		assertTrue(emptyOrders.get().isEmpty());
	}
	
	@Test
	@org.junit.jupiter.api.Order(5)
	@DisplayName("Also checks the availability to be merge and returned back to the managed state from the detached")
	@Transactional
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"Admin"})
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
		
		//THEN all entities are managed and have an id
		
		employees.forEach(employee -> assertTrue(employee.getId() > 0));
		employees.forEach(employee -> assertNotNull(employee.getCreated()));
		
		orders.forEach(order -> assertNotNull(order.getCreated()));
		orders.forEach(order -> assertTrue(order.getId() > 0));
		
		employees.forEach(employee -> assertTrue(entityManager.contains(employee)));
		orders.forEach(order -> assertTrue(entityManager.contains(order)));
	}
	
	
	@Test
	@DisplayName("New entity with including new entities graph should all be persisted")
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"Admin"})
	public void cascade_Persisting_New_Entities_Graph() {
		//GIVEN entities with CascadeType.PERSIST allowed
		//First pair to be cascade-persisted
		Department department1 = new Department("Department one");
		Position position1 = new Position("Position one", department1);
		department1.setPositions(Collections.singleton(position1));
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
			  () -> assertTrue(orderPersisted.get().getId() > 0)
		);
		//The Department has been persisted with the appropriate Position
		assertAll(
			  () -> assertTrue(departmentPersisted.get().getId() > 0),
			  () -> assertTrue(departmentPersisted.get().getPositions().iterator().next().getId() > 0)
		);
		//The Task has been persisted with appropriate new Classifiers
		assertAll(
			  () -> assertFalse(orderPersisted.get().getTasks().isEmpty()),
			  () -> assertTrue(orderPersisted.get().getTasks().iterator().next().getId() > 0),
			  () -> assertTrue(orderPersisted.get().getTasks().iterator().next().getClassifiers().iterator().next().getId() > 0)
		);
	}
	
	@Disabled
	@ParameterizedTest
	@ValueSource(ints = {1, 2, 3, 4})
	@DisplayName("The test doesn't consider any page num that exceeds the Entities quantity")
	@Transactional
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"Admin"})
	public void pagination_With_Limits_And_Offsets_Works_Properly(int source) {
		//TODO: to be done
		//GIVEN 21 pre-persisted Orders
		persistAllOrders();
		
		//WHEN
		
		//THEN
		
		//To clear the DataBase
		removeAllOrders();
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"Admin"})
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
		Employee employeeByEmail = employeesDao.findEmployeeByEmail(employeeEmail);
		Optional<User> userByEmail = usersDao.findByEmail(userEmail);
		
		//THEN
		assertAll(
			  () -> assertEquals(employeeEmail, employeeByEmail.getEmail()),
			  () -> assertTrue(userByEmail.isPresent()),
			  () -> assertEquals(userEmail, userByEmail.get().getEmail())
		);
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"Admin"})
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
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"Admin"})
	public void createdBy_Should_Be_Automatically_Persisted_From_SecurityContext() {
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
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"Admin"})
	public void createdBy_Set_Manually_Should_Be_Persisted() {
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
	
	@BeforeEach
	@DisplayName("Every Collection has to contain minimum 3 items!")
	public void initNewEntities() {
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
//	@AfterEach
	@Transactional
	public void removeAllPersistedEntities() {
		Optional<List<Employee>> employeesManaged = employeesDao.findAll(0, 0, null, null);
		employeesDao.removeEntities(employeesManaged.orElse(Collections.emptyList()));
		
		Optional<List<Task>> tasksManaged = tasksDao.findAll(0, 0, null, null);
		tasksDao.removeEntities(tasksManaged.orElse(Collections.emptyList()));
		
		Optional<List<Order>> ordersManaged = ordersDao.findAll(0, 0, null, null);
		ordersDao.removeEntities(ordersManaged.orElse(Collections.emptyList()));
		
		Optional<List<Classifier>> classifiersManaged = classifiersDao.findAll(0, 0, null, null);
		classifiersDao.refreshEntities(classifiersManaged.orElse(Collections.emptyList()));
		
		Optional<List<User>> usersManaged = usersDao.findAll(0, 0, null, null);
		usersDao.removeEntities(usersManaged.orElse(Collections.emptyList()));
		
		Optional<List<Position>> positionsManaged = positionsDao.findAll(0, 0, null, null);
		positionsDao.removeEntities(positionsManaged.orElse(Collections.emptyList()));
		
		Optional<List<Department>> departmentsManaged = departmentsDao.findAll(0, 0, null, null);
		departmentsDao.removeEntities(departmentsManaged.orElse(Collections.emptyList()));
		
		entityManager.clear();
	}
	
	@Transactional
	public void persistAllOrders() {
		Order order1 = new Order();
		order1.setDescription("Description one");
		order1.setCreated(ZonedDateTime.of(2018, 11, 20, 9, 35, 45, 0, ZoneId.systemDefault()));
		Order order2 = new Order();
		order2.setDescription("Description two");
		order2.setCreated(ZonedDateTime.of(2018, 11, 20, 9, 35, 45, 0, ZoneId.systemDefault()));
		Order order3 = new Order();
		order3.setCreated(ZonedDateTime.of(2017, 11, 20, 9, 35, 45, 0, ZoneId.systemDefault()));
		Order order4 = new Order();
		order4.setDescription("Description");
		Order order5 = new Order();
		Order order6 = new Order();
		Order order7 = new Order();
		Order order8 = new Order();
		Order order9 = new Order();
		Order order10 = new Order();
		Order order11 = new Order();
		Order order12 = new Order();
		Order order13 = new Order();
		Order order14 = new Order();
		Order order15 = new Order();
		Order order16 = new Order();
		Order order17 = new Order();
		Order order18 = new Order();
		Order order19 = new Order();
		Order order20 = new Order();
		Order order21 = new Order();
		
		orders = new ArrayList<>(Arrays.asList(order1, order2, order3, order4, order5, order6, order7, order8, order9,
			  order10, order11, order12, order13, order14, order15, order16, order17, order18, order19, order20, order21));
		
		ordersDao.persistEntities(orders);
	}
	
	@Transactional
	public void removeAllOrders() {
		ordersDao.refreshEntities(orders);
	}
	
	public static Stream<? extends Arguments> entitiesFactory() {
		return Stream.of(Arguments.of(employees.get(0)), Arguments.of(employees.get(1)), Arguments.of(orders.get(0)),
			  Arguments.of(orders.get(1)), Arguments.of(orders.get(2)));
	}
}