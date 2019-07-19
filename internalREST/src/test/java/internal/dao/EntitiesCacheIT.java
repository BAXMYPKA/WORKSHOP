package internal.dao;

import internal.entities.Order;
import internal.service.OrdersService;
import org.hibernate.Cache;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.internal.SessionImpl;
import org.hibernate.stat.SessionStatistics;
import org.hibernate.stat.Statistics;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestEntityManager
//Add Hibernate statistics support during the tests
@TestPropertySource(properties = {"spring.jpa.properties.hibernate.generate_statistics=true"})
@Sql(scripts = {"classpath:entitiesCacheIT.sql"})
public class EntitiesCacheIT {
	
	@PersistenceContext
	EntityManager entityManager;
	
	@Autowired
	OrdersService ordersService;
	
	@Test
	public void init() {
		assertNotNull(entityManager);
		assertNotNull(ordersService);
	}
	
	@Test
	@DisplayName("Cache Region 'Order' is using while entityManager.findById()")
	public void cache_Orders() {
		//GIVEN
		//The underlying provider object for the EntityManager, if available
		SessionFactory sessionFactory = ordersService.getOrdersDao().getEntityManager().getEntityManagerFactory().unwrap(SessionFactory.class);
		Statistics statistics = sessionFactory.getStatistics();
		
		//WHEN
		Optional<Order> fromDb = ordersService.findById(501);
		assertTrue(fromDb.isPresent());
		Optional<Order> fromCache = ordersService.findById(501);
		assertTrue(fromCache.isPresent());
		Optional<List<Order>> allOrdersFromDb = ordersService.findAllOrders(0, 0, null, null);
		assertTrue(allOrdersFromDb.isPresent());
		
		//THEN
		assertNotNull(statistics);
		assertTrue(statistics.isStatisticsEnabled());
		
		System.out.println(statistics.getEntityLoadCount());
		System.out.println(statistics.getConnectCount());
		
		System.out.println(statistics.getSecondLevelCacheHitCount());
		System.out.println(statistics.getSecondLevelCacheMissCount());
		System.out.println(statistics.getSecondLevelCachePutCount());
		System.out.println(Arrays.toString(statistics.getSecondLevelCacheRegionNames()));
	}
}
