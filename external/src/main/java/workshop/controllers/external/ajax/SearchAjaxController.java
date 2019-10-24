package workshop.controllers.external.ajax;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import workshop.internal.services.UsersService;
import workshop.internal.services.WorkshopEntitiesServiceAbstract;

@Slf4j
@Controller
@RequestMapping(path = "/ajax")
public class SearchAjaxController {
	
	@Autowired
	private UsersService usersService;
	
	@PostMapping(path = "/entity-exist", consumes = {
		MediaType.APPLICATION_FORM_URLENCODED_VALUE,
		MediaType.APPLICATION_JSON_UTF8_VALUE,
		MediaType.MULTIPART_FORM_DATA_VALUE})
	public ResponseEntity<String> workshopEntityExist(
		@RequestParam(name = "workshopEntityType") String workshopEntityType,
		@RequestParam(name = "propertyName") String propertyName,
		@RequestParam(name = "propertyValue") String propertyValue) {
		
//		usersService.findByWorkshopEntityType(workshopEntityType, propertyName, propertyValue);
		
		boolean exist = propertyValue.contains("pro");
		if (exist) {
			return ResponseEntity.ok("FOUND");
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("NOT FOUND");
		}
	}
}
