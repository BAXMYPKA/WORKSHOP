package internal.entities.hateoasResources;

import internal.controllers.DepartmentsController;
import internal.entities.Department;
import org.springframework.stereotype.Component;

@Component
public class DepartmentResourceAssembler extends WorkshopEntityResourceAssembler<Department> {
	
	public DepartmentResourceAssembler(DepartmentsController workshopController) {
		super(workshopController);
	}
}
