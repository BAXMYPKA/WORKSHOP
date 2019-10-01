package workshop.internal.dao;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import workshop.internal.entities.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Overall DaoAbstract tests with EntityManager for performing some common operations for all the DAOs implementations
 * within existing ApplicationContext.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@PropertySource("classpath:applicationTest.properties")
@AutoConfigureTestEntityManager
@DirtiesContext
@EnableTransactionManagement
@Transactional
@Sql(scripts = {"classpath:testImport.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Slf4j
class WorkshopEntitiesDaoAbstractIT {
	
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
	@Autowired
	InternalAuthoritiesDao internalAuthoritiesDao;
	@PersistenceContext
	EntityManager entityManager;
	@Autowired
	EntityManagerFactory emf; //To support transactions with emf.getEntityManager();
	
	@Test
	public void context_Should_Be_Initialized() {
		assertNotNull(ordersDao);
		assertNotNull(ordersDao.getEntityManager());
		assertNotNull(entityManager);
	}
	
	@Test
	@Transactional
	@DisplayName("The persisting simple entities without identifier (identifier=0) one by one should be successful")
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void persist_Simple_Entities_One_By_One_Should_Be_Successful() {
		//GIVEN
		//New entities
		Department department1 = new Department("Department one");
		Department department2 = new Department("Department two");
		Classifier classifier1 = Classifier.builder().name("Classifier one").description("").isOfficial(true)
			.price(BigDecimal.valueOf(20.20)).build();
		Classifier classifier2 = Classifier.builder().name("Classifier two").description("").isOfficial(true)
			.price(BigDecimal.valueOf(30.35)).build();
		
		//WHEN Persist entities one by one
		Optional<Department> departmentOne = departmentsDao.persistEntity(department1);
		Optional<Department> departmentTwo = departmentsDao.persistEntity(department2);
		Optional<Classifier> classifierOne = classifiersDao.persistEntity(classifier1);
		Optional<Classifier> classifierTwo = classifiersDao.persistEntity(classifier2);
		
		//THEN all of them got their ids
		assertAll(
			() -> assertTrue(departmentOne.get().getIdentifier() > 0),
			() -> assertTrue(departmentTwo.get().getIdentifier() > 0),
			() -> assertTrue(classifierOne.get().getIdentifier() > 0),
			() -> assertTrue(classifierTwo.get().getIdentifier() > 0)
		);
	}
	
	@Test
	@Transactional
	@DisplayName("Entities should be removed one by one.")
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void remove_Entities_One_By_One() {
		//GIVEN
		//New entities
		Department department1 = new Department("Department one");
		Department department2 = new Department("Department two");
		Position position1 = new Position("Position one", department1);
		Position position2 = new Position("Position two", department2);
		//Persisted entities
		Optional<Department> departmentOne = departmentsDao.persistEntity(department1);
		Optional<Department> departmentTwo = departmentsDao.persistEntity(department2);
		Optional<Position> positionOne = positionsDao.persistEntity(position1);
		Optional<Position> positionTwo = positionsDao.persistEntity(position2);
		//Is persistence successful (just a check)
		assertAll(
			() -> assertTrue(departmentOne.get().getIdentifier() > 0),
			() -> assertTrue(departmentTwo.get().getIdentifier() > 0),
			() -> assertTrue(positionOne.get().getIdentifier() > 0),
			() -> assertTrue(positionTwo.get().getIdentifier() > 0)
		);
		
		//WHEN
		//Remove one by one
		positionsDao.removeEntity(positionOne.get());
		positionsDao.removeEntity(positionTwo.get());
		departmentsDao.removeEntity(departmentOne.get());
		//Try to get the removed entities
		Optional<Position> positionOneById = positionsDao.findById(departmentOne.get().getIdentifier());
		Optional<Position> positionTwoById = positionsDao.findById(departmentOne.get().getIdentifier());
		Optional<Position> departmentOneId = positionsDao.findById(departmentOne.get().getIdentifier());
		
		//THEN
		assertAll(
			() -> assertFalse(positionOneById.isPresent()),
			() -> assertFalse(positionTwoById.isPresent()),
			() -> assertFalse(departmentOneId.isPresent())
		);
	}
	
	@Test
	@org.junit.jupiter.api.Order(4)
	@Transactional
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void batch_remove_Entities_By_Collection() {
		//GIVEN
		Department department1 = new Department("Delete me 1");
		Department department2 = new Department("Delete me 2");
		Department department3 = new Department("Delete me 3");
		//Check the persistence
		Collection<Department> departments =
			departmentsDao.persistEntities(Arrays.asList(department1, department2, department3)).get();
		departments.forEach(department -> assertNotNull(department.getIdentifier()));
		
		//WHEN
		departmentsDao.removeEntities(departments);
		//THEN No entities of that kind should be found
		departments.forEach(department -> assertFalse(departmentsDao.isExist(department.getIdentifier())));
	}
	
	@Test
	@DisplayName("New entity with including new entities graph should all be persisted")
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void cascade_Persisting_New_Order_And_Its_Tasks_Should_Persist_All() {
		//GIVEN entities with CascadeType.PERSIST allowed
		Order order1 = new Order();
		Task task1 = Task.builder().name("Task one 1").order(order1).build();
		System.out.println(task1.getPrice());
		order1.setTasks(new HashSet<>(Collections.singleton(task1)));
		
		//WHEN
		Order orderPersisted = ordersDao.persistEntity(order1).get();
		
		//THEN
		assertAll(
			() -> assertNotNull(orderPersisted.getIdentifier()),
			() -> assertNotNull(orderPersisted.getTasks().iterator().next().getIdentifier())
		);
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void find_Entities_By_Email_Should_Return_Entities_With_Emails() {
		//GIVEN
		Department department = new Department("Department enByEmail");
		departmentsDao.persistEntity(department);
		
		Position position = new Position("Position  enByEmail", department);
		positionsDao.persistEntity(position);
		
		String employeeEmail = "employeeToBeFoune@workshop.pro";
		String userEmail = "userToBeFound@user.com";
		//Entities to be persisted
		Employee employee = new Employee("fn", "ln", "12345", employeeEmail,
			LocalDate.now().minusYears(50), position);
		employeesDao.persistEntity(employee);
		
		User user = new User(userEmail);
		usersDao.persistEntity(user);
		
		//WHEN
		Employee employeeByEmail = employeesDao.findEmployeeByEmail(employeeEmail).get();
		Optional<User> userByEmail = usersDao.findByEmail(userEmail);
		
		//THEN
		assertAll(
			() -> assertEquals(employeeEmail, employeeByEmail.getEmail()),
			() -> assertTrue(userByEmail.isPresent()),
			() -> assertEquals(userEmail, userByEmail.get().getEmail())
		);
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	@DisplayName("JPA should persist and return ZonedDateTime in UTC despite a set one.")
	public void zonedDateTime_Should_Be_Saved_And_Returned_In_UTC_TimeZone() {
		//GIVEN an Employee with Europe/Moscow (+3) TimeZone
		ZonedDateTime europeMoscowZone = ZonedDateTime.of(
			2019, 1, 30, 12, 30, 0, 0, ZoneId.of("Europe/Moscow"));
		ZonedDateTime utcZone = europeMoscowZone.withZoneSameInstant(ZoneId.of("UTC"));
		
		Department department = new Department("Department zdt one");
		departmentsDao.persistEntity(department);
		Position position = new Position("Position one", department);
		positionsDao.persistEntity(position);
		
		Employee employee = new Employee(
			"FN", "LN", "12345", "emp@test.pro", LocalDate.now().minusYears(33), position);
		employee.setFinished(europeMoscowZone);
		employeesDao.persistEntity(employee);
		
		//WHEN persist and get the Entity back
		Optional<Employee> employeePersisted = employeesDao.persistEntity(employee);
		
		//THEN receive the Entity with UTC-corrected ZonedDateTime
		//Just a check that UTC zone 3 hours less
		assertEquals(utcZone.getHour(), europeMoscowZone.minusHours(3).getHour());
		//Persisted Entity now has the UTC-corrected field
		assertTrue(employeePersisted.get().getFinished().isEqual(utcZone));
	}
	
	@Test
	@DisplayName("CreatedBy should by automatically set from the SecurityContext")
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void createdBy_Property_Should_Be_Automatically_Persisted_From_SecurityContext() {
		//GIVEN
		//Pre persist entities.
		Department department = new Department("Department for createdBy test");
		Position position = new Position("Position for createdBy test", department);
		Employee employeeToBeCreatedBy = new Employee("Admin", "ln", "54321",
			"admin@workshop.pro", LocalDate.now().minusYears(50), position);
		Employee employeeNotToBeCreatedBy = new Employee("Employee", "ln", "12345",
			"employee@workshop.pro", LocalDate.now().minusYears(55), position);
		
		departmentsDao.persistEntity(department);
		positionsDao.persistEntity(position);
		employeesDao.persistEntities(new ArrayList<Employee>(Arrays.asList(employeeToBeCreatedBy, employeeNotToBeCreatedBy)));
		//This Employee has to be automatically got from SecurityContext
		SecurityContextHolder.getContext().setAuthentication(
			new UsernamePasswordAuthenticationToken(employeeToBeCreatedBy, "",
				new ArrayList<>(Collections.singletonList(new SimpleGrantedAuthority("Admin")))));
		
		//The given Classifier without createdBy
		Classifier classifier = new Classifier("Classifier for createdBy test", "Descr", true, BigDecimal.TEN);
		
		//WHEN
		Optional<Classifier> classifierPersisted = classifiersDao.persistEntity(classifier);
		
		//THEN
		assertEquals(employeeToBeCreatedBy, classifierPersisted.get().getCreatedBy());
	}
	
	@ParameterizedTest
	@ValueSource(strings = {"property", "anotherProperty"})
	@DisplayName("FindByProperty should throw IllegalArgExc with exact message if Entity doesn't have such a property")
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void findByProperty_Should_Throw_IllegalArgsEx_If_Entity_Doesnt_Have_Such_Property(String propertyName) {
		//GIVEN
		
		//WHEN
		IllegalArgumentException exceptionMessage = assertThrows(IllegalArgumentException.class,
			() -> departmentsDao.findByProperty(propertyName, "value"));
		
		assertEquals(
			Department.class.getSimpleName() + " doesn't have such a '" + propertyName + "' property!",
			exceptionMessage.getMessage());
		
	}
	
	@ParameterizedTest
	@CsvSource({"overallPrice, 10.11", "overallPrice, 10.21", "description, OrderByProp1", "description, OrderByProp2"})
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void findByProperty_Should_Return_Proper_String_Result(String propertyName, String propertyValue) {
		//GIVEN
		Order order1 = new Order();
		order1.setDescription("OrderByProp1");
		order1.setOverallPrice(BigDecimal.valueOf(10.11));
		
		Order order2 = new Order();
		order2.setDescription("OrderByProp2");
		order2.setOverallPrice(BigDecimal.valueOf(10.21));
		
		ordersDao.persistEntities(Arrays.asList(order1, order2));
		
		//WHEN
		Optional<List<Order>> orderByProperty = ordersDao.findByProperty(propertyName, propertyValue);
		
		//THEN
		assertTrue(orderByProperty.isPresent() && orderByProperty.get().size() == 1);
		
		if ("overallPrice".equals(propertyName)) {
			assertEquals(new BigDecimal(propertyValue), orderByProperty.get().get(0).getOverallPrice());
		} else if ("description".equals(propertyName)) {
			assertEquals(propertyValue, orderByProperty.get().get(0).getDescription());
		} else if ("deadline".equals(propertyName)) {
			System.out.println(orderByProperty.get().get(0).getDeadline());
		}
	}
	
	@ParameterizedTest
	@CsvSource({"created, 2017-11-20T09:35:45+03:00", "deadline, 2020-12-30T12:00:00+00:00"})
	@DisplayName("FindByProperty should parse Temporal and return more than one result if they are")
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void findByProperty_Should_Return_Proper_ZonedDateTime_Results(String propertyName, String propertyValue) {
		//GIVEN
		ZonedDateTime expectedCreatedInOrder1 = ZonedDateTime.of(
			2017, 11, 20, 9, 35, 45, 0, ZoneId.systemDefault());
		
		ZonedDateTime expectedDeadlineInOrder2 = ZonedDateTime.of(
			2020, 12, 30, 12, 0, 0, 0, ZoneId.of("UTC"));
		
		Order order1 = new Order();
		order1.setDescription("OrderByDateTime1");
		order1.setOverallPrice(BigDecimal.valueOf(10.11));
		order1.setCreated(expectedCreatedInOrder1);
		
		Order order2 = new Order();
		order2.setDescription("OrderByDateTime2");
		order2.setOverallPrice(BigDecimal.valueOf(10.21));
		order2.setDeadline(expectedDeadlineInOrder2);
		
		ordersDao.persistEntities(Arrays.asList(order1, order2));
		
		//WHEN
		Optional<List<Order>> orderByProperty = ordersDao.findByProperty(propertyName, propertyValue);
		
		//THEN
		assertTrue(orderByProperty.isPresent());
		
		if ("created".equals(propertyName)) {
			assertEquals(expectedCreatedInOrder1, orderByProperty.get().get(0).getCreated());
		} else if ("deadline".equals(propertyName)) {
			assertEquals(expectedDeadlineInOrder2, orderByProperty.get().get(0).getDeadline());
		}
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	@Transactional
	public void positions_By_DepartmentId_Should_Be_Returned() {
		//GIVEN
		Department department1 = new Department("Department id 1");
		departmentsDao.persistEntity(department1).get();
		Long departmentId = department1.getIdentifier();
		
		Position position1 = new Position("Position id 1", department1);
		Position position2 = new Position("Position id 2", department1);
		Position position3 = new Position("Position id 3", department1);
		Position position4 = new Position("Position id 4", department1);
		Position position5 = new Position("Position id 5", department1);
		Position position6 = new Position("Position id 6", department1);
		Position position7 = new Position("Position id 7", department1);
		Position position8 = new Position("Position id 8", department1);
		Position position9 = new Position("Position id 9", department1);
		
		positionsDao.persistEntities(Arrays.asList(position1, position2, position3, position4, position5, position6,
			position7, position8, position9));
		
		//WHEN
		Optional<List<Position>> allPositionsByDepartment =
			positionsDao.
				findPositionsByDepartment(20, 0, "created", Sort.Direction.DESC, departmentId);
		
		//THEN
		assertEquals(9, allPositionsByDepartment.get().size());
		assertTrue(allPositionsByDepartment.get().stream().anyMatch(position -> "Position id 1".equals(position.getName())));
		assertTrue(allPositionsByDepartment.get().stream().anyMatch(position -> "Position id 5".equals(position.getName())));
		assertTrue(allPositionsByDepartment.get().stream().anyMatch(position -> "Position id 9".equals(position.getName())));
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	@Transactional
	public void positions_By_DepartmentId_Should_Be_Returned_OrderedBy_Name_Ascending() {
		//GIVEN
		Department department1 = new Department("Department is 1");
		departmentsDao.persistEntity(department1).get();
		Long departmentId = department1.getIdentifier();
		
		Position position1 = new Position("Position is 1", department1);
		Position position2 = new Position("Position is 2", department1);
		Position position3 = new Position("Position is 3", department1);
		Position position4 = new Position("Position is 4", department1);
		Position position5 = new Position("Position is 5", department1);
		Position position6 = new Position("Position is 6", department1);
		Position position7 = new Position("Position is 7", department1);
		Position position8 = new Position("Position is 8", department1);
		Position position9 = new Position("Position is 9", department1);
		
		positionsDao.persistEntities(Arrays.asList(position1, position2, position3, position4, position5, position6,
			position7, position8, position9));
		
		//WHEN
		List<Position> allPositionsByDepartmentOrderedByNameAsc =
			positionsDao.findPositionsByDepartment(20, 0, "name", Sort.Direction.ASC, departmentId).get();
		
		//THEN
		assertEquals(9, allPositionsByDepartmentOrderedByNameAsc.size());
		assertEquals("Position is 1", allPositionsByDepartmentOrderedByNameAsc.get(0).getName());
		assertEquals("Position is 9", allPositionsByDepartmentOrderedByNameAsc.get(8).getName());
	}
	
	@ParameterizedTest
	@ValueSource(ints = {0, 2})
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	@Transactional
	public void positions_By_DepartmentId_Should_Return_First_And_Last_Pages_OrderedBy_Name_Descending(int pageNum) {
		//GIVEN
		Department department1 = new Department("Department forPos 1");
		departmentsDao.persistEntity(department1);
		
		Position position1 = new Position("Position byDep 1", department1);
		Position position2 = new Position("Position byDep 2", department1);
		Position position3 = new Position("Position byDep 3", department1);
		Position position4 = new Position("Position byDep 4", department1);
		Position position5 = new Position("Position byDep 5", department1);
		Position position6 = new Position("Position byDep 6", department1);
		Position position7 = new Position("Position byDep 7", department1);
		Position position8 = new Position("Position byDep 8", department1);
		Position position9 = new Position("Position byDep 9", department1);
		positionsDao.persistEntities(Arrays.asList(position1, position2, position3, position4, position5, position6,
			position7, position8, position9));
		
		//WHEN
		List<Position> positionsByDepartmentOrderedByNameDesc = positionsDao.findPositionsByDepartment(
			3, pageNum, "name", Sort.Direction.DESC, department1.getIdentifier()).get();
		
		//THEN
		assertEquals(3, positionsByDepartmentOrderedByNameDesc.size());
		
		if (pageNum == 0) {
			assertEquals("Position byDep 9", positionsByDepartmentOrderedByNameDesc.get(0).getName());
			assertEquals("Position byDep 7", positionsByDepartmentOrderedByNameDesc.get(2).getName());
		} else if (pageNum == 2) {
			assertEquals("Position byDep 3", positionsByDepartmentOrderedByNameDesc.get(0).getName());
			assertEquals("Position byDep 1", positionsByDepartmentOrderedByNameDesc.get(2).getName());
		}
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	@Transactional
	public void department_By_PositionId_Should_Return_() {
		//GIVEN
		Department department1 = new Department("Department byPosId 1");
		departmentsDao.persistEntity(department1);
		
		Position position1 = new Position("Position byPosId 1", department1);
		Position position2 = new Position("Position byPosId 2", department1);
		positionsDao.persistEntities(Arrays.asList(position1, position2));
		
		//WHEN
		Department departmentByPosition1 = departmentsDao.findDepartmentByPosition(position1.getIdentifier()).get();
		Department departmentByPosition2 = departmentsDao.findDepartmentByPosition(position2.getIdentifier()).get();
		
		//THEN
		assertEquals(department1.getIdentifier(), departmentByPosition1.getIdentifier());
		assertEquals(department1.getIdentifier(), departmentByPosition2.getIdentifier());
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	@Transactional
	public void tasks_By_OrderId_Should_Return_All_Included_Tasks() {
		//GIVEN
		Order order1 = new Order();
		order1.setDescription("Order is 1");
		
		Task task1 = new Task();
		task1.setName("Task is 1");
		
		Task task2 = new Task();
		task2.setName("Task is 2");
		
		Order orderPersisted = ordersDao.persistEntity(order1).get();
		long orderPersistedId = orderPersisted.getIdentifier();
		
		task1.setOrder(orderPersisted);
		task2.setOrder(orderPersisted);
		
		Task task1Persisted = tasksDao.persistEntity(task1).get();
		Task task2Persisted = tasksDao.persistEntity(task2).get();
		
		//WHEN
		List<Task> tasksByOrder =
			tasksDao.findAllTasksByOrder(20, 0, "name", Sort.Direction.DESC, orderPersistedId).get();
		
		//THEN
		assertEquals(2, tasksByOrder.size());
		assertTrue(tasksByOrder.contains(task1Persisted));
		assertTrue(tasksByOrder.contains(task2Persisted));
		
		ordersDao.removeEntity(order1);
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	@Transactional
	public void classifier_Tasks_Should_Be_Returned_As_ManyToMany() {
		//GIVEN
		Order order1 = new Order();
		
		Classifier classifier1 = new Classifier("Classifier byTask 1", "", true, BigDecimal.ONE);
		Classifier classifier2 = new Classifier("Classifier byTask 2", "", true, BigDecimal.TEN);
		
		classifiersDao.persistEntities(Arrays.asList(classifier1, classifier2));
		
		Task task1 = Task.builder().name("Task byTask 1").build();
		task1.setClassifiers(new HashSet<>(Arrays.asList(classifier1, classifier2)));
		task1.setOrder(order1);
		
		Task task2 = Task.builder().name("Task byTask 2").build();
		task2.setClassifiers(new HashSet<>(Arrays.asList(classifier1, classifier2)));
		task2.setOrder(order1);
		
		Task task3 = Task.builder().name("Task byTask 3").build();
		task3.setClassifiers(new HashSet<>(Arrays.asList(classifier1, classifier2)));
		task3.setOrder(order1);
		
		order1.setTasks(new HashSet<>(Arrays.asList(task1, task2, task3)));
		
		ordersDao.persistEntity(order1);
		
		//WHEN
		Optional<List<Task>> tasksByClassifier = tasksDao.findAllTasksByClassifier(
			10,
			0,
			"created",
			Sort.Direction.DESC,
			classifier1.getIdentifier());
		
		//THEN
		assertTrue(tasksByClassifier.isPresent());
		assertEquals(3, tasksByClassifier.get().size());
		assertAll(
			() -> assertTrue(tasksByClassifier.get().contains(task1)),
			() -> assertTrue(tasksByClassifier.get().contains(task2)),
			() -> assertTrue(tasksByClassifier.get().contains(task3))
		);
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void positions_By_Authority_Should_Return_All_Linked_Positions() {
		//GIVEN
		Department department = new Department("Department forPosAuth 1");
		departmentsDao.persistEntity(department);
		
		Position position1 = new Position("Position forPosAuth 1", department);
		Position position2 = new Position("Position forPosAuth 2", department);
		Position position3 = new Position("Position forPosAuth 3", department);
		positionsDao.persistEntities(Arrays.asList(position1, position2, position3));
		
		InternalAuthority authority1 = new InternalAuthority("Authority forPosAuth 1");
		InternalAuthority authority2 = new InternalAuthority("Authority forPosAuth 2");
		authority1.setPositions(new HashSet<>(Arrays.asList(position1, position2)));
		authority2.setPositions(new HashSet<>(Collections.singletonList(position3)));
		internalAuthoritiesDao.persistEntities(Arrays.asList(authority1, authority2));
		
		authority1 = internalAuthoritiesDao.findById(authority1.getIdentifier()).get();
		position1 = positionsDao.findById(position1.getIdentifier()).get();
		
		//WHEN
		List<Position> positionsByAuthority1 =
			positionsDao.findPositionsByInternalAuthority(
				10, 0, "created", Sort.Direction.DESC, authority1.getIdentifier()).get();
		
		List<Position> positionsByAuthority2 =
			positionsDao.findPositionsByInternalAuthority(
				10, 0, "created", Sort.Direction.DESC, authority2.getIdentifier()).get();
		
		//THEN
		assertEquals(2, positionsByAuthority1.size());
		assertTrue(positionsByAuthority1.contains(position1));
		assertTrue(positionsByAuthority1.contains(position2));
		
		assertEquals(1, positionsByAuthority2.size());
		assertTrue(positionsByAuthority2.contains(position3));
		
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void count_All_Positions_By_Department() {
		//GIVEN
		Department department = new Department("Department unique 100");
		departmentsDao.persistEntity(department);
		
		Position position1 = new Position("Position unique 100", department);
		Position position2 = new Position("Position unique 200", department);
		Position position3 = new Position("Position unique 300", department);
		positionsDao.persistEntities(Arrays.asList(position1, position2, position3));
		
		//WHEN
		long totalPositionsByDepartment = positionsDao.countAllPositionsByDepartment(department.getIdentifier());
		long totalPositionsByUnknownDepartment = positionsDao.countAllPositionsByDepartment(100501L);
		
		//THEN
		assertEquals(3, totalPositionsByDepartment);
		assertEquals(0, totalPositionsByUnknownDepartment);
	}
	
	@Test
	@WithMockUser(username = "admin@workshop.pro", password = "12345", authorities = {"ADMIN_FULL"})
	public void count_All_Positions_By_InternalAuthority() {
		//GIVEN
		Department department = new Department("Department unique is 100");
		departmentsDao.persistEntity(department);
		
		Position position1 = new Position("Position unique is 100", department);
		Position position2 = new Position("Position unique is 200", department);
		Position position3 = new Position("Position unique is 300", department);
		positionsDao.persistEntities(Arrays.asList(position1, position2, position3));
		
		InternalAuthority authority = new InternalAuthority("Authority 1 for Positions");
		authority.setPositions(new HashSet<>(Arrays.asList(position1, position2, position3)));
		internalAuthoritiesDao.persistEntity(authority);
		
		//WHEN
		long totalPositionsByInternalAuthority = positionsDao.countAllPositionsByInternalAuthority(authority.getIdentifier());
		long totalPositionsByUnknownAuthority = positionsDao.countAllPositionsByInternalAuthority(100501L);
		
		//THEN
		assertEquals(3, totalPositionsByInternalAuthority);
		assertEquals(0, totalPositionsByUnknownAuthority);
	}
}