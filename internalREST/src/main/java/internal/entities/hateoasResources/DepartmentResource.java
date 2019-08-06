package internal.entities.hateoasResources;

import internal.entities.Department;

public class DepartmentResource extends WorkshopEntityResource<Department> {
	
	/**
	 * @param department The concrete instance of the WorkshopEntity to be the HATEOAS Resource.
	 */
	public DepartmentResource(Department department) {
		super(department);
	}
}
