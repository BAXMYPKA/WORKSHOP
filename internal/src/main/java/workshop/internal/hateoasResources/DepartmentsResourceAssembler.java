package workshop.internal.hateoasResources;

import workshop.internal.controllers.rest.DepartmentsController;
import workshop.internal.entities.Department;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DepartmentsResourceAssembler extends WorkshopEntitiesResourceAssemblerAbstract<Department> {
	
	/**
	 * Obligatory constructor.
	 * Delete the method arguments and only leave:
	 * super(WorkshopControllerInstance.class, WorkshopEntityInstance.class);
	 */
	public DepartmentsResourceAssembler() {
		super(DepartmentsController.class, Department.class);
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
					.getPositions(
						departmentResource.getContent().getIdentifier(),
						getDEFAULT_PAGE_SIZE(),
						1,
						getDEFAULT_ORDER_BY(),
						getDEFAULT_ORDER()))
				.withRel("Positions");
		
		departmentResource.add(departmentPositionsLink);
		return departmentResource;
	}
	
	@Override
	protected Link getPagedLink(Pageable pageable, int pageNum, String relation, String hrefLang, String media, String title, Long ownerId, String controllerMethodName) {
		return null;
	}
}
