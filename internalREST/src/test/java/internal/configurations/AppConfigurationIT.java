package internal.configurations;

import internal.service.DepartmentsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureTestEntityManager
class AppConfigurationIT {
	
	@Autowired
	AppConfiguration appConfiguration;
	@Autowired
	DepartmentsService departmentsService;
	
	@Test
	@Order(1)
	@DisplayName("@PropertySource = classpath:configs/workshop.properties")
	public void workshop_Properties_Loads_From_Classpath_And_Injects_Into_Beans() {
		//GIVEN
		//A workshop.properties from SharedResources/properties/configs to be loaded by Spring in AppConfiguration settings
		
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