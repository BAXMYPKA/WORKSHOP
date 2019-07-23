package internal.service;

import internal.entities.Classifier;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EntitiesServiceAbstract subclasses inheritance and initialization with underlying DAOs testing.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureTestEntityManager
@Slf4j
class EntitiesServiceAbstractIT {
	
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
	@Order(3)
	public void departmentsService_Will_Persist_Departments() {
		//GIVEN
		Department departmentToPersist = new Department("The Department to be stored");
		
		//WHEN
		Optional<Department> departmentPersisted = departmentsService.persistOrMergeEntity(departmentToPersist);
		
		//THEN
		assertTrue(departmentPersisted.isPresent());
		assertEquals("The Department to be stored", departmentPersisted.get().getName());
	}
	
	@Test
	@Order(4)
	public void task_Service_Will_Find_Task_By_Id() {
		//GIVEN a Task to be persisted
		Task taskToPersist = new Task();
		taskToPersist.setName("Task name");
		
		//WHEN persist the Task by EntityManager and obtain its id
		Task taskPersisted = entityManager.persist(taskToPersist);
		long id = taskPersisted.getId();
		//TaskService will find that Task by its new id
		Optional<Task> taskFoundById = tasksService.findById(id);
		
		//THEN TaskService will find that Task by its new id
		assertTrue(taskFoundById.isPresent());
	}
	
	@Test
	@Order(4)
	public void departments_Service_Will_Persist_Department_With_Objects_Graph() {
		//GIVEN
		Department departmentToPersist = new Department("Department one");
		Position positionToPersist = new Position("Position name", departmentToPersist);
		positionToPersist.setDepartment(departmentToPersist);
		
		//WHEN
		Optional<Department> departmentPersisted = departmentsService.persistOrMergeEntity(departmentToPersist);
		
		//THEN
		
		//TODO: to complete
	}
	
}