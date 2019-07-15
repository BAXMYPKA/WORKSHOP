package internal.dao;

import internal.entities.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.parameters.P;
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
import java.time.LocalDateTime;
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
//@Sql(scripts = {"classpath:data1.sql"})
class DaoIT {
	
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
	public void context_Initialization() {
		assertNotNull(ordersDao);
		assertNotNull(ordersDao.getEntityManager());
		assertNotNull(entityManager);
	}
	
	@org.junit.jupiter.api.Order(1)
	@Test
	@Transactional
	@DisplayName("Employees are deleting one by one, Orders are deleting in a batch way")
	public void deleting_Entities_By_One_And_By_Collection() {
		//GIVEN
		Optional<List<Employee>> persistedEmployees = employeesDao.findAll(0, 0, "", Sort.Direction.ASC);
		Optional<List<Order>> persistedOrders = ordersDao.findAll(0, 0, "", Sort.Direction.ASC);
		
		assertFalse(persistedEmployees.get().isEmpty());
		assertFalse(persistedOrders.get().isEmpty());
		
		//WHEN
		persistedEmployees.get().forEach(employee -> employeesDao.removeEntity(employee));
		ordersDao.removeEntities(persistedOrders.get());
		
		//THEN
		Optional<List<Employee>> emptyEmployees = employeesDao.findAll(0, 0, "", Sort.Direction.ASC);
		Optional<List<Order>> emptyOrders = ordersDao.findAll(0, 0, "", Sort.Direction.ASC);
		
		assertTrue(emptyEmployees.get().isEmpty());
		assertTrue(emptyOrders.get().isEmpty());
	}
	
	
	@Test
	@org.junit.jupiter.api.Order(2)
	@DisplayName("Also checks the availability to be merge and returned back to the managed state from the detached")
	@Transactional
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"Admin"})
	public void batch_Persisting_Collections() {
		
		//GIVEN Employees and Orders collections. EntityManager doesn't contain them
		init();
		clearContext();
		
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
	
	@ParameterizedTest
	@MethodSource("entitiesFactory")
	@DisplayName("Test DaoAbstract for being able to persist Entities without an id field")
	@Transactional
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"Admin"})
	public void persist_Simple_Entity(WorkshopEntity entity) {
		//Pre-persist Entities without CascadeType.PERSIST
		departmentsDao.persistEntities(departments);
		positionsDao.persistEntities(positions);
		
		if ("Employee".equals(entity.getClass().getSimpleName())) {
			//GIVEN
			Employee employee = (Employee) entity;
			
			assertFalse(entityManager.contains(employee)); //Not managed entity
			assertEquals(0, employee.getId());
			
			//WHEN
			employeesDao.persistEntity((Employee) entity);
			
			//THEN
			assertTrue(employee.getId() > 0); //The id has been set
			assertTrue(entityManager.contains(employee)); //Managed entity
		} else if ("Order".equals(entity.getClass().getSimpleName())) {
			
			//GIVEN
			Order order = (Order) entity;
			
			assertFalse(entityManager.contains(order)); //Not managed entity
			assertEquals(0, order.getId());
			
			//WHEN
			ordersDao.persistEntity((Order) entity);
			
			//THEN
			assertTrue(order.getId() > 0);//The id has been set
			assertTrue(entityManager.contains(order));//Managed
		}
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"Admin"})
	public void persist_Entities_With_Id_0_isSuccessful() {
		//GIVEN
		
		//Pre-persist Entities which have to be persisted separately
		departmentsDao.persistEntities(departments);
		positionsDao.persistEntities(positions);
		
		orders.forEach(order -> order.setId(0));
		employees.forEach(employee -> employee.setId(0));
		
		//WHEN
		ordersDao.persistEntities(orders);
		employeesDao.persistEntities(employees);
		
		//THEN
		Set<Long> ids = new HashSet<>();
		
		orders.forEach(order -> {
			assertTrue(order.getId() > 0);
			assertTrue(entityManager.contains(order));
			ids.add(order.getId());
			
		});
		
		employees.forEach(employee -> {
			assertTrue(employee.getId() > 0);
			assertTrue(entityManager.contains(employee));
			ids.add(employee.getId());
		});
		
		assertEquals(orders.size() + employees.size(), ids.size());
	}
	
	@RepeatedTest(3)
	@DisplayName("All the new entities tuples have to be persisted by JPA cascading by persisting a single Entity")
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"Admin"})
	public void cascade_Persisting_All_New_Entities() {
		//GIVEN all non-persisted Entities.
		
		//As every collection has to have at least 3 items, we can randomly use their indexes
		int i = ((int) (Math.random() * 3));
		int j = ((int) (Math.random() * 3));
		
		//Preparing tuples from new Entities
		users.forEach(user -> {
			user.setId(0);
			user.setPhones(new HashSet<Phone>(Arrays.asList(phones.get(0), phones.get(1))));
		});
		phones.forEach(phone -> {
			phone.setId(0);
		});
		classifiers.forEach(classifier -> {
			classifier.setId(0);
			classifier.setCreatedBy(employees.get(j));
		});
		tasks.forEach(task -> {
			task.setId(0);
			task.setClassifiers(new HashSet<Classifier>(Arrays.asList(classifiers.get(0), classifiers.get(1))));
			task.setCreatedBy(employees.get(i));
		});
		
		orders.forEach(order -> {
			order.setId(0);
			order.setCreatedFor(users.get(i));
			order.setTasks(new HashSet<Task>(Arrays.asList(tasks.get(0), tasks.get(1))));
		});
		
		//WHEN only Orders are persisted
		
		ordersDao.persistEntities(orders);
		
		//THEN all the tuples from those Orders have to be persisted either
		
		//Orders are persisted
		orders.forEach(order -> {
			entityManager.contains(order);
		});
		//Tasks from those Orders are persisted
		assertTrue(entityManager.contains(tasks.get(0)) && entityManager.contains(tasks.get(1)));
		//Classifiers from those Tasks are persisted
		assertTrue(entityManager.contains(classifiers.get(0)) && entityManager.contains(classifiers.get(1)));
		//User from those Orders is persisted
		assertTrue(entityManager.contains(users.get(i)));
		//Phones from that User are persisted
		assertTrue(entityManager.contains(phones.get(0)) && entityManager.contains(phones.get(1)));
	}
	
	@RepeatedTest(3)
	@DisplayName("New entities have to be cascade persisted, but the old ones not")
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"Admin"})
	public void cascade_Persisting_Partly_New_Entities() {
		//GIVEN persisted and non-persisted Entities.
		
		init();
		clearContext();
		
		//As every collection has to have at least 3 items, we can randomly use their indexes
		int i = ((int) (Math.random() * 3));
		int j = ((int) (Math.random() * 3));
		
		//Preparing tuples from new Entities
		users.forEach(user -> {
			user.setId(0);
			user.setPhones(new HashSet<Phone>(Arrays.asList(phones.get(0), phones.get(1))));
		});
		phones.forEach(phone -> {
			phone.setId(0);
		});
		tasks.forEach(task -> {
			task.setId(0);
			task.setClassifiers(new HashSet<Classifier>(Arrays.asList(classifiers.get(0), classifiers.get(1))));
			task.setCreatedBy(employees.get(i));
		});
		orders.forEach(order -> {
			order.setId(0);
			order.setCreatedFor(users.get(i));
			order.setTasks(new HashSet<Task>(Arrays.asList(tasks.get(0), tasks.get(1))));
		});
		
		//WHEN 1) pre-persist Classifiers 2) then persist Orders
		
		//Here we persist Classifiers for the following check
		classifiers.forEach(classifier -> {
			classifier.setCreatedBy(employees.get(j));
			classifiersDao.persistEntity(classifier);
		});
		//Check the Classifiers are persisted
		classifiers.forEach(classifier -> {
			assertTrue(entityManager.contains(classifier));
		});
		
		//Then persist Orders with pre-persisted Classifiers
		ordersDao.persistEntities(orders);
		
		//THEN all the tuples from those Orders have to be persisted either
		
		//Orders are persisted
		orders.forEach(order -> {
			assertTrue(entityManager.contains(order));
		});
		//Tasks from those Orders are persisted
		assertTrue(entityManager.contains(tasks.get(0)) && entityManager.contains(tasks.get(1)));
		//User from those Orders is persisted
		assertTrue(entityManager.contains(users.get(i)));
		//Phones from that User are persisted
		assertTrue(entityManager.contains(phones.get(0)) && entityManager.contains(phones.get(1)));
	}
	
	@ParameterizedTest
	@ValueSource(ints = {1, 2, 3, 4})
	@DisplayName("The test doesn't consider any page num that exceeds the Entities quantity")
	@Transactional
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"Admin"})
	public void pagination_With_Limits_And_Offsets_Works_Properly(int source) {
		
		//GIVEN
		//Clear the InMemory DataBase and reinit all the Entities for the test
		init();
		//Load some new test Entities
		departmentsDao.persistEntities(departments);
		positionsDao.persistEntities(positions);
		employeesDao.persistEntities(employees);
		ordersDao.persistEntities(orders);
		
		//Get all the preloaded Orders
		Optional<List<Order>> allOrders = ordersDao.findAll(0, 0, "", Sort.Direction.DESC);
		
		//By default Dao sorts Entities by 'created' field
		allOrders.get().sort((ord1, ord2) -> ord1.getCreated().compareTo(ord2.getCreated()));
		
		int pageSize = source;
		int pageNum = source;
		//Page formula to count the number of item from a Global ordered list the current Page has to start with
		int itemNumToStartPageWith = (pageNum - 1) * pageSize;
		
		//WHEN
		
		Optional<List<Order>> page = ordersDao.findAll(pageSize, pageNum, "", Sort.Direction.DESC);
		
		//THEN
		
		//The result size may by less than the pageSize
		assertTrue(page.get().size() <= pageSize);
		//Depending on pageSize every page starts with the proper item
		assertSame(page.get().get(0), allOrders.get().get(itemNumToStartPageWith));
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"Admin"})
	public void find_Entity_By_Id_Email() {
		//GIVEN
		init();
		clearContext();
		
		departmentsDao.persistEntities(departments);
		positionsDao.persistEntities(positions);
		ordersDao.persistEntities(orders);
		employeesDao.persistEntities(employees);
		
		//Take the first non-persisted Order
		Order nonPersistedOrder = orders.get(0);
		long nonPersistedOrderId = nonPersistedOrder.getId();
		
		//Take the first non-persisted Employee
		Employee nonPersistedEmployee = employees.get(0);
		String nonPersistedEmail = nonPersistedEmployee.getEmail();
		
		//WHEN
		//Try to found persisted Order by id
		Optional<Order> foundByIdOrder = ordersDao.findById(nonPersistedOrderId);
		//Try to found persisted Employee by email
		Employee persistedEmployee = employeesDao.findEmployeeByEmail(nonPersistedEmail);
		
		//THEN
		//Order has to be found
		assertNotNull(foundByIdOrder.get());
		assertTrue(entityManager.contains(foundByIdOrder.get()));
		assertEquals(nonPersistedOrder.getId(), foundByIdOrder.get().getId());
		assertEquals(nonPersistedOrder.getCreated(), foundByIdOrder.get().getCreated());
		//Employee has to be found
		assertNotNull(persistedEmployee);
		assertEquals(nonPersistedEmail, persistedEmployee.getEmail());
	}
	
	@BeforeAll
	@DisplayName("Every Collection has to contain minimum 3 items!")
	public static void init() {
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
		order1.setCreated(LocalDateTime.of(2018, 11, 20, 9, 35, 45));
		order1.setCreatedBy(employee1);
		
		Order order2 = new Order();
		order2.setCreatedBy(employee1);
		order2.setDescription("Description two");
		order2.setCreated(LocalDateTime.of(2018, 11, 20, 9, 35, 45));
		
		Order order3 = new Order();
		order3.setCreatedBy(employee1);
		order3.setCreated(LocalDateTime.of(2017, 11, 20, 9, 35, 45));
		
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
		task1.setDeadline(LocalDateTime.now().plusDays(5));
		
		Task task2 = new Task();
		task2.setName("Task Two");
		task2.setDeadline(LocalDateTime.now().plusDays(5));
		
		Task task3 = new Task();
		task3.setName("Task Three");
		task3.setDeadline(LocalDateTime.now().plusDays(5));
		
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
	
	@Transactional
	public void persistEntities() {
		departmentsDao.persistEntities(departments);
		positionsDao.persistEntities(positions);
		employeesDao.persistEntities(employees);
		ordersDao.persistEntities(orders);
	}
	
	@Transactional
	public void clearContext() {
		Optional<List<Employee>> employeesManaged = employeesDao.findAll(0, 0, null, null);
		employeesDao.removeEntities(employeesManaged.get());
		
		Optional<List<Order>> ordersManaged = ordersDao.findAll(0, 0, null, null);
		ordersDao.removeEntities(ordersManaged.get());
		
		Optional<List<Task>> tasksManaged = tasksDao.findAll(0, 0, null, null);
		tasksDao.removeEntities(tasksManaged.get());
		
		Optional<List<Classifier>> classifiersManaged = classifiersDao.findAll(0, 0, null, null);
		classifiersDao.refreshEntities(classifiersManaged.get());
		
		Optional<List<User>> usersManaged = usersDao.findAll(0, 0, null, null);
		usersDao.removeEntities(usersManaged.get());
		
		Optional<List<Position>> positionsManaged = positionsDao.findAll(0, 0, null, null);
		positionsDao.removeEntities(positionsManaged.get());
		
		Optional<List<Department>> departmentsManaged = departmentsDao.findAll(0, 0, null, null);
		departmentsDao.removeEntities(departmentsManaged.get());
		
		entityManager.clear();
	}
	
	public static Stream<? extends Arguments> entitiesFactory() {
		init();
		return Stream.of(Arguments.of(employees.get(0)), Arguments.of(employees.get(1)), Arguments.of(orders.get(0)),
			Arguments.of(orders.get(1)), Arguments.of(orders.get(2)));
	}
}