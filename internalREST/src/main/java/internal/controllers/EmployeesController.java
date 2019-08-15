package internal.controllers;

import internal.entities.Employee;
import internal.services.EmployeesService;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/internal/employees", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
@ExposesResourceFor(Employee.class)
public class EmployeesController extends WorkshopControllerAbstract<Employee> {
	
	/**
	 * @param employeesService By this instance we set the concrete instance of WorkshopServiceAbstract
	 *                        and through it set the concrete type of WorkshopEntity as {@link #getWorkshopEntityClass()}
	 *                        to operate with.
	 */
	public EmployeesController(EmployeesService employeesService) {
		super(employeesService);
	}
}
