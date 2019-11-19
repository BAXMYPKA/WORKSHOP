package workshop.internal.services;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import workshop.internal.entities.*;
import workshop.exceptions.EntityNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDate;
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
@DirtiesContext
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
	@Autowired
	private EmployeesService employeesService;
	@Autowired
	private PhonesService phonesService;
	@Autowired
	private UsersService usersService;
	
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
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
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
		
		task.addClassifier(classifier1, classifier2);
		tasksService.mergeEntity(task);
		
		//WHEN
		Order orderPersisted = ordersService.findById(order.getIdentifier());
		
		//THEN
		assertNotNull(orderPersisted.getTasks().iterator().next().getIdentifier());
		assertEquals(2, orderPersisted.getTasks().iterator().next().getClassifiers().size());
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void delete_Task_From_Classifier() {
		//GIVEN as the Task is the owning side, we add Classifiers into the Task, not wise versa
		Classifier classifier1 = new Classifier("Classifier delTask 1", "", true, BigDecimal.TEN);
		classifiersService.persistEntity(classifier1);
		
		Classifier classifier2 = new Classifier("Classifier delTask 2", "", true, BigDecimal.TEN);
		classifiersService.persistEntity(classifier2);
		
		Order order = new Order();
		order.setDescription("Order delTask 1");
		
		Task task = new Task();
		task.setName("Task delTask 1");
		
		ordersService.persistEntity(order);
		
		task.setOrder(order);
		tasksService.persistEntity(task);
		
		order.setTasks(new HashSet<>(Collections.singletonList(task)));
		
		task.addClassifier(classifier1, classifier2);
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
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void set_Department_To_Position_Will_Be_Updated_For_Both_of_Them() {
		//GIVEN
		Department department = new Department("Department depToPos 1");
		
		departmentsService.persistEntity(department);
		//Only Position is updated with the Department set
		Position position = new Position();
		position.setName("Position depToPos 1");
		position.setDepartment(department);
		positionsService.persistEntity(position);
		
		//WHEN
		Department persistedDepartment = departmentsService.findById(department.getIdentifier());
		
		//THEN
		assertEquals(1, persistedDepartment.getPositions().size());
		assertEquals("Position depToPos 1", persistedDepartment.getPositions().iterator().next().getName());
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void update_Existed_Position_With_Another_Department_Will_Update_All_Of_Them() {
		//GIVEN position1 and position2 are set to department1
		Department department1 = new Department("Department update 1");
		Department department2 = new Department("Department update 2");
		departmentsService.persistEntities(department1, department2);
		
		Position position1 = new Position();
		position1.setName("Position update 1");
		position1.setDepartment(department1);
		
		Position position2 = new Position();
		position2.setName("Position update 2");
		position2.setDepartment(department1);
		
		positionsService.persistEntities(position1, position2);
		
		//WHEN position2 is set to department2
		position2.setDescription("Description update 2");
		positionsService.updatePositionDepartment(position2, department2.getIdentifier());
		
		//THEN
		Department department1Persisted = departmentsService.findById(department1.getIdentifier());
		Department department2Persisted = departmentsService.findById(department2.getIdentifier());
		
		assertEquals(1, department1Persisted.getPositions().size());
		assertEquals("Position update 1", department1Persisted.getPositions().iterator().next().getName());
		assertEquals(1, department2Persisted.getPositions().size());
		assertEquals("Position update 2", department2Persisted.getPositions().iterator().next().getName());
		assertEquals("Description update 2", department2Persisted.getPositions().iterator().next().getDescription());
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void add_New_Phone_To_Employee_Should_Return_Both_Updated() {
		//GIVEN
		Department department1 = new Department("Department newPhone 1");
		departmentsService.persistEntity(department1);
		
		Position position1 = new Position();
		position1.setName("Position newPhone 1");
		position1.setDepartment(department1);
		positionsService.persistEntity(position1);
		
		Employee employee = new Employee("FName", "LName", "12345", "email1@pro.pro",
			LocalDate.now().minusYears(28), position1);
		employeesService.persistEntity(employee);
		
		Phone phone1 = new Phone("Phone newPhone1", "111-111-12-115");
		phone1.setEmployee(employee);
		phonesService.persistEntity(phone1);
		
		//WHEN
		Phone phone2 = new Phone("Phone newPhone2", "111-111-12-225");
		phonesService.addPhoneToEmployee(employee.getIdentifier(), phone2);
		
		//THEN
		Phone phone2WithEmployee = phonesService.findById(phone2.getIdentifier());
		Employee employeeWith2Phones = employeesService.findById(employee.getIdentifier());
		
		assertTrue(employeeWith2Phones.getPhones().containsAll(Arrays.asList(phone1, phone2)));
		
		assertEquals(phone2WithEmployee.getEmployee(), employeeWith2Phones);
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void add_Existing_Phone_To_Employee_Should_Return_Both_Updated() {
		//GIVEN
		Department department1 = new Department("Department existPhone 1");
		departmentsService.persistEntity(department1);
		
		Position position1 = new Position();
		position1.setName("Position existPhone 1");
		position1.setDepartment(department1);
		positionsService.persistEntity(position1);
		
		Employee employee1 = new Employee("FName", "LName", "12345", "email11@pro1.pro",
			LocalDate.now().minusYears(28), position1);
		
		Employee employee2 = new Employee("FName", "LName", "12345", "email12@pro2.pro",
			LocalDate.now().minusYears(28), position1);
		
		Phone phone1 = new Phone("Phone existPhone1", "111-111-11-1133");
		Phone phone2 = new Phone("Phone existPhone2", "111-111-11-2233");
		
		employee1.addPhone(phone1, phone2);
		
		employeesService.persistEntities(employee1, employee2);
		
		//WHEN
		Employee employee1Persisted = employeesService.findById(employee1.getIdentifier());
		
		assertEquals(2, employee1Persisted.getPhones().size());
		assertTrue(employee1Persisted.getPhones().containsAll(Arrays.asList(phone1, phone2)));
		
		//WHEN
		Phone phone2Persisted = phonesService.findById(phone2.getIdentifier());
		phone2Persisted.setName("Phone existPhone22");
		Phone phone2Merged = phonesService.mergeEntity(phone2Persisted);
		
		phonesService.addPhoneToEmployee(employee2.getIdentifier(), phone2Persisted.getIdentifier());
		
		//THEN
		Employee employee1ToAssert = employeesService.findById(employee1.getIdentifier());
		Employee employee2ToAssert = employeesService.findById(employee2.getIdentifier());
		
		assertEquals(1, employee1ToAssert.getPhones().size());
		assertTrue(employee1ToAssert.getPhones().contains(phone1));
		
		assertEquals(1, employee2ToAssert.getPhones().size());
		assertTrue(employee2ToAssert.getPhones().contains(phone2Merged));
		
		assertEquals("Phone existPhone1", employee1ToAssert.getPhones().iterator().next().getName());
		assertEquals("Phone existPhone22", employee2ToAssert.getPhones().iterator().next().getName());
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void delete_Existing_Phone_From_Existing_Employee() {
		//GIVEN
		Department department1 = new Department("Department 1");
		departmentsService.persistEntity(department1);
		
		Position position1 = new Position();
		position1.setName("Position delPhone 1");
		position1.setDepartment(department1);
		positionsService.persistEntity(position1);
		
		Employee employee1 = new Employee("FName", "LName", "12345", "email@pro1.pro",
			LocalDate.now().minusYears(28), position1);
		
		Phone phone1 = new Phone("Phone delPhone1", "144-111-11-11");
		Phone phone2 = new Phone("Phone delPhone2", "144-111-11-22");
		
		employee1.addPhone(phone1, phone2);
		
		employeesService.persistEntities(employee1);
		
		//WHEN
		phonesService.removePhoneFromEmployee(employee1.getIdentifier(), phone1.getIdentifier());
		
		Employee employeeWithSinglePhone2 = employeesService.findById(employee1.getIdentifier());
		
		//THEN
		assertEquals(1, employeeWithSinglePhone2.getPhones().size());
		assertTrue(employeeWithSinglePhone2.getPhones().contains(phone2));
		//TODO: Phone deleting doesn't work! It has to be examined
//		assertThrows(EntityNotFoundException.class, () -> phonesService.findById(phone1.getIdentifier()));
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void add_New_Position_To_Existing_Employee() {
		//GIVEN
		Department department1 = new Department("Department newPosn 1");
		departmentsService.persistEntity(department1);
		
		Position position1 = new Position();
		position1.setName("Position newPosn 1");
		position1.setDepartment(department1);
		positionsService.persistEntity(position1);
		
		Employee employee1 = new Employee("FName", "LName", "12345", "email@pro21.pro",
			LocalDate.now().minusYears(28), position1);
		
		employeesService.persistEntities(employee1);
		
		//WHEN
		Position position2 = new Position();
		position2.setName("Position newPosn 2");
		position2.setDepartment(department1);
		
		positionsService.addPositionToEmployee(employee1.getIdentifier(), position2);
		
		//THEN
		Employee updatedEmployee = employeesService.findById(employee1.getIdentifier());
		List<Employee> employeesByPosition =
			employeesService.findEmployeesByPosition(
				PageRequest.of(0, 10, Sort.Direction.DESC, "created"), position2.getIdentifier())
				.getContent();
		Position updatedPosition = positionsService.findById(position2.getIdentifier());
		
		assertEquals(updatedPosition, updatedEmployee.getPosition());
		assertEquals(1, employeesByPosition.size());
		assertTrue(employeesByPosition.contains(updatedEmployee));
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void add_Existing_Position_To_Existing_Employee() {
		//GIVEN
		Department department1 = new Department("Department existEmp 1");
		departmentsService.persistEntity(department1);
		
		Position position1 = new Position();
		position1.setName("Position existEmp 1");
		position1.setDepartment(department1);
		
		Position position2 = new Position();
		position2.setName("Position existEmp 2");
		position2.setDepartment(department1);
		
		positionsService.persistEntities(position1, position2);
		
		Phone phone1 = new Phone("Phone existEmp1", "188-111-11-11");
		Phone phone2 = new Phone("Phone existEmp2", "188-111-11-22");
		
		Employee employee1 = new Employee("Employee 1", "LName", "12345", "email34@pro1.pro",
			LocalDate.now().minusYears(28), position1);
		employee1.setPhones(new HashSet<>(Collections.singletonList(phone1)));
		
		Employee employee2 = new Employee("Employee 2", "LName", "12345", "email35@pro2.pro",
			LocalDate.now().minusYears(28), position2);
		employee2.setPhones(new HashSet<>(Collections.singletonList(phone2)));
		
		employeesService.persistEntities(employee1, employee2);
		
		position1.setEmployees(new HashSet<>(Collections.singletonList(employee1)));
		positionsService.mergeEntity(position1);
		
		//WHEN
		positionsService.addPositionToEmployee(employee1.getIdentifier(), position2);
		
		//THEN
		Employee updatedEmployee1 = employeesService.findById(employee1.getIdentifier());
		
		List<Employee> employeesByPosition2 =
			employeesService.findEmployeesByPosition(
				PageRequest.of(0, 10, Sort.Direction.DESC, "created"), position2.getIdentifier())
				.getContent();
		Position updatedPosition2 = positionsService.findById(position2.getIdentifier());
		
		assertEquals(updatedPosition2, updatedEmployee1.getPosition());
		assertEquals(2, employeesByPosition2.size());
		assertTrue(employeesByPosition2.contains(updatedEmployee1));
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void delete_AppointedTask_From_Existing_Employee() {
		//GIVEN
		Department department1 = new Department("Department delAppTask 1");
		departmentsService.persistEntity(department1);
		
		Position position1 = new Position();
		position1.setName("Position delAppTask 1");
		position1.setDepartment(department1);
		positionsService.persistEntity(position1);
		
		Employee employee1 = new Employee("Employee 1", "LName", "12345", "email87@pro1.pro",
			LocalDate.now().minusYears(28), position1);
		
		employeesService.persistEntities(employee1);
		
		Order order = new Order();
		
		Task task1 = new Task();
		task1.setName("Task delAppTask 1");
		task1.setOrder(order);
		task1.setAppointedTo(employee1);
		
		ordersService.persistEntity(order);
		tasksService.persistOrMergeEntity(task1);
		
		//WHEN check
		Page<Task> tasksAppointedToEmployee = tasksService.findTasksAppointedToEmployee(
			PageRequest.of(0, 10, Sort.Direction.DESC, "created"),
			employee1.getIdentifier());
		
		assertTrue(tasksAppointedToEmployee.getContent().contains(task1));
		
		//WHEN
		Task taskToBeDeletedFromEmployee = tasksService.findById(task1.getIdentifier());
		taskToBeDeletedFromEmployee.setAppointedTo(null);
		tasksService.mergeEntity(taskToBeDeletedFromEmployee);
		
		//THEN
		Task taskWithoutAppointedTo = tasksService.findById(task1.getIdentifier());
		
		assertThrows(EntityNotFoundException.class, () ->
			tasksService.findTasksAppointedToEmployee(
				PageRequest.of(0, 10, Sort.Direction.DESC, "created"),
				employee1.getIdentifier()));
		
		assertNull(taskWithoutAppointedTo.getAppointedTo());
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void post_New_TaskCreatedBy_To_Existing_Employee() {
		//GIVEN
		Department department1 = new Department("Department newTaskCreated 1");
		departmentsService.persistEntity(department1);
		
		Position position1 = new Position();
		position1.setName("Position newTaskCreated 1");
		position1.setDepartment(department1);
		positionsService.persistEntity(position1);
		
		Employee employee1 = new Employee("Employee 1", "LName", "12345", "email98@pro1.pro",
			LocalDate.now().minusYears(28), position1);
		employeesService.persistEntities(employee1);
		
		Order order = new Order();
		ordersService.persistEntity(order);
		
		//WHEN
		Employee persistedEmployee = employeesService.findById(employee1.getIdentifier());
		Task task1 = new Task();
		task1.setName("Task newTaskCreated 1");
		task1.setOrder(order);
		task1.setCreatedBy(persistedEmployee);
		tasksService.persistEntity(task1);
		
		//THEN
		Task taskWithCreatedBy = tasksService.findById(task1.getIdentifier());
		Page<Task> tasksCreatedByEmployee = tasksService.findTasksCreatedByEmployee(
			PageRequest.of(0, 10, Sort.Direction.DESC, "created"),
			persistedEmployee.getIdentifier());
		
		assertEquals(persistedEmployee, taskWithCreatedBy.getCreatedBy());
		assertTrue(tasksCreatedByEmployee.getContent().contains(task1));
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void post_New_Phone_To_Existing_User_And_Getting_User_Again_Should_Return_User_With_Phone() {
		//GIVEN
		User user = new User("user@email.com");
		user = usersService.persistEntity(user);
		
		//WHEN
		Phone phone = new Phone("Home", "111-111-11-11");
		phone.setUser(user);
		phone = phonesService.persistEntity(phone);
		
		user = usersService.findById(user.getIdentifier());
		
		//THEN
		assertTrue(user.getPhones().contains(phone));
	}
	
	@ParameterizedTest
	@ValueSource(strings = {"Department", "Position", "Employee"})
	public void workshopEntityTypes_as_String_Should_Return_Corresponding_WorkshopEntitiesService(String workshopEntityType) {
		//GIVEN
		
		//WHEN
		WorkshopEntitiesServiceAbstract workshopEntitiesService =
			WorkshopEntitiesServiceAbstract.getWorkshopEntitiesServiceBeanByEntityType(workshopEntityType);
		
		//THEN
		assertEquals(workshopEntityType, workshopEntitiesService.getEntityClass().getSimpleName());
	}
	
	@ParameterizedTest
	@ValueSource(strings = {"Department", "Position", "Employee"})
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	@Transactional
	@Rollback
	public void workshopEntitiesServices_From_WorkshopEntitiesTypes_Strings_Should_Return_Corresponding_WorkshopEntities(
		String workshopEntityType) {
		
		//GIVEN
		Department department1 = new Department("Department newType");
		departmentsService.persistEntity(department1);
		
		Position position1 = new Position();
		position1.setName("Position newType");
		position1.setDepartment(department1);
		positionsService.persistEntity(position1);
		
		Employee employee1 = new Employee("Employee newType", "LName", "12345", "emailNewType@pro1.pro",
			LocalDate.now().minusYears(28), position1);
		employeesService.persistEntities(employee1);
		
		//WHEN
		WorkshopEntitiesServiceAbstract workshopEntitiesService =
			WorkshopEntitiesServiceAbstract.getWorkshopEntitiesServiceBeanByEntityType(workshopEntityType);
		
		//THEN
		List<WorkshopEntity> workshopEntityByProperty = null;
		
		if (workshopEntityType.equals("Department")) {
			workshopEntityByProperty =
				workshopEntitiesService.findByProperty("name", "Department newType");
			assertEquals(1, workshopEntityByProperty.size());
			assertEquals(department1.getIdentifier(), workshopEntityByProperty.get(0).getIdentifier());
		} else if (workshopEntityType.equals("Position")) {
			workshopEntityByProperty =
				workshopEntitiesService.findByProperty("name", "Position newType");
			assertEquals(1, workshopEntityByProperty.size());
			assertEquals(position1.getIdentifier(), workshopEntityByProperty.get(0).getIdentifier());
		} else if (workshopEntityType.equals("Employee")) {
			workshopEntityByProperty =
				workshopEntitiesService.findByProperty("firstName", "Employee newType");
			assertEquals(1, workshopEntityByProperty.size());
			assertEquals(employee1.getIdentifier(), workshopEntityByProperty.get(0).getIdentifier());
		}
	}
	
}