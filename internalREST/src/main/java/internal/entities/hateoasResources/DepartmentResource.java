package internal.entities.hateoasResources;

import internal.controllers.DepartmentsController;
import internal.entities.Department;

public class DepartmentResource extends WorkshopEntityResource<Department> {
	
	/**
	 * @param department     The concrete instance of the WorkshopEntity to be the HATEOAS Resource.
	 * @param departmentsController The concrete instance of the WorkshopController to get Links for this WorkshopEntity
	 */
	public DepartmentResource(Department department, DepartmentsController departmentsController) throws Throwable {
		super(department, departmentsController);
	}
}
