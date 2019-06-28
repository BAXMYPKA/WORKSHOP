package internal.dao;

import internal.entities.Employee;
import internal.entities.Order;
import internal.entities.WorkshopEntity;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
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
	
	@PersistenceContext
	EntityManager entityManager;
	
	@Autowired
	EntityManagerFactory emf; //To support transactions
	
	static List<Employee> employees;
	static List<Order> orders;
	
	@Test
	public void context_Initialization() {
		assertNotNull(ordersDao);
		assertNotNull(ordersDao.getEntityManager());
		assertNotNull(entityManager);
	}
	
	@org.junit.jupiter.api.Order(1)
	@Test
	@Transactional
	@DisplayName("Employees are deleting one by one, Orders are deleting a batch way")
	public void deleting_Entities_By_One_And_By_Collection() {
		//GIVEN
		Optional<List<Employee>> persistedEmployees = employeesDao.findAll(0, 0, "", "");
		Optional<List<Order>> persistedOrders = ordersDao.findAll(0, 0, "", "");
		
		assertFalse(persistedEmployees.get().isEmpty());
		assertFalse(persistedOrders.get().isEmpty());
		
		//WHEN
		persistedEmployees.get().forEach(employee -> employeesDao.removeEntity(employee));
		ordersDao.removeEntities(persistedOrders.get());

		//THEN
		Optional<List<Employee>> emptyEmployees = employeesDao.findAll(0, 0, "", "");
		Optional<List<Order>> emptyOrders = ordersDao.findAll(0, 0, "", "");
		
		assertTrue(emptyEmployees.get().isEmpty());
		assertTrue(emptyOrders.get().isEmpty());
	}
	
	@ParameterizedTest
	@MethodSource("entitiesFactory")
	@DisplayName("Test DaoAbstract to be able to perform all the basic operations with EntityManager")
	@Transactional
	public void persist_Simple_Entities_By_EntityManager_With_Id_And_Management_Check(WorkshopEntity entity) {
		
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
	@DisplayName("Also checks the availability to be merge and returned back to the managed state from the detached")
	@Transactional
	public void batch_Persisting_Collections() {
		
		//GIVEN Employees and Orders collections. EntityManager doesn't contain them
		
//		ordersDao.setBatchSize(3);
		
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
		
		//Return them all to the managed state if Collection.size exceeds batchSize
/*
		for (int i = 0; i < employees.size(); i++) {
			employees.set(i, employeesDao.mergeEntity(employees.get(i)));
		}
		for (int i = 0; i < orders.size(); i++) {
			orders.set(i, ordersDao.mergeEntity(orders.get(i)));
		}
*/
		
		employees.forEach(employee -> assertTrue(entityManager.contains(employee)));
		orders.forEach(order -> assertTrue(entityManager.contains(order)));
	}
	
	@ParameterizedTest
	@ValueSource(ints = {1, 2, 3, 4})
	@DisplayName("The test doesn't consider any page num that exceeds the Entities quantity")
	@Transactional
	public void pagination_With_Limits_And_Offsets_Works_Properly(int source) {
		
		//GIVEN
		deleting_Entities_By_One_And_By_Collection();
		init();
		//Load some new test Entities
		employeesDao.persistEntities(employees);
		ordersDao.persistEntities(orders);
		//Get all Orders preloaded before and from the 'import.sql'
		Optional<List<Order>> allOrders = ordersDao.findAll(0, 0, "", "");
		//By default Dao sorts Entities by 'createdBy' field
		allOrders.get().sort((ord1, ord2) -> ord1.getCreated().compareTo(ord2.getCreated()));
		
		int pageSize = source;
		int pageNum = source;
		int itemNumToStartPageWith = (pageNum - 1) * pageSize; //Item num the page has to start with
		
		//WHEN
		
		Optional<List<Order>> page = ordersDao.findAll(pageSize, pageNum, "", "");
		
		//THEN
		
		//The result size may by less than the pageSize
		assertTrue(page.get().size() <= pageSize);
		//Depending on pageSize every page starts with the proper item
		assertSame(page.get().get(0), allOrders.get().get(itemNumToStartPageWith));
	}
	
	@Test
	public void find_Entity_By_Id_Email() {
		//GIVEN
		deleting_Entities_By_One_And_By_Collection();
		init();
		ordersDao.persistEntities(orders);
		employeesDao.persistEntities(employees);
	}
	
	@BeforeAll
	public static void init() {
		Employee employee1 = new Employee();
		employee1.setEmail("firstEmployee@workshop.pro");
		employee1.setFirstName("EmployeeFirstName");
		employee1.setLastName("StaticFirstLastName");
		employee1.setBirthday(LocalDate.of(1968, 7, 15));
		employee1.setPassword("12345");
		
		Employee employee2 = new Employee();
		employee2.setEmail("secondEmployee@workshop.pro");
		employee2.setFirstName("Employee2FirstName");
		employee2.setLastName("Employee2LastName");
		employee2.setBirthday(LocalDate.of(1967, 7, 15));
		employee2.setPassword("12345");
		
		Employee employee3 = new Employee();
		employee3.setEmail("thirdEmployee@workshop.pro");
		employee3.setFirstName("Employee3FirstName");
		employee3.setLastName("Employee3LastName");
		employee3.setBirthday(LocalDate.of(1970, 7, 15));
		employee3.setPassword("12345");
		
		Order order1 = new Order();
		order1.setDescription("Description");
		order1.setCreated(LocalDateTime.of(2018, 11, 20, 9, 35, 45));
		order1.setCreatedBy(employee1);
		
		Order order2 = new Order();
		order2.setCreatedBy(employee1);
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
		
		employees = new ArrayList<>(Arrays.asList(employee1, employee2, employee3));
		orders = new ArrayList<Order>(Arrays.asList(order1, order2, order3, order4, order5, order6, order7, order8, order9,
			order10, order11, order12, order13, order14, order15, order16, order17, order18, order19, order20, order21));
		
	}
	
	@AfterEach
	public void tearDown() {
		init();
	}
	
	public static Stream<? extends Arguments> entitiesFactory() {
		return Stream.of(Arguments.of(employees.get(0)), Arguments.of(employees.get(1)), Arguments.of(orders.get(0)),
			Arguments.of(orders.get(1)), Arguments.of(orders.get(2)));
	}
}