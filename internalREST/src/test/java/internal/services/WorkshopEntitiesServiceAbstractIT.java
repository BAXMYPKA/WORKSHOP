package internal.services;

import internal.entities.Department;
import internal.entities.Order;
import internal.entities.Position;
import internal.entities.Task;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EntitiesServiceAbstract subclasses inheritance and initialization with underlying DAOs testing.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureTestEntityManager
@Slf4j
class WorkshopEntitiesServiceAbstractIT {
	
	@Autowired
	private TestEntityManager entityManager;
	@Autowired
	private DepartmentsService departmentsService;
	@Autowired
	private OrdersService ordersService;
	@Autowired
	private TasksService tasksService;
	
	@Test
	@DisplayName("EntitiesServiceAbstract subclasses initializes and autowires successfully")
	public void entitiesServiceAbstract_Subclasses_Initializes_And_Autowires() {
		assertAll(
			() -> assertNotNull(entityManager),
			() -> assertNotNull(departmentsService)
		);
	}
	
	@Test
	public void departmentsService_Should_Persist_Departments() {
		//GIVEN
		Department departmentToPersist = new Department("The Department to be stored");
		
		//WHEN
		Department departmentPersisted = departmentsService.persistOrMergeEntity(departmentToPersist);
		
		//THEN
		assertNotNull(departmentPersisted);
		assertEquals("The Department to be stored", departmentPersisted.getName());
	}
	
	@Test
	@Transactional
	public void taskService_Should_Find_Task_By_Id() {
		//GIVEN a Task to be persisted
		Task taskToPersist = new Task();
		taskToPersist.setName("Task name");
		
		//WHEN persist the Task by EntityManager and obtain its identifier
		Task taskPersisted = entityManager.persist(taskToPersist);
		long id = taskPersisted.getIdentifier();
		//TaskService will find that Task by its new identifier
		Task taskFoundById = tasksService.findById(id);
		
		//THEN TaskService will find that Task by its new identifier
		assertNotNull(taskFoundById);
	}
	
	@Test
	public void departmentsService_Should_Persist_Department_With_Attached_New_Objects_Graph() {
		//GIVEN
		Department departmentToPersist = new Department("Department one");
		Position positionToPersist = new Position("Position name", departmentToPersist);
		
		departmentToPersist.addPosition(positionToPersist);
		
		//WHEN
		Department departmentPersisted = departmentsService.persistOrMergeEntity(departmentToPersist);
		
		//THEN
		assertNotNull(departmentPersisted);
		//Position has been persisted
		assertTrue(departmentPersisted.getPositions().iterator().next().getIdentifier() > 0);
		//Position is same
		assertEquals(departmentPersisted.getPositions().iterator().next().getName(), positionToPersist.getName());
	}
	
	//TODO: to complete deriving Tasks from Order
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"Admin"})
	public void departmentsServiceh() {
		//GIVEN
		
		Order order1 = new Order();
		order1.setDescription("Order 1");
		
		Task task1 = new Task();
		task1.setName("Task 1");
		
		Task task2 = new Task();
		task2.setName("Task 2");
		
		Order orderPersisted = ordersService.persistEntity(order1);
		long orderPersistedId = orderPersisted.getIdentifier();
		
		task1.setOrder(orderPersisted);
		task2.setOrder(orderPersisted);
		
		Task task1Persisted = tasksService.persistEntity(task1);
		Task task2Persisted = tasksService.persistEntity(task2);
		
		orderPersisted.setTasks(new HashSet<>(Arrays.asList(task1Persisted, task2Persisted)));
		ordersService.mergeEntity(orderPersisted);
		
		Order orderPersisted2 = ordersService.findById(orderPersistedId);
		System.out.println(orderPersisted2);
		
		//WHEN
//		List<Task> tasksByOrder =
//			tasksDao.findAllTasksByOrder(0, 0, "name", Sort.Direction.DESC, orderPersistedId).get();
	}
	
}