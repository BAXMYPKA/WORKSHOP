package internal.hateoasResources;

import internal.controllers.DepartmentsController;
import internal.entities.Department;
import internal.entities.Position;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class DepartmentsResourceAssembler extends WorkshopEntitiesResourceAssemblerAbstract<Department> {
	
	public DepartmentsResourceAssembler() {
		setWorkshopControllerAbstractClass(DepartmentsController.class);
		setWorkshopEntityClass(Department.class);
	}
	
	/**
	 * Overridden to add a Link to Positions of every Department
	 *
	 * @param workshopEntity An WorkshopEntity instance to extract 'identifier' from.
	 * @return Resource<Department> with added self-rel Link and 'departments/{id}/positions' relation Link.
	 */
	@Override
	public Resource<Department> toResource(Department workshopEntity) {
		Resource<Department> selfLinkedDepartmentResource = super.toResource(workshopEntity);
		return addPositionsFromDepartmentLink(selfLinkedDepartmentResource);
	}
	
	private Resource<Department> addPositionsFromDepartmentLink(Resource<Department> departmentResource) {
		Link departmentPositionsLink =
			ControllerLinkBuilder.linkTo(
				ControllerLinkBuilder.methodOn(DepartmentsController.class)
					.getDepartmentPositions(
						departmentResource.getContent().getIdentifier(),
						getDEFAULT_PAGE_SIZE(),
						1,
						getDEFAULT_ORDER_BY(),
						getDEFAULT_ORDER()))
				.withRel("Positions");
		
		departmentResource.add(departmentPositionsLink);
		return departmentResource;
	}
}
