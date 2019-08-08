package internal.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import internal.entities.Department;
import internal.service.DepartmentsService;
import internal.service.WorkshopEntitiesServiceAbstract;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@ExposesResourceFor(Department.class)
@RestController
@RequestMapping(path = "/internal/departments", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
public class DepartmentsController extends WorkshopControllerAbstract<Department> {
	
	
	/**
	 * @see WorkshopControllerAbstract#WorkshopControllerAbstract(WorkshopEntitiesServiceAbstract)
	 */
	public DepartmentsController(DepartmentsService departmentsService) {
		super(departmentsService);
	}
	
/*
	@Override
	@GetMapping(path = "/{id}")
	public ResponseEntity<String> getOne(@PathVariable(name = "id") long id) throws JsonProcessingException {
		Department department = getEntitiesService().findById(id);
		Link link = (ControllerLinkBuilder.linkTo(this.getClass()).withRel("testRel"));
		Resource<Department> departmentResource = new Resource<>(department, link);
		System.out.println(departmentResource.getLinks());
		String json = getJsonServiceUtils().convertEntityToJson(department);
		return ResponseEntity.ok(json);
	}
*/
}
