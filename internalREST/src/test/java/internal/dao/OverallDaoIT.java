package internal.dao;

import internal.entities.Employee;
import internal.entities.Order;
import internal.entities.WorkshopEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Overall DaoAbstract with EntityManager test by performing some common operations for all the DAOs within existing ApplicationContext.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestEntityManager
@EnableTransactionManagement
@Transactional
//@Sql(scripts = {"classpath:testInit.sql"})
class OverallDaoIT {
	
	@Autowired
	OrdersDao ordersDao;
	
	@Autowired
	EmployeesDao employeesDao;
	
	@PersistenceContext
	EntityManager entityManager;
	
	static Employee staticEmployee;
	static List<Order> staticOrders;
	
	@Test
	public void init_Test() {
		assertNotNull(ordersDao);
		assertNotNull(ordersDao.getEntityManager());
		assertNotNull(entityManager);
	}
	
	@ParameterizedTest
	@MethodSource("entitiesFactory")
	@DisplayName("Test DaoAbstract to be able to perform all the basic operations with EntityManager")
	public void persist_Simple_Entities_By_EntityManager_With_Id_And_Management_Check(WorkshopEntity entity) {
		System.out.println(employeesDao.getBatchSize());
		if ("Employee".equals(entity.getClass().getSimpleName())) {
			//GIVEN
			Employee employee = (Employee) entity;
			
			assertFalse(entityManager.contains(employee));//Not managed
			assertEquals(0, employee.getId());
			//WHEN
			employeesDao.persistEntity((Employee) entity);
			//THEN
			assertTrue(employee.getId() > 0);//The id has been set
			assertTrue(entityManager.contains(employee));//Managed
		} else if ("Order".equals(entity.getClass().getSimpleName())) {
			//GIVEN
			Order order = (Order) entity;
			
			assertFalse(entityManager.contains(order));//Not managed
			assertEquals(0, order.getId());
			//WHEN
			ordersDao.persistEntity((Order) entity);
			//THEN
			assertTrue(order.getId() > 0);//The id has been set
			assertTrue(entityManager.contains(order));//Managed
		}
	}
	
	public static Stream<? extends Arguments> entitiesFactory() {
		Employee employee1 = new Employee();
		employee1.setEmail("testEmployee@workshop.pro");
		employee1.setBirthday(LocalDate.of(1968, 7, 15));
		employee1.setPassword("12345");
		
		Employee employee2 = new Employee();
		employee2.setEmail("testEmployee2@workshop.pro");
		employee2.setBirthday(LocalDate.of(1967, 7, 15));
		employee2.setPassword("12345");
		
		Order order1 = new Order();
		order1.setDescription("Description");
		order1.setCreatedBy(employee1);
		
		Order order2 = new Order();
		order2.setCreatedBy(employee1);
		order2.setCreated(LocalDateTime.of(2018, 11, 20, 9, 35, 45));
		
		Order order3 = new Order();
		order3.setCreatedBy(employee2);
		order3.setCreated(LocalDateTime.of(2017, 11, 20, 9, 35, 45));
		
		return Stream.of(Arguments.of(employee1), Arguments.of(employee2), Arguments.of(order1), Arguments.of(order2),
			Arguments.of(order3));
	}
	
	@BeforeAll
	public static void init() {
		staticEmployee = new Employee();
		staticEmployee.setEmail("staticTestEmployee2@workshop.pro");
		staticEmployee.setFirstName("StaticFirst");
		staticEmployee.setLastName("StaticLast");
		staticEmployee.setBirthday(LocalDate.of(1968, 7, 15));
		staticEmployee.setPassword("12345");
		
		Order order1 = new Order();
		order1.setDescription("Description");
		order1.setCreatedBy(staticEmployee);
		
		Order order2 = new Order();
		order2.setCreatedBy(staticEmployee);
		
		Order order3 = new Order();
		order3.setCreatedBy(staticEmployee);
		
		Order order4 = new Order();
		order4.setCreatedBy(staticEmployee);
		
		Order order5 = new Order();
		order5.setCreatedBy(staticEmployee);
		
		Order order6 = new Order();
		order6.setCreatedBy(staticEmployee);
		
		Order order7 = new Order();
		order7.setCreatedBy(staticEmployee);
		
		Order order8 = new Order();
		order8.setCreatedBy(staticEmployee);
		
		Order order9 = new Order();
		order9.setCreatedBy(staticEmployee);
		
		Order order10 = new Order();
		order10.setCreatedBy(staticEmployee);
	}
	
}