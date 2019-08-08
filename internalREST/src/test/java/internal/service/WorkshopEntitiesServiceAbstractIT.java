package internal.service;

import internal.entities.Department;
import internal.entities.Position;
import internal.entities.Task;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

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
	private TasksService tasksService;
	
	@Test
	@Order(1)
	@DisplayName("EntitiesServiceAbstract subclasses initializes and autowires successfully")
	public void entitiesServiceAbstract_Subclasses_Initializes_And_Autowires() {
		assertAll(
			() -> assertNotNull(entityManager),
			() -> assertNotNull(departmentsService)
		);
	}
	
	@Test
	@Order(2)
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
	@Order(3)
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
	@Order(4)
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
	
}