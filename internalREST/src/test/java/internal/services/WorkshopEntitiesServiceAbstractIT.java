package internal.services;

import internal.entities.*;
import javafx.geometry.Pos;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.parameters.P;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
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
	private PositionsService positionsService;
	@Autowired
	private OrdersService ordersService;
	@Autowired
	private TasksService tasksService;
	@Autowired
	private ClassifiersService classifiersService;
	
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
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"Admin"})
	public void set_New_Task_To_Classifier() {
		//GIVEN as the Task is the owning side, we add Classifiers into the Task, not wise versa
		Classifier classifier1 = new Classifier("Classifier 1", "", true, BigDecimal.TEN);
		classifiersService.persistEntity(classifier1);
		
		Classifier classifier2 = new Classifier("Classifier 2", "", true, BigDecimal.TEN);
		classifiersService.persistEntity(classifier2);
		
		Order order = new Order();
		order.setDescription("Order 1");
		
		Task task = new Task();
		task.setName("Task 1");
		
		ordersService.persistEntity(order);
		
		task.setOrder(order);
		tasksService.persistEntity(task);
		
		order.setTasks(new HashSet<>(Collections.singletonList(task)));
		
		task.addClassifiers(classifier1, classifier2);
		tasksService.mergeEntity(task);
		
		//WHEN
		Order orderPersisted = ordersService.findById(order.getIdentifier());
		
		//THEN
		assertNotNull(orderPersisted.getTasks().iterator().next().getIdentifier());
		assertEquals(2, orderPersisted.getTasks().iterator().next().getClassifiers().size());
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"Admin"})
	public void delete_Task_From_Classifier() {
		//GIVEN as the Task is the owning side, we add Classifiers into the Task, not wise versa
		Classifier classifier1 = new Classifier("Classifier 1", "", true, BigDecimal.TEN);
		classifiersService.persistEntity(classifier1);
		
		Classifier classifier2 = new Classifier("Classifier 2", "", true, BigDecimal.TEN);
		classifiersService.persistEntity(classifier2);
		
		Order order = new Order();
		order.setDescription("Order 1");
		
		Task task = new Task();
		task.setName("Task 1");
		
		ordersService.persistEntity(order);
		
		task.setOrder(order);
		tasksService.persistEntity(task);
		
		order.setTasks(new HashSet<>(Collections.singletonList(task)));
		
		task.addClassifiers(classifier1, classifier2);
		tasksService.mergeEntity(task);
		
		//WHEN
		
		Classifier classifierToBeDeletedFromTask = classifiersService.findById(classifier1.getIdentifier());
		Task persistedTask = tasksService.findById(task.getIdentifier());
		
		persistedTask.getClassifiers().remove(classifierToBeDeletedFromTask);
		tasksService.mergeEntity(persistedTask);
		
		Order orderPersisted = ordersService.findById(order.getIdentifier());
		
		//THEN
		assertNotNull(orderPersisted.getTasks().iterator().next().getIdentifier());
		//Only one Classifier left by Task
		assertEquals(1, orderPersisted.getTasks().iterator().next().getClassifiers().size());
		//And that Classifier is 'classifier2'
		assertEquals(classifier2.getIdentifier(),
					 orderPersisted.getTasks().iterator().next().getClassifiers().iterator().next().getIdentifier());
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"Admin"})
	public void set_Department_To_Position_Will_Be_Updated_For_Both_of_Them() {
		//GIVEN
		Department department = new Department("Department 1");
		
		departmentsService.persistEntity(department);
		//Only Position is updated with the Department set
		Position position = new Position();
		position.setName("Position 1");
		position.setDepartment(department);
		positionsService.persistEntity(position);
		
		//WHEN
		Department persistedDepartment = departmentsService.findById(department.getIdentifier());
		
		//THEN
		assertEquals(1, persistedDepartment.getPositions().size());
		assertEquals("Position 1", persistedDepartment.getPositions().iterator().next().getName());
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"Admin"})
	public void update_Department_On_Persisted_Position_Will_Be_Updated_For_Both_of_Them() {
		//GIVEN all Positions are set to department1
		Department department1 = new Department("Department 1");
		Department department2 = new Department("Department 2");
		departmentsService.persistEntities(department1, department2);
		
		Position position1 = new Position();
		position1.setName("Position 1");
		position1.setDepartment(department1);
		
		Position position2 = new Position();
		position2.setName("Position 2");
		position2.setDepartment(department1);
		positionsService.persistEntities(position1, position2);
		
		boolean equals = position1.equals(position2);
		System.out.println(equals);
		
		//WHEN
		
		Department persistedDepartment2 = departmentsService.findById(department2.getIdentifier());
		
		Position persistedPosition1 = positionsService.findById(position1.getIdentifier());
		Department department = persistedPosition1.getDepartment();
		persistedPosition1.setDepartment(persistedDepartment2);
		positionsService.mergeEntity(persistedPosition1);
		departmentsService.mergeEntity(department);
		
		Department persisted1Department = departmentsService.findById(department1.getIdentifier());
		Department persisted2Department = departmentsService.findById(department2.getIdentifier());
		
		//THEN
		System.out.println(persisted1Department);
		
	}
	
}