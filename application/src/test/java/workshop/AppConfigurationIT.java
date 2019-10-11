package workshop;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import workshop.internal.services.DepartmentsService;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureTestEntityManager
@DirtiesContext
class AppConfigurationIT {
	
	@Autowired
	private DepartmentsService departmentsService;
	
	@Test
	@DisplayName("@PropertySource = classpath:configs/workshop.properties")
	public void workshop_Properties_Loads_From_Classpath_From_SharedResources_Module_And_Injects_Into_Beans() {
		//GIVEN
		//The workshop.properties from SharedResources/properties/configs to be loaded by Spring in AppConfiguration settings
		
		//WHEN a bean is initialized and got injected values from properties
		int pageSize = departmentsService.getDEFAULT_PAGE_SIZE();
		int maxPageNum = departmentsService.getMAX_PAGE_NUM();
		
		//THEN properties is set
		assertAll(
			() -> assertTrue(pageSize > 1),
			() -> assertTrue(maxPageNum > 1)
		);
	}
}