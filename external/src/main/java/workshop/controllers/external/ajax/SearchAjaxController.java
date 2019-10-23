package workshop.controllers.external.ajax;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import workshop.internal.services.UsersService;

@Slf4j
@Controller
@RequestMapping(path = "/ajax")
public class SearchAjaxController {
	
	@Autowired
	private UsersService usersService;
	
	@PostMapping(path = "/entity-exist",
		consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.APPLICATION_JSON_UTF8_VALUE})
	public ResponseEntity<String> workshopEntityExist(@RequestAttribute(name = "workshopEntityType", required = false) String workshopEntityType,
													  @RequestAttribute(name = "propertyName", required = false) String propertyName,
													  @RequestAttribute(name = "propertyValue", required = false) String propertyValue) {
		System.out.println(workshopEntityType + "  " + propertyName + "  " + propertyValue);
		boolean exist = true;
		return ResponseEntity.ok("{exist: " + exist + "}");
	}
}
