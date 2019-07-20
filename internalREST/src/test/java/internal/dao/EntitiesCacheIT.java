package internal.dao;

import internal.entities.Order;
import internal.entities.Task;
import internal.service.OrdersService;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ALL THE PRE INSTALLED DATA HAVE TO BE PRESENTED INTO THE "import.sql" MAIN FILE
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureTestEntityManager
//Add Hibernate statistics support during the tests
@TestPropertySource(properties = {"spring.jpa.properties.hibernate.generate_statistics=true"})
public class EntitiesCacheIT {
	
	@PersistenceContext
	EntityManager entityManager;
	@PersistenceUnit
	EntityManagerFactory entityManagerFactory;
	@Autowired
	OrdersService ordersService;
	
	@Test
	public void initTest() {
		assertNotNull(entityManager);
		assertNotNull(entityManagerFactory);
	}
	
	@Test
	@DisplayName("Check second level cache with raw Hibernate sessions")
	@Sql(scripts = {"classpath:entitiesCacheIT.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
	public void cache_Region_Order_Works_After_Second_Transaction() {
		//GIVEN
		//For own non-Spring transactions we have to get a separate copy of javax EntityManager
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		//The underlying provider object for the EntityManager. (Hibernate SessionFactory)
		SessionFactory sessionFactory = entityManager.getEntityManagerFactory().unwrap(SessionFactory.class);
		Statistics statistics = sessionFactory.getStatistics();
		
		//WHEN
		//First transaction
		entityManager.getTransaction().begin();
		Order orderFromDb = entityManager.find(Order.class, 10501L);
		entityManager.getTransaction().commit();
		//Also close a Hibernate session for stop using its session cache
		entityManager.close();
		//Create a new Hibernate session
		entityManager = entityManagerFactory.createEntityManager();
		
		//Second transaction
		entityManager.getTransaction().begin();
		Order orderFromCache = entityManager.find(Order.class, 10501L);
		entityManager.getTransaction().commit();
		//Close the underlying Hibernate session
		entityManager.close();
		
		//THEN
		assertNotNull(statistics);
		assertTrue(statistics.isStatisticsEnabled());
		//All opened sessions (transactions) were closed
		assertEquals(statistics.getSessionCloseCount(), statistics.getSessionCloseCount());
		//Cache region Order is presented
		assertTrue(Arrays.asList(statistics.getSecondLevelCacheRegionNames()).contains("internal.entities.Order"));
		//Orders have been retrieved within the different transactions and they are not the same objects in memory
		assertNotNull(orderFromDb);
		assertNotNull(orderFromCache);
		assertNotSame(orderFromDb, orderFromCache);
		//Cache region Order has only one object in it
		assertEquals(1, statistics.getDomainDataRegionStatistics("internal.entities.Order").getPutCount());
		//And this cache region has been retrieved only once
		assertEquals(1, statistics.getDomainDataRegionStatistics("internal.entities.Order").getHitCount());
		//Only the first session is missed the second level cache to derive an Order.id=501
		assertEquals(1, statistics.getDomainDataRegionStatistics("internal.entities.Order").getMissCount());
	}
	
	@Test
	@DisplayName("Check second level cache with Spring @Transactional EntityManager")
	@Sql(scripts = {"classpath:entitiesCacheIT.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
	public void cache_Region_Order_Works_After_Second_Transaction_With_String_Transactional() {
		//GIVEN
		SessionFactory sessionFactory =
			ordersService.getOrdersDao().getEntityManager().getEntityManagerFactory().unwrap(SessionFactory.class);
		Statistics statistics = sessionFactory.getStatistics();
		
		//WHEN two different transactions are used to derive a single Order
		Optional<Order> orderFromDb = ordersService.findById(10501L);
		Optional<Order> orderFromCache = ordersService.findById(10501L);
		
		//THEN
		//Two different Order objects must be got from two different transactions
		assertAll(
			() -> assertTrue(orderFromDb.isPresent()),
			() -> assertTrue(orderFromCache.isPresent()),
			() -> assertNotSame(orderFromDb, orderFromCache)
		);
		//The cache region Order is presented
		assertTrue(Arrays.asList(statistics.getSecondLevelCacheRegionNames()).contains("internal.entities.Order"));
		//The Order.id=501 has been put only once
		assertEquals(1, statistics.getDomainDataRegionStatistics("internal.entities.Order").getPutCount());
		//The Order.id=501 has been successfully got from the cache once
		assertEquals(1, statistics.getDomainDataRegionStatistics("internal.entities.Order").getHitCount());
		//Only the first transaction is missed the second level cache to derive an Order.id=501
		assertEquals(1, statistics.getDomainDataRegionStatistics("internal.entities.Order").getMissCount());
	}
	
	@ParameterizedTest
	@ValueSource(ints = {1, 2, 3})
	@DisplayName("Check second level cache with raw Hibernate sessions with various amount of transactions")
	@Sql(scripts = {"classpath:entitiesCacheIT.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
	public void cache_Region_Task_Works_After_Second_Transaction(int repeat) {
		//GIVEN unwrap underlying Hibernate SessionFactory and get Statistics
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		SessionFactory sessionFactory = entityManager.getEntityManagerFactory().unwrap(SessionFactory.class);
		Statistics statistics = sessionFactory.getStatistics();
		//Tasks are equals as Tasks but not as Objects as they will be retrieved by different transactions (sessions)
		//as every transaction returns a different (!==) object. Unlike within a single Hibernate session.
		Map<Task, Integer> tasksObjects = new IdentityHashMap<>(4);
		
		//WHEN
		//Create sessions 'repeat'-times, get the same Task and put in into IdentityHashMap
		for (int count = 0; count < repeat; count++) {
			entityManager.getTransaction().begin();
			Task task = entityManager.find(Task.class, 10511L);
			assertNotNull(task);
			tasksObjects.put(task, repeat);
			entityManager.getTransaction().commit();
			//To eliminate Hibernate session first-level cache
			entityManager.close();
			entityManager = entityManagerFactory.createEntityManager();
		}
		
		//THEN
		//taskObjects must contain 'repeat' amount of Tasks
		Set<Task> identityTasksSet = tasksObjects.keySet();
		assertEquals(repeat, identityTasksSet.size());
		//Hibernate second-level cache must contain the Task region
		assertTrue(Arrays.asList(statistics.getSecondLevelCacheRegionNames()).contains("internal.entities.Task"));
		//The Task was put in the second-level cache only once
		assertEquals(1, statistics.getDomainDataRegionStatistics("internal.entities.Task").getPutCount());
		//And only that first time there was a miss (as the cache was empty)
		assertEquals(1, statistics.getDomainDataRegionStatistics("internal.entities.Task").getMissCount());
		//And only if the repeat more than once, the subsequent transactions (sessions) hit the cache successful
		assertEquals(repeat-1, statistics.getDomainDataRegionStatistics("internal.entities.Task").getHitCount());
	}
	
	@AfterEach
	@DisplayName("Clears all the Database, the second-level cache and Hibernate Sessions Statistics.")
	private void tearDownDatabase() {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		
		entityManager.getTransaction().begin();
		entityManager.createQuery("DELETE FROM internal.entities.Task").executeUpdate();
		entityManager.createQuery("DELETE FROM internal.entities.Order").executeUpdate();
		entityManager.getTransaction().commit();
		
		entityManager.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics().clear();
	}
	
}
