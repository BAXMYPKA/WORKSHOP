package internal.entities.hateoasResources;

import internal.controllers.DepartmentsController;
import internal.controllers.WorkshopControllerAbstract;
import internal.entities.Department;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class DepartmentResourceAssembler extends WorkshopEntityResourceAssemblerAbstract<Department> {
	
	public DepartmentResourceAssembler() {
		setWorkshopControllerAbstractClass(DepartmentsController.class);
		setWorkshopEntityClass(Department.class);
	}
	
	/**
	 * @param workshopEntity An WorkshopEntity instance to extract 'identifier' from.
	 * @return Resource<Department> with added self-rel Link and 'departments/{id}/positions' relation Link.
	 */
	@Override
	public Resource<Department> toResource(Department workshopEntity) {
		Resource<Department> selfLinkedDepartmentResource = super.toResource(workshopEntity);
		return addDepartmentPositionsLink(selfLinkedDepartmentResource);
	}
	
	private Resource<Department> addDepartmentPositionsLink(Resource<Department> departmentResource) {
		Link departmentPositionsLink =
			ControllerLinkBuilder.linkTo(
				ControllerLinkBuilder.methodOn(DepartmentsController.class)
					.getDepartmentPositions(
						departmentResource.getContent().getIdentifier(),
						getDEFAULT_PAGE_SIZE(),
						1, getDEFAULT_ORDER_BY(),
						getDEFAULT_ORDER()))
				.withRel("Positions");
		
		departmentResource.add(departmentPositionsLink);
		return departmentResource;
	}
	
	
	/**
	 * @see WorkshopEntityResourceAssemblerAbstract#getPagedLink(Pageable, int, String, String, String, String, String, String, Long)
	 */
	@Override
	protected Link getPagedLink(Pageable pageable, int pageSize, String orderBy, String order, String relation, String hrefLang,
								String media, @Nullable String title, Long departmentId) {
		if (departmentId == null) { //It is a standard 'getAll' request
			return super.getPagedLink(pageable, pageSize, orderBy, order, relation, hrefLang, media, title, departmentId);
		} else { //It is the special request
			title = title == null ? "Page " + (pageable.getPageNumber() + 1) : title;
			
			Link link =
				ControllerLinkBuilder.linkTo(
					ControllerLinkBuilder.methodOn(DepartmentsController.class)
						.getDepartmentPositions(departmentId, pageSize, pageable.getPageNumber() + 1, orderBy, order))
					.withRel(relation)
					.withHreflang(hrefLang)
					.withMedia(media)
					.withTitle(title);
			
			return link;
		}
	}
}
