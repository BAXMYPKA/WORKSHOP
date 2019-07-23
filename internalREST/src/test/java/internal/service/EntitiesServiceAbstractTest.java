package internal.service;

import internal.entities.Department;
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
class EntitiesServiceAbstractTest {
	
	@Autowired
	TestEntityManager entityManager;
	@Autowired
	DepartmentsService departmentsService;
	
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
	public void departmentsService_Persists() {
		//GIVEN
		Department departmentToPersist = new Department("The Department to be stored");
		
		//WHEN
		Optional<Department> departmentPersisted = departmentsService.persistOrMergeEntity(departmentToPersist);
		
		//THEN
		assertTrue(departmentPersisted.isPresent());
		assertEquals("The Department to be stored", departmentPersisted.get().getName());
	}
}