package internal.controllers;

import internal.entities.Department;
import internal.services.DepartmentsService;
import internal.services.WorkshopEntitiesServiceAbstract;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/internal/departments", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
@ExposesResourceFor(Department.class)
public class DepartmentsController extends WorkshopControllerAbstract<Department> {
	
	
	/**
	 * @see WorkshopControllerAbstract#WorkshopControllerAbstract(WorkshopEntitiesServiceAbstract)
	 */
	public DepartmentsController(DepartmentsService departmentsService) {
		super(departmentsService);
	}
	
	@GetMapping(path = "/{id}")
	public ResponseEntity<String> getAllPositions(@PathVariable(name = "id") long id) {
		if (!getWorkshopEntitiesService().isExist(id)) {
			return new ResponseEntity<>(getMessageSource().getMessage(
				"httpStatus.notAcceptable.identifier(1)", new Object[]{id}, LocaleContextHolder.getLocale()),
				HttpStatus.NOT_FOUND);
		}
		
		return ResponseEntity.ok("");
	}
}
