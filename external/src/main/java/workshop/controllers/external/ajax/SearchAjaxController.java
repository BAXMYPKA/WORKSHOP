package workshop.controllers.external.ajax;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import workshop.internal.entities.WorkshopEntity;
import workshop.internal.services.UsersService;
import workshop.internal.services.WorkshopEntitiesServiceAbstract;

import java.util.List;

@Slf4j
@Controller
@RequestMapping(path = "/ajax")
public class SearchAjaxController {
	
	
	/**
	 * @param workshopEntityType {@link String} as a representation for {@link workshop.internal.entities.WorkshopEntityType}
	 * @param propertyName       {@link String} as a representation for any existing
	 *                           {@link workshop.internal.entities.WorkshopEntityType} property.
	 * @param propertyValue      {@link String} as a representation for any existing property value.
	 * @return {@link HttpStatus#OK} if such an {@link workshop.internal.entities.WorkshopEntityType} with such a
	 * property with such a value exist.
	 * Otherwise returns {@link HttpStatus#NOT_FOUND}
	 */
	@PostMapping(path = "/entity-exist", consumes = {
		MediaType.APPLICATION_FORM_URLENCODED_VALUE,
		MediaType.APPLICATION_JSON_UTF8_VALUE,
		MediaType.MULTIPART_FORM_DATA_VALUE})
	public ResponseEntity<String> workshopEntityExist(
		@RequestParam(name = "workshopEntityType") String workshopEntityType,
		@RequestParam(name = "propertyName") String propertyName,
		@RequestParam(name = "propertyValue") String propertyValue) {
		
		List<WorkshopEntity> workshopEntities =
			WorkshopEntitiesServiceAbstract.findByWorkshopEntityType(workshopEntityType, propertyName, propertyValue);
		
		if (workshopEntities.size() > 0) {
			return ResponseEntity.ok("");
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
	}
}
